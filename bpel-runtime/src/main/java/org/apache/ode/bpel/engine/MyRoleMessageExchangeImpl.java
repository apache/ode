package org.apache.ode.bpel.engine;

import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.intercept.AbortMessageExchangeException;
import org.apache.ode.bpel.intercept.FaultMessageExchangeException;
import org.apache.ode.bpel.intercept.InterceptorInvoker;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor.InterceptorContext;

class MyRoleMessageExchangeImpl extends MessageExchangeImpl implements MyRoleMessageExchange {

    private static final Log __log = LogFactory.getLog(MyRoleMessageExchangeImpl.class);
    
    protected CorrelationStatus _cstatus;

    protected String _clientId;

    protected QName _callee;

    public MyRoleMessageExchangeImpl(BpelServerImpl engine, String mexId) {
        super(engine, mexId);
    }

    public CorrelationStatus getCorrelationStatus() {
        return _cstatus;
    }

    @Override
    void load(MessageExchangeDAO dao) {
        super.load(dao);
        if (_cstatus == null)
            _cstatus = CorrelationStatus.valueOf(dao.getCorrelationStatus());
        if (_clientId == null)
            _clientId = dao.getCorrelationId();
        if (_callee == null)
            _callee = dao.getCallee();
    }

    @Override
    public void save(MessageExchangeDAO dao) {
        super.save(dao);
        dao.setCorrelationStatus(_cstatus.toString());
        dao.setCorrelationId(_clientId);
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
            return "{MyRoleMex#" + _mexId + " [Client " + _clientId + "] calling " + _callee + "." + _opname + "(...)}";
        } catch (Throwable t) {
            return "{MyRoleMex#???}";
        }
    }

    public void complete() {
        // TODO Auto-generated method stub

    }

    protected void scheduleInvoke(BpelProcess target) {
        // Schedule a new job for invocation
        final WorkEvent we = new WorkEvent();
        we.setType(WorkEvent.Type.INVOKE_INTERNAL);
        we.setInMem(target.isInMemory());
        we.setProcessId(target.getPID());
        we.setMexId(_mexId);

        // Schedule a timeout 
        final WorkEvent we1 = new WorkEvent();
        we1.setType(WorkEvent.Type.INVOKE_TIMEOUT);
        we1.setInMem(target.isInMemory());
        we1.setProcessId(target.getPID());
        we1.setMexId(_mexId);
        
        setStatus(Status.ASYNC);
        doInTX(new InDbAction<Void>() {

            public Void call(MessageExchangeDAO mexdao) {
                _server._contexts.scheduler.schedulePersistedJob(we.getDetail(), null);
                _server._contexts.scheduler.schedulePersistedJob(we1.getDetail(), null);
                return null;
            }

        });

    }
    

    /**
     * Process the message-exchange interceptors.
     * 
     * @param mex
     *            message exchange
     * @return <code>true</code> if execution should continue, <code>false</code> otherwise
     */
    protected boolean processInterceptors(InterceptorInvoker invoker, MessageExchangeDAO mexDao) {
        InterceptorContextImpl ictx = new InterceptorContextImpl(_server._contexts.dao.getConnection(), mexDao.getProcess(), null);

        for (MessageExchangeInterceptor i : _server.getGlobalInterceptors())
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
            mex.setFault(fme.getFaultName(), fme.getFaultData());
            return false;
        } catch (AbortMessageExchangeException ame) {
            __log.debug("interceptor " + i + " cause invoke on " + this + " to be aborted with FAILURE: " + ame.getMessage());
            mex.setFailure(MessageExchange.FailureType.ABORTED, __msgs.msgInterceptorAborted(mex.getMessageExchangeId(), i
                    .toString(), ame.getMessage()), null);
            return false;
        }
        return true;
    }


    /**
     * Callback.
     * 
     * @param mexdao
     */
    protected void onMessageExchangeComplete(MessageExchangeDAO mexdao) {
    }
}
