package org.apache.ode.jbi;


import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.FailureType;

/**
 * 
 * @author mszefler
 *
 */
class OdeConsumerAsync extends OdeConsumer {
    private static final Log __log = LogFactory.getLog(OdeConsumerAsync.class);

    /** 
     * We create an executor to handle all the asynchronous invocations/timeouts. Note, we don't need a lot of threads
     * here, the operations are all async, using single-thread executor avoids any possible problems in concurrent
     * use of delivery channel.
     */
    private ScheduledExecutorService _executor;

    OdeConsumerAsync(OdeContext ode) {
        super(ode);
       _executor = Executors.newSingleThreadScheduledExecutor();

    }

    @Override
    protected void doSendOneWay(final PartnerRoleMessageExchange odeMex, final InOnly inonly) {
        _executor.submit(new Runnable() {
            public void run() {
                try {
                    _outstandingExchanges.put(inonly.getExchangeId(), odeMex);
                    _ode.getChannel().send(inonly);
                } catch (MessagingException e) {
                    String errmsg = "Error sending request-only message to JBI for ODE mex " + odeMex;
                    __log.error(errmsg, e);
                }
            }
        });

    }

    @Override
    protected void doSendTwoWay(final PartnerRoleMessageExchange odeMex, final InOut inout) {
        _executor.submit(new Runnable() {
            public void run() {
                try {
                    _outstandingExchanges.put(inout.getExchangeId(), odeMex);
                    _executor.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            doTimeoutCheck(inout);
                        }

                    }, _responseTimeout, TimeUnit.MILLISECONDS);
                    _ode.getChannel().send(inout);
                } catch (MessagingException e) {
                    String errmsg = "Error sending request-only message to JBI for ODE mex " + odeMex;
                    __log.error(errmsg, e);
                }
            }
        });

    }

    private void doTimeoutCheck(InOut inout) {
        final PartnerRoleMessageExchange pmex = _outstandingExchanges.remove(inout.getExchangeId());

        if (pmex == null) /* no worries, got a response. */
            return;

        __log.warn("Timeout on JBI message exchange " + inout.getExchangeId());

        try {
            _ode._scheduler.execIsolatedTransaction(new Callable<Void>() {
                public Void call() throws Exception {
                    pmex.replyWithFailure(FailureType.NO_RESPONSE, "Response not received after " + _responseTimeout + "ms.", null);
                    return null;
                }

            });
        } catch (Exception ex) {
            __log.error("Error executing transaction:  ", ex);
        }
    }
}
