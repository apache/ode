package org.apache.ode.bpel.engine;

import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.engine.MessageExchangeImpl.Change;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.intercept.AbortMessageExchangeException;
import org.apache.ode.bpel.intercept.FaultMessageExchangeException;
import org.apache.ode.bpel.intercept.InterceptorInvoker;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor.InterceptorContext;
import org.apache.ode.bpel.o.OPartnerLink;
import org.w3c.dom.Element;

abstract class MyRoleMessageExchangeImpl extends MessageExchangeImpl implements MyRoleMessageExchange {

    private static final Log __log = LogFactory.getLog(MyRoleMessageExchangeImpl.class);

    protected final QName _callee;

    protected CorrelationStatus _cstatus;

    protected String _clientId;

    public MyRoleMessageExchangeImpl(BpelProcess process, String mexId, OPartnerLink oplink, Operation operation, QName callee) {
        super(process, null, mexId, oplink, oplink.myRolePortType, operation);
        _callee = callee;
    }

    public CorrelationStatus getCorrelationStatus() {
        return _cstatus;
    }
  
    @Override
    void load(MessageExchangeDAO dao) {
        super.load(dao);
        _cstatus = dao.getCorrelationStatus() == null ? null : CorrelationStatus.valueOf(dao.getCorrelationStatus());
        _clientId = dao.getPartnersKey();
    }

    @Override
    public void save(MessageExchangeDAO dao) {
        super.save(dao);
        dao.setCorrelationStatus(_cstatus == null ? null : _cstatus.toString());
        dao.setPartnersKey(_clientId);
        dao.setCallee(_callee);
        
        if (_changes.contains(Change.REQUEST)) {
            _changes.remove(Change.REQUEST);
            MessageDAO requestDao = dao.createMessage(_request.getType());
            requestDao.setData(_request.getMessage());   
            dao.setRequest(requestDao);
        }
        
    }

    public FailureType getFailureType() {
        if (getStatus() != Status.ACK || getAckType() != AckType.FAILURE)
            throw new IllegalStateException("MessageExchange did not fail!");
        
        return _failureType;
    }
    
    public String getClientId() {
        return _clientId;
    }

    public Future<Status> invokeAsync() {
        throw new BpelEngineException("Unsupported InvocationStyle");
    }

    public Status invokeBlocking() throws BpelEngineException, TimeoutException {
        throw new BpelEngineException("Unsupported InvocationStyle");
    }

    public void invokeReliable() {
        throw new BpelEngineException("Unsupported InvocationStyle");

    }

    public Status invokeTransacted() throws BpelEngineException {
        throw new BpelEngineException("Unsupported InvocationStyle");
    }

    public void setRequest(final Message request) {
        _request = (MessageImpl) request;
        _changes.add(Change.REQUEST);
    }

    public QName getServiceName() {
        return _callee;
    }

    public String toString() {
        try {
            return "{MyRoleMex#" + _mexId + " [Client " + _clientId + "] calling " + _callee + "." + getOperationName() + "(...)}";
        } catch (Throwable t) {
            return "{MyRoleMex#???}";
        }
    }

    public void complete() {
        // TODO Auto-generated method stub

    }

    protected void scheduleInvoke() {

        assert !_process.isInMemory() : "Cannot schedule invokes for in-memory processes.";
        assert _contexts.isTransacted() : "Cannot schedule outside of transaction context.";

        // Schedule a new job for invocation
        final WorkEvent we = new WorkEvent();
        we.setType(WorkEvent.Type.MYROLE_INVOKE);
        we.setProcessId(_process.getPID());
        we.setMexId(_mexId);

        // Schedule a timeout
        final WorkEvent we1 = new WorkEvent();
        we1.setType(WorkEvent.Type.MYROLE_INVOKE_TIMEOUT);
        we1.setProcessId(_process.getPID());
        we1.setMexId(_mexId);

        _contexts.scheduler.schedulePersistedJob(we.getDetail(), null);
        _contexts.scheduler.schedulePersistedJob(we1.getDetail(), null);

    }

    /**
     * Process the message-exchange interceptors.
     * 
     * @param mex
     *            message exchange
     * @return <code>true</code> if execution should continue, <code>false</code> otherwise
     */
    protected boolean processInterceptors(InterceptorInvoker invoker, MessageExchangeDAO mexDao) {
        // TODO: should we give the in-mem dao connection for interceptors on in-mem processes?
        InterceptorContextImpl ictx = new InterceptorContextImpl(_contexts.dao.getConnection(), mexDao.getProcess(), null);

        for (MessageExchangeInterceptor i : _contexts.globalIntereceptors)
            if (!processInterceptor(i, this, ictx, invoker))
                return false;

        return true;
    }

    protected boolean processInterceptor(MessageExchangeInterceptor i, MyRoleMessageExchangeImpl mex, InterceptorContext ictx,
            InterceptorInvoker invoker) {
        __log.debug(invoker + "--> interceptor " + i);
        try {
            invoker.invoke(i, mex, ictx);
        } catch (FaultMessageExchangeException fme) {
            __log.debug("interceptor " + i + " caused invoke on " + this + " to be aborted with FAULT " + fme.getFaultName());
            mex.serverFaulted(fme.getFaultName(), fme.getFaultData());
            return false;
        } catch (AbortMessageExchangeException ame) {
            __log.debug("interceptor " + i + " cause invoke on " + this + " to be aborted with FAILURE: " + ame.getMessage());
            mex.serverFailed(MessageExchange.FailureType.ABORTED, __msgs.msgInterceptorAborted(mex.getMessageExchangeId(), i
                    .toString(), ame.getMessage()), null);
            return false;
        }
        return true;
    }

    protected void onStateChanged(MessageExchangeDAO mexdao, Status oldstatus, final Status newstatus) {
        MessageDAO response = mexdao.getResponse();
        if (newstatus == Status.ACK)
            switch (mexdao.getAckType()) {
            case RESPONSE: {
                final Element msg = response.getData();
                final QName msgtype = response.getType();
                _process.scheduleRunnable(new Runnable() {
                    public void run() {
                        serverResponded(new MemBackedMessageImpl(msg, msgtype, true));
                    }
                });
            }
                break;
            case FAULT: {
                final QName fault = mexdao.getFault();
                final Element faultMsg = response.getData();
                final QName msgtype = response.getType();
                _process.scheduleRunnable(new Runnable() {
                    public void run() {
                        serverFaulted(fault, new MemBackedMessageImpl(faultMsg, msgtype, true));
                    }
    
                });
            }
                break;
            case FAILURE:
                final String failureExplanation = mexdao.getFaultExplanation();
                final FailureType ftype = FailureType.valueOf(mexdao.getFailureType());
                _process.scheduleRunnable(new Runnable() {
                    public void run() {
                        serverFailed(ftype, failureExplanation, null); // TODO add failure detail
                    }
    
                });
                break;
            }
    }

    protected void finalize() {
        _process.unregisterMyRoleMex(this);
    }

    
    void serverFaulted(QName faultType, Message outputFaultMessage) throws BpelEngineException {
        _fault = faultType;
        _response = (MessageImpl) outputFaultMessage;
        ack(AckType.FAULT);
    }

   
    void serverResponded(Message outputMessage) {
        _fault = null;
        _explanation = null;
        _response = (MessageImpl) outputMessage;
        _response.makeReadOnly();
        ack(AckType.RESPONSE);

    }

    void serverFailed(FailureType type, String reason, Element details) {
        _failureType = type;
        _explanation = reason;
        ack(AckType.FAILURE);
    }

}
