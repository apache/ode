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
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.intercept.AbortMessageExchangeException;
import org.apache.ode.bpel.intercept.FaultMessageExchangeException;
import org.apache.ode.bpel.intercept.InterceptorInvoker;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor.InterceptorContext;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class MyRoleMessageExchangeImpl extends MessageExchangeImpl implements MyRoleMessageExchange {

    private static final Log __log = LogFactory.getLog(MyRoleMessageExchangeImpl.class);
    public static final int TIMEOUT = 2 * 60 * 1000;

    private static Map<String, ResponseFuture> _waitingFutures =
            new ConcurrentHashMap<String, ResponseFuture>();


    public MyRoleMessageExchangeImpl() {
        super(engine, mexdao);
    }

    public CorrelationStatus getCorrelationStatus() {
        return CorrelationStatus.valueOf(getDAO().getCorrelationStatus());
    }

    void setCorrelationStatus(CorrelationStatus status) {
        getDAO().setCorrelationStatus(status.toString());
    }

    /**
     * Process the message-exchange interceptors.
     * 
     * @param mex
     *            message exchange
     * @return <code>true</code> if execution should continue,
     *         <code>false</code> otherwise
     */
    private boolean processInterceptors(MyRoleMessageExchangeImpl mex, InterceptorInvoker invoker) {
        InterceptorContextImpl ictx = new InterceptorContextImpl(_engine._contexts.dao.getConnection(), 
                mex._dao.getProcess(), null);

        for (MessageExchangeInterceptor i : _engine.getGlobalInterceptors())
            if (!processInterceptor(i, mex, ictx, invoker))
                return false;

        return true;
    }

    boolean processInterceptor(MessageExchangeInterceptor i, MyRoleMessageExchangeImpl mex, InterceptorContext ictx,
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

    public Future<MessageExchange.Status> invoke(Message request) {
        if (request == null) {
            String errmsg = "Must pass non-null message to invoke()!";
            __log.fatal(errmsg);
            throw new NullPointerException(errmsg);
        }

        _dao.setRequest(((MessageImpl) request)._dao);
        setStatus(MessageExchange.Status.REQUEST);

        if (!processInterceptors(this, InterceptorInvoker.__onBpelServerInvoked)) {
            throw new BpelEngineException("Intercepted.");
        }

        BpelProcess target = _engine.route(getDAO().getCallee(), request);

        if (__log.isDebugEnabled())
            __log.debug("invoke() EPR= " + _epr + " ==> " + target);

        
        ResponseFuture future = new ResponseFuture();
        
        if (target == null) {
            if (__log.isWarnEnabled())
                __log.warn(__msgs.msgUnknownEPR("" + _epr));

            setCorrelationStatus(MyRoleMessageExchange.CorrelationStatus.UKNOWN_ENDPOINT);
            setFailure(MessageExchange.FailureType.UNKNOWN_ENDPOINT, null, null);
            future.done(_lastStatus);
        } else {
            // Schedule a new job for invocation
            WorkEvent we = new WorkEvent();
            we.setType(WorkEvent.Type.INVOKE_INTERNAL);
            if (target.isInMemory()) we.setInMem(true);
            we.setProcessId(target.getPID());
            we.setMexId(getDAO().getMessageExchangeId());

            setStatus(Status.ASYNC);

            if (getOperation().getOutput() != null) {
                _waitingFutures.put(getMessageExchangeId(), future);
            } else {
                future.done(_lastStatus);
            }


            if (target.isInMemory())
                _engine._contexts.scheduler.scheduleVolatileJob(true, we.getDetail());
            else
                _engine._contexts.scheduler.schedulePersistedJob(we.getDetail(), null);

        }

        return future;
    }

    public void complete() {
    }

    public QName getServiceName() {
        return getDAO().getCallee();
    }

    public void setClientId(String clientKey) {
        getDAO().setCorrelationId(clientKey);
    }

    public String getClientId() {
        return getDAO().getCorrelationId();
    }

    public String toString() {
        try {
            return "{MyRoleMex#" + getMessageExchangeId() + " [Client " + getClientId() + "] calling " + getServiceName() + "."
                    + getOperationName() + "(...)}";
        } catch (Throwable t) {
            return "{MyRoleMex#???}";
        }
    }

    public boolean isAsynchronous() {
        return true;
    }


    protected void responseReceived() {
        final String mexid = getMessageExchangeId();
        _engine._contexts.scheduler.registerSynchronizer(new Scheduler.Synchronizer() {
            public void afterCompletion(boolean success) {
                __log.debug("Received myrole mex response callback");
                ResponseFuture callback = _waitingFutures.remove(mexid);
                callback.done(_lastStatus);
            }
            public void beforeCompletion() {
            }
        });
    }
    
    private static class ResponseFuture implements Future<Status> {
        private Status _status;

        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }
        
        public Status get() throws InterruptedException, ExecutionException {
            try {
                return get(0, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                // If it's thrown it's definitely a bug
                throw new RuntimeException(e);
            }
        }
        
        public Status get(long timeout, TimeUnit unit) 
            throws InterruptedException, ExecutionException, TimeoutException {
            
            
            synchronized(this) {
                if (_status != null)
                    return _status;
                
                while (_status == null) {
                    this.wait(TimeUnit.MILLISECONDS.convert(timeout, unit));
                }
    
                if (_status == null)
                    throw new TimeoutException();
                
                return _status;
            }
        }

        public boolean isCancelled() {
            return false;
        }
        
        public boolean isDone() {
            return _status != null;
        }
        
        void done(Status status) {
            synchronized(this) {
                _status = status;
                this.notifyAll();
            }
        }
    }

}
