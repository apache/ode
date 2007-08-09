package org.apache.ode.bpel.engine;

import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.intercept.AbortMessageExchangeException;
import org.apache.ode.bpel.intercept.FaultMessageExchangeException;
import org.apache.ode.bpel.intercept.InterceptorInvoker;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor.InterceptorContext;
import org.apache.ode.bpel.o.OPartnerLink;

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

  
    protected MessageExchangeDAO doInvoke() {

        if (getStatus() != Status.NEW)
            throw new IllegalStateException("Invalid state: " + getStatus());
        
        request();
        
        MessageExchangeDAO dao = _process.createMessageExchange(getMessageExchangeId(), MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE);
        save(dao);
        
        if (__log.isDebugEnabled())
            __log.debug("invoke() EPR= " + _epr + " ==> " + _process);
        
        try {
            if (!processInterceptors(InterceptorInvoker.__onBpelServerInvoked, dao)) {
                assert getStatus() == Status.ACK;
                return dao;
            }

            _process.invokeProcess(dao);
        } finally {
            if (dao.getStatus() == Status.ACK) {
                _failureType = dao.getFailureType();
                _fault = dao.getFault();
                _explanation  = dao.getFaultExplanation();
                ack(dao.getAckType());
            }
        }
        
        return dao;
        
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
            if (!processInterceptor(i, this, ictx, invoker, mexDao))
                return false;

        return true;
    }

    protected boolean processInterceptor(
            MessageExchangeInterceptor i, 
            MyRoleMessageExchangeImpl mex, 
            InterceptorContext ictx,
            InterceptorInvoker invoker,
            MessageExchangeDAO mexdao) {
        __log.debug(invoker + "--> interceptor " + i);
        try {
            invoker.invoke(i, mex, ictx);
        } catch (FaultMessageExchangeException fme) {
            __log.debug("interceptor " + i + " caused invoke on " + this + " to be aborted with FAULT " + fme.getFaultName());
            MexDaoUtil.setFaulted(mexdao, fme.getFaultName(), fme.getFaultData() == null ? null : fme.getFaultData().getMessage());
            return false;
        } catch (AbortMessageExchangeException ame) {
            __log.debug("interceptor " + i + " cause invoke on " + this + " to be aborted with FAILURE: " + ame.getMessage());
            MexDaoUtil.setFailed(mexdao, FailureType.ABORTED, __msgs.msgInterceptorAborted(mex.getMessageExchangeId(), i
                    .toString(), ame.getMessage()));
            return false;
        }
        return true;
    }

   
    
    protected abstract void onAsyncAck(MessageExchangeDAO mexdao);
    
    
    protected void finalize() {
        _process.unregisterMyRoleMex(this);
    }
    
    

 
}
