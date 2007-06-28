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

package org.apache.ode.bpel.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange.CorrelationStatus;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.iapi.Scheduler.JobInfo;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.utils.msg.MessageBundle;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Transaction process engine.
 * 
 * @author mszefler
 * @author Matthieu Riou <mriou at apache dot org>
 */
class BpelEngineImpl {
    private static final Log __log = LogFactory.getLog(BpelEngineImpl.class);

    /** RNG, for delays */
    private Random _random = new Random(System.currentTimeMillis());

    private static double _delayMean = 0;

    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    /** Maximum number of tries for async jobs. */
    private static final int MAX_RETRIES = 3;

    final Contexts _contexts;

    BpelEngineImpl(Contexts contexts) {
        _contexts = contexts;
    }

     OProcess getOProcess(QName processId) {
        BpelProcess process = _activeProcesses.get(processId);

        if (process == null)
            return null;

        return process.getOProcess();
    }

    public void processJob(WorkEvent we) throws BpelEngineException {
        }
    }

    private boolean checkRetry(final JobInfo jobInfo, Throwable t) {
        // TODO, better handling of failed jobs (put them in the DB perhaps?)
        if (jobInfo.retryCount < MAX_RETRIES)
            return true;

        __log.error("Job could not be completed after " + MAX_RETRIES + ": " + jobInfo, t);

        boolean saveToDisk = false;
        if (jobInfo.jobDetail.get("final") == null) {
            __log.error("Rescheduling problematic job for a bit later: " + jobInfo, t);

            try {
                if (jobInfo.jobDetail.get("inmem") != null)
                    _contexts.scheduler.scheduleVolatileJob(true, jobInfo.jobDetail);
                else
                    _contexts.scheduler.execIsolatedTransaction(new Callable<Void>() {
                        public Void call() throws Exception {
                            jobInfo.jobDetail.put("final", true);
                            _contexts.scheduler.schedulePersistedJob(jobInfo.jobDetail, new Date(
                                    System.currentTimeMillis() + 60 * 1000));
                            return null;
                        }
                    });
            } catch (Exception ex) {
                __log.error("Error rescheduling problematic job: " + jobInfo, ex);
                saveToDisk = true;
            }
        } else {
            saveToDisk = true;
        }

        if (saveToDisk)
            try {
                File f = File.createTempFile("ode-bad-job", ".ser", new File(""));
                ObjectOutputStream fos = new ObjectOutputStream(new FileOutputStream(f));
                fos.writeObject(jobInfo);
                fos.close();
                __log.error("Saved problematic job to disk (last resort): " + jobInfo + " in file " + f);
            } catch (Exception ex) {
                __log.error("Could not save bad job; it will be lost: " + jobInfo, ex);
            }

        // No more retries.
        return false;
    }

    /**
     * Block the thread for random amount of time. Used for testing for races and the like. The delay generated is exponentially
     * distributed with the mean obtained from the <code>ODE_DEBUG_TX_DELAY</code> environment variable.
     */
    private void debuggingDelay() {
        // Do a delay for debugging purposes.
        if (_delayMean != 0)
            try {
                long delay = randomExp(_delayMean);
                // distribution
                // with mean
                // _delayMean
                __log.warn("Debugging delay has been activated; delaying transaction for " + delay + "ms.");
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                ; // ignore
            }
    }

    private long randomExp(double mean) {
        double u = _random.nextDouble(); // Uniform
        long delay = (long) (-Math.log(u) * mean); // Exponential
        return delay;
    }

    void fireEvent(BpelEvent event) {
        // Note that the eventListeners list is a copy-on-write array, so need
        // to mess with synchronization.
        for (org.apache.ode.bpel.iapi.BpelEventListener l : _contexts.eventListeners) {
            l.onEvent(event);
        }
    }

    /**
     * Get the list of globally-registered message-exchange interceptors.
     * 
     * @return list
     */
    List<MessageExchangeInterceptor> getGlobalInterceptors() {
        return _contexts.globalIntereceptors;
    }


}
