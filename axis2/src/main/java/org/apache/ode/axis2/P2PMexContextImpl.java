package org.apache.ode.axis2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.w3c.dom.Element;

import javax.transaction.TransactionManager;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class P2PMexContextImpl implements MessageExchangeContext {
    private static final Log __log = LogFactory.getLog(P2PMexContextImpl.class);

    private MessageExchangeContext _wrapped;
    private ODEServer _server;
    private ExecutorService _executorService;
    private TransactionManager _txMgr;

    private Map<String, ODEService.ResponseCallback> _waitingCallbacks =
            new HashMap<String, ODEService.ResponseCallback>();


    public P2PMexContextImpl(ODEServer server, MessageExchangeContext wrapped,
                             ExecutorService executorService, TransactionManager txMgr) {
        _server = server;
        _wrapped = wrapped;
        _executorService = executorService;
        _txMgr = txMgr;
    }

    public void invokePartner(final PartnerRoleMessageExchange mex) throws ContextException {
        ExternalService target = (ExternalService) mex.getChannel();
        ODEService myService = _server.getService(target.getServiceName(), target.getPortName());
        if (myService != null) {
            MyRoleMessageExchange odeMex = null;

            // Starting a new transaction in a new thread
            Future<MyRoleMessageExchange> futureMex = _executorService.submit(new Callable<MyRoleMessageExchange>() {
                public MyRoleMessageExchange call() throws Exception {
                    MyRoleMessageExchange mmex = null;
                    Exception thrown;
                    int retryCount = 0;
                    do {
                        try {
                            _txMgr.begin();
                            mmex = buildAndSendMex(mex);
                            _txMgr.commit();
                            thrown = null;
                        } catch (Exception e) {
                            _txMgr.rollback();
                            retryCount++;
                            thrown = e;
                        }
                    } while (thrown != null && retryCount < 3);

                    if (thrown != null) throw thrown;
                    return mmex;
                }
            });

            if (mex.getMessageExchangePattern() != MessageExchange.MessageExchangePattern.REQUEST_ONLY) {
                // Partner MEX must be failed in the caller's transaction
                try {
                    odeMex = futureMex.get();
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof FailureException) {
                        FailureException fe = (FailureException)e.getCause();
                        failExchange(mex, fe._failureType, fe._explanation);
                    } else {
                        failExchange(mex, MessageExchange.FailureType.OTHER, "Message exchange " + odeMex +
                                " produced an unexpected exception while being processed by the engine!");
                    }
                } catch (Exception e) {
                    failExchange(mex, MessageExchange.FailureType.OTHER, "Message exchange " + odeMex +
                            " produced an unexpected exception while being processed by the engine!");
                }

                if (odeMex != null) {
                    // Cleaning up
                    _waitingCallbacks.remove(odeMex.getMessageExchangeId());
                    try {
                        onResponse(mex, odeMex);
                    } catch (Exception e) {
                        failExchange(mex, MessageExchange.FailureType.FORMAT_ERROR,
                                "Got an exception when handling the response: " + e.toString());
                    }
                }
            }
        } else {
            _wrapped.invokePartner(mex);
        }
    }

    public void onAsyncReply(MyRoleMessageExchange myRoleMex) throws BpelEngineException {
        ODEService.ResponseCallback callback = _waitingCallbacks.get(myRoleMex.getClientId());
        if (callback == null) {
            __log.debug("No active service for message exchange " + myRoleMex + " on process to process interaction.");
            _wrapped.onAsyncReply(myRoleMex);
        } else {
            callback.onResponse(myRoleMex);
            _waitingCallbacks.remove(myRoleMex.getClientId());
        }
    }

    private MyRoleMessageExchange buildAndSendMex(PartnerRoleMessageExchange pmex) throws FailureException {
        ExternalService target = (ExternalService) pmex.getChannel();

        // Creating message exchange
        String messageId = new GUID().toString();

        MyRoleMessageExchange odeMex = _server.getBpelServer().getEngine().createMessageExchange("" + messageId,
                target.getServiceName(), pmex.getOperationName());
        __log.debug("ODE routed to operation " + pmex.getOperationName() + " from service " + target.getServiceName());

        ODEService.ResponseCallback callback = null;
        if (odeMex.getOperation() != null) {
            // Preparing message to send to ODE
            Message odeRequest = odeMex.createMessage(odeMex.getOperation().getInput().getMessage().getQName());
            copyHeader(pmex, odeMex);
            odeRequest.setMessage(pmex.getRequest().getMessage());

            // Preparing a callback just in case we would need one.
            if (odeMex.getOperation().getOutput() != null) {
                callback = new ODEService.ResponseCallback();
                _waitingCallbacks.put(odeMex.getClientId(), callback);
            }

            if (__log.isDebugEnabled()) {
                __log.debug("Invoking ODE using MEX " + odeMex);
                __log.debug("Message content:  " + DOMUtils.domToString(odeRequest.getMessage()));
            }

            // Invoking ODE
            odeMex.invoke(odeRequest);

            boolean timeout = false;
            // Invocation response could be delayed, if so we have to wait for it.
            if (odeMex.getMessageExchangePattern() == MessageExchange.MessageExchangePattern.REQUEST_RESPONSE &&
                    odeMex.getStatus() == MessageExchange.Status.ASYNC) {
                odeMex = callback.getResponse(ODEService.TIMEOUT);
                if (odeMex == null) timeout = true;
            }

            if (timeout) {
                __log.error("Timeout when waiting for response to MEX " + odeMex);
                throw new FailureException(MessageExchange.FailureType.ABORTED, "Timeout after " +
                        ODEService.TIMEOUT + "ms");
            }
        } else {
            // Somethings's wrong
            throw new FailureException(MessageExchange.FailureType.UNKNOWN_OPERATION, "Operation to invoke is null!");
        }

        return odeMex;
    }

    private void onResponse(PartnerRoleMessageExchange mex, MyRoleMessageExchange responseMex) {
        switch (responseMex.getStatus()) {
            case FAULT:
                mex.replyWithFault(responseMex.getFault(), responseMex.getFaultResponse());
            case ASYNC:
            case RESPONSE:
                Element responseElmt = responseMex.getResponse().getMessage();
                if (__log.isDebugEnabled()) __log.debug("Received response message " +
                        responseElmt == null ? "null" : DOMUtils.domToString(responseElmt));
                Message response = mex.createMessage(responseMex.getOperation().getOutput().getMessage().getQName());
                __log.debug("Received synchronous response for MEX " + responseMex);
                __log.debug("Message: " + DOMUtils.domToString(responseElmt));
                response.setMessage(responseElmt);
                mex.reply(response);
                break;
            case FAILURE:
                failExchange(mex, MessageExchange.FailureType.COMMUNICATION_ERROR, "The called process failed " +
                        "in a process to process interaction.");
                break;
            default:
                failExchange(mex, MessageExchange.FailureType.NO_RESPONSE, "The called process failed " +
                        "to respond properly in a process to process interaction.");
                __log.warn("Received ODE message exchange in unexpected state: " + mex.getStatus());
        }
    }

    private void copyHeader(MessageExchange source, MessageExchange dest) {
        if (source.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID) != null)
            dest.setProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID,
                    source.getProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID));
        if (source.getProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID) != null)
            dest.setProperty(MessageExchange.PROPERTY_SEP_MYROLE_SESSIONID,
                    source.getProperty(MessageExchange.PROPERTY_SEP_PARTNERROLE_SESSIONID));
    }

    private void failExchange(PartnerRoleMessageExchange mex, MessageExchange.FailureType failure, String explanation) {
        __log.error("Failure while sending mex " + mex + ": " + explanation);
        mex.replyWithFailure(failure, explanation, null);
    }

    private static class FailureException extends Exception {
        public MessageExchange.FailureType _failureType;
        public String _explanation;

        public FailureException(MessageExchange.FailureType failureType, String explanation) {
            super(explanation);
            _explanation = explanation;
            _failureType = failureType;
        }
    }
}
