package org.apache.ode.axis2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.utils.GUID;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class P2PMexContextImpl implements MessageExchangeContext {
    private static final Log __log = LogFactory.getLog(P2PMexContextImpl.class);

    private MessageExchangeContext _wrapped;

    private ODEServer _server;

    private Scheduler _scheduler;

    private Map<String, PartnerRoleMessageExchange> _waiters = Collections
            .synchronizedMap(new HashMap<String, PartnerRoleMessageExchange>());

    public P2PMexContextImpl(ODEServer server, MessageExchangeContext wrapped, Scheduler scheduler) {
        _server = server;
        _wrapped = wrapped;
        _scheduler = scheduler;
    }

    public void invokePartner(final PartnerRoleMessageExchange mex) throws ContextException {
        ExternalService target = (ExternalService) mex.getChannel();
        ODEService myService = _server.getService(target.getServiceName(), target.getPortName());

        // If we have direct access to the other process (i.e. it is locally
        // deployed), then we would like to avoid the
        // whole SOAP step. In this case we do direct invoke.
        if (myService != null) {
            // Defer invoke until tx is comitted.
            _scheduler.registerSynchronizer(new Scheduler.Synchronizer() {

                public void afterCompletion(boolean success) {
                    if (!success)
                        return;

                    try {
                        _scheduler.execIsolatedTransaction(new Callable<Void>() {

                            public Void call() throws Exception {
                                buildAndInvokeMyRoleMex(mex);
                                return null;
                            }
                        });

                    } catch (Exception ex) {
                        __log.error("Unexpected error", ex);
                        throw new RuntimeException(ex);
                    }

                }

                public void beforeCompletion() {
                }
            });

            if (mex.getMessageExchangePattern() == MessageExchange.MessageExchangePattern.REQUEST_RESPONSE) 
                _waiters.put(mex.getMessageExchangeId(),mex);
            // There is no way we can get a synchronous response.
            mex.replyAsync();
        } else {
            _wrapped.invokePartner(mex);
        }
    }

    public void onAsyncReply(MyRoleMessageExchange myRoleMex) throws BpelEngineException {
        PartnerRoleMessageExchange pmex = _waiters.remove(myRoleMex.getMessageExchangeId());
        if (pmex == null) {
            _wrapped.onAsyncReply(myRoleMex);
            return;
        }

        handleResponse(pmex, myRoleMex);

    }

    private MyRoleMessageExchange buildAndInvokeMyRoleMex(PartnerRoleMessageExchange pmex) {
        ExternalService target = (ExternalService) pmex.getChannel();

        // Creating message exchange
        String messageId = new GUID().toString();

        MyRoleMessageExchange odeMex = _server.getBpelServer().getEngine().createMessageExchange("" + messageId,
                target.getServiceName(), pmex.getOperationName());
        __log.debug("ODE routed to operation " + pmex.getOperationName() + " from service " + target.getServiceName());

        copyHeader(pmex, odeMex);

        if (__log.isDebugEnabled()) {
            __log.debug("Invoking ODE using MEX " + odeMex);
        }

        odeMex.invoke(pmex.getRequest());
        if (odeMex.getStatus() != MessageExchange.Status.ASYNC) {
            _waiters.remove(pmex.getMessageExchangeId());
            handleResponse(pmex, odeMex);
        }

        return odeMex;
    }

    private void handleResponse(PartnerRoleMessageExchange pmex, MyRoleMessageExchange myRoleMex) {

        switch (myRoleMex.getStatus()) {
        case FAILURE:
            // We can't seem to get the failure out of the myrole mex?
            pmex.replyWithFailure(MessageExchange.FailureType.OTHER, "operation failed", null);
            break;
        case FAULT:
            // note, we are reusing the message object from the my role mex..
            // not quite kosher.
            pmex.replyWithFault(myRoleMex.getFault(), myRoleMex.getFaultResponse());
            break;
        case RESPONSE:
            pmex.reply(myRoleMex.getResponse());
            break;
        default:
            pmex.replyWithFailure(MessageExchange.FailureType.NO_RESPONSE, "no response received", null);
            break;

        }

    }

    private void copyHeader(MessageExchange source, MessageExchange dest) {
        if (source.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID) != null)
            dest.setProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID, source
                    .getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID));
        if (source.getProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID) != null)
            dest.setProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID, source
                    .getProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID));
    }

}
