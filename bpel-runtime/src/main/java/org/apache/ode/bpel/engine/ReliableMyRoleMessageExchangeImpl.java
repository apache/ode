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
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange.CorrelationStatus;
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

/**
 * Provides an implementation of the {@link MyRoleMessageExchange} inteface for interactions performed in the
 * {@link InvocationStyle#RELIABLE} style.
 * 
 * @author Maciej Szefler
 */
class ReliableMyRoleMessageExchangeImpl extends MessageExchangeImpl implements MyRoleMessageExchange {

    private static final Log __log = LogFactory.getLog(ReliableMyRoleMessageExchangeImpl.class);

    public static final int TIMEOUT = 2 * 60 * 1000;

    private static Map<String, ResponseFuture> _waitingFutures = new ConcurrentHashMap<String, ResponseFuture>();

    private CorrelationStatus _cstatus;

    private String _clientId;

    public ReliableMyRoleMessageExchangeImpl(BpelEngineImpl engine, String mexId) {
        super(engine, mexId);

        // RELIABLE means we are bound to a transaction
        _txflag = true;
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
    }

    @Override
    public void save(MessageExchangeDAO dao) {
        super.save(dao);
        dao.setCorrelationStatus(_cstatus.toString());
        dao.setCorrelationId(_clientId);
    }

    /**
     * Process the message-exchange interceptors.
     * 
     * @param mex
     *            message exchange
     * @return <code>true</code> if execution should continue, <code>false</code> otherwise
     */
    private boolean processInterceptors(InterceptorInvoker invoker, MessageExchangeDAO mexDao) {
        InterceptorContextImpl ictx = new InterceptorContextImpl(_engine._contexts.dao.getConnection(), mexDao.getProcess(), null);

        for (MessageExchangeInterceptor i : _engine.getGlobalInterceptors())
            if (!processInterceptor(i, this, ictx, invoker))
                return false;

        return true;
    }

    boolean processInterceptor(MessageExchangeInterceptor i, ReliableMyRoleMessageExchangeImpl mex, InterceptorContext ictx,
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

    public Future<MessageExchange.Status> invoke(final Message request) {
        if (request == null) {
            String errmsg = "Must pass non-null message to invoke()!";
            throw new NullPointerException(errmsg);
        }

        // For reliable, we MUST HAVE A TRANSACTION!
        assertTransaction();

        BpelProcess target = _engine.route(_callee, request);
        if (target == null) {
            if (__log.isWarnEnabled())
                __log.warn(__msgs.msgUnknownEPR("" + _epr));

            ResponseFuture future = new ResponseFuture();

            _cstatus = MyRoleMessageExchange.CorrelationStatus.UKNOWN_ENDPOINT;
            setFailure(MessageExchange.FailureType.UNKNOWN_ENDPOINT, null, null);
            future.done(_status);

            return future;
        }

        doInDb(new InDbAction<Void>() {

            public Void call(MessageExchangeDAO mexdao) {
                // TODO: perhaps we should check if already backed by DB?
                MessageDAO msgDao = mexdao.createMessage(request.getType());
                msgDao.setData(request.getMessage());
                setStatus(MessageExchange.Status.REQUEST);

                if (!processInterceptors(this, InterceptorInvoker.__onBpelServerInvoked)) {
                    throw new BpelEngineException("Intercepted.");
                }

                if (__log.isDebugEnabled())
                    __log.debug("invoke() EPR= " + _epr + " ==> " + target);

            }

        });

        {
            // Schedule a new job for invocation
            WorkEvent we = new WorkEvent();
            we.setType(WorkEvent.Type.INVOKE_INTERNAL);
            if (target.isInMemory())
                we.setInMem(true);
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

    public String getClientId() {
        return _clientId;
    }

    public String toString() {
        try {
            return "{MyRoleMex#" + getMessageExchangeId() + " [Client " + getClientId() + "] calling " + getServiceName() + "."
                    + getOperationName() + "(...)}";
        } catch (Throwable t) {
            return "{MyRoleMex#???}";
        }
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

        public Status get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {

            synchronized (this) {
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
            synchronized (this) {
                _status = status;
                this.notifyAll();
            }
        }
    }

}
