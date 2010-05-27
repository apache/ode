/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.jbi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Receiver pulls messages from the NMS and submits them to ODE for further processing.
 */
public class Receiver implements Runnable {
    private static final Log __log = LogFactory.getLog(Receiver.class);

    // default time to wait for MessageExchanges, in seconds
    private static final long ACCEPT_TIMEOUT = 1L;

    // default time to wait for the ExecutorService to shut down, in seconds
    private static final long THREADPOOL_SHUTDOWN_TIMEOUT = 10L;

    // default number of threads in the thread pool
    private static final int THREADPOOL_SIZE = 8;

    private OdeContext _odeContext = null;

    private DeliveryChannel _channel = null;

    /** Receiver-Running Flag. */
    private AtomicBoolean _isRunning = new AtomicBoolean(false);

    /** Receiver-Started Flag. */
    private AtomicBoolean _isStarted = new AtomicBoolean(false);

    private Thread _thread;

    // thread pool for dispatching received messages
    private ExecutorService _executorService;

    /**
     * Constructor for creating instance of this class.
     *
     * @param context
     *            for receiving environment parameters.
     */
    public Receiver(OdeContext context) {
        _odeContext = context;
        _thread = new Thread(this);
        _executorService = Executors.newFixedThreadPool(THREADPOOL_SIZE);
    }

    /**
     * Start the receiver thread.
     */
    public void start() {
        if (_isStarted.compareAndSet(false, true)) {
            _isRunning.set(true);
            _thread.start();
        } else
            throw new IllegalStateException("Receiver cannot be restarted.");
    }

    /**
     * This is called to gracefully stop the Receiver thread. After shutting down the thread pool we wait for a maximum
     * of 10 seconds before forcefully canceling in-flight threads.
     */
    public void cease() {

        if (!_isStarted.get())
            return;

        __log.info("Receiver is ceasing.");

        if (_isRunning.compareAndSet(true, false)) {
            try {
                // This should not take more ACCEPT_TIMEOUT seconds, we
                // give it three times as much time.
                _thread.join(3 * TimeUnit.SECONDS.toMillis(ACCEPT_TIMEOUT));

                // Odd, we should not be alive at this point.
                if (_thread.isAlive()) {
                    __log.warn("Receiver thread is not dying gracefully; interrupting.");
                    _thread.interrupt();
                }

                // Try joining again.
                _thread.join(3 * TimeUnit.SECONDS.toMillis(ACCEPT_TIMEOUT));

                // If it's not dead yet, we got a problem we can't deal with.
                if (_thread.isAlive()) {
                    __log.fatal("Receiver thread is not dying gracefully despite our insistence!.");
                }

                // In any case, next step is to shutdown the thread pool
                _executorService.shutdown();

                // make sure no outstanding threads are hanging around
                if (!_executorService.awaitTermination(THREADPOOL_SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                    __log.warn("Problem shutting down ExecutorService - trying harder.");
                    List<Runnable> outstanding = _executorService.shutdownNow();
                    if (outstanding != null && !outstanding.isEmpty()) {
                        __log.warn("Cancelled " + outstanding.size() + " in-flight threads.");
                    }
                }
            } catch (InterruptedException ie) {
                __log.warn("Interrupted during cease(): ", ie);
            }

            // just to be sure..
            _executorService.shutdown();
            __log.info("Receiver ceased.");

            _executorService = null;
            _thread = null;
            _odeContext = null;
            _channel = null;
        }
    }

    /**
     * We periodically poll for input messages, blocking for 1 sec on the accept() call to receive messages. Depending
     * on runFlag status we either try to again poll again or exit.
     */
    public void run() {
        __log.info("Receiver is executing.");

        try {
            _channel = _odeContext.getContext().getDeliveryChannel();
            if (_channel == null) {
                __log.fatal("No Channel!");
                return;
            }
        } catch (MessagingException ex) {
            __log.fatal("Error getting channel! ", ex);
            return;
        }

        while (_isRunning.get()) {
            final MessageExchange messageExchange;
            try {
                messageExchange = _channel.accept(TimeUnit.SECONDS.toMillis(ACCEPT_TIMEOUT));
                if (messageExchange != null) {
                    if (__log.isTraceEnabled()) {
                        __log.trace("Got JBI message for endpoint: " + messageExchange.getEndpoint().getEndpointName());
                    }

                    // Even if we got a message exchange, we only run it
                    // if we have not been told to cease.
                    if (_isRunning.get()) {
                        if (__log.isTraceEnabled()) {
                            __log.trace("Scheduling execution of " + messageExchange.getExchangeId());
                        }
                        _executorService.submit(new Runnable() {
                            public void run() {
                                try {
                                    _odeContext._jbiMessageExchangeProcessor.onJbiMessageExchange(messageExchange);
                                } catch (Throwable t) {
                                    __log.error("Error processing JBI message.", t);
                                }
                            }
                        });
                    } else {
                        __log.warn("Skipping processing of message exchange " + messageExchange.getExchangeId()
                                + "; component no longer active.");
                    }
                }
            } catch (MessagingException mex) {
                if (_isRunning.get())
                    __log.warn("Receiver exiting due to MessagingException:", mex);
                else
                    __log.info("Receiver finished.");
                break;
            } catch (Exception ex) {
                if (!_isRunning.get()) {
                    __log.info("Receiver finished.");
                    break;
                }
                __log.warn("Caught unexpected Exception: ", ex);
                return;
            }
        }

        __log.info("Receiver finished.");
    }
}
