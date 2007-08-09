package org.apache.ode.bpel.engine;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.o.OPartnerLink;

/**
 * For invoking the engine using UNRELIABLE style.
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 * 
 */
public class UnreliableMyRoleMessageExchangeImpl extends MyRoleMessageExchangeImpl {
    private static final Log __log = LogFactory.getLog(ReliableMyRoleMessageExchangeImpl.class);

    boolean _done = false;

    ResponseFuture _future;

    public UnreliableMyRoleMessageExchangeImpl(BpelProcess process, String mexId, OPartnerLink oplink, Operation operation,
            QName callee) {
        super(process, mexId, oplink, operation, callee);
    }

    public Future<Status> invokeAsync() {
        if (_future != null)
            return _future;

        if (_request == null)
            throw new IllegalStateException("Must call setRequest(...)!");

        _future = new ResponseFuture();
        _process.enqueueTransaction(new Callable<Void>() {

            public Void call() throws Exception {
                MessageExchangeDAO dao = doInvoke();

                if (dao.getStatus() == Status.ACK) {
                    // not really an async ack, same idea.
                    onAsyncAck(dao);
                }

                return null;
            }

        });

        return _future;

    }

    @Override
    public InvocationStyle getInvocationStyle() {
        return InvocationStyle.UNRELIABLE;
    }

    @Override
    public Status invokeBlocking() throws BpelEngineException, TimeoutException {

        if (_done)
            return getStatus();

        Future<Status> future = _future != null ? _future : invokeAsync();

        try {
            future.get(Math.max(_timeout, 1), TimeUnit.MILLISECONDS);
            _done = true;
            return getStatus();
        } catch (InterruptedException e) {
            throw new BpelEngineException(e);
        } catch (ExecutionException e) {
            throw new BpelEngineException(e.getCause());
        }
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

                this.wait(TimeUnit.MILLISECONDS.convert(timeout, unit));

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

    @Override
    protected void onAsyncAck(MessageExchangeDAO mexdao) {
        final MemBackedMessageImpl response;
        final QName fault = mexdao.getFault();
        final FailureType failureType = mexdao.getFailureType();
        final AckType ackType = mexdao.getAckType();
        final String explanation = mexdao.getFaultExplanation();
        switch (mexdao.getAckType()) {
        case RESPONSE:
        case FAULT:
            response = new MemBackedMessageImpl(mexdao.getResponse().getData(), mexdao.getResponse().getType(), false);
            break;
        default:
            response = null;
        }

        // Lets be careful, the TX can still rollback!
        _process.scheduleRunnable(new Runnable() {
            public void run() {
                _response = response;
                _fault = fault;
                _failureType = failureType;
                ack(ackType);
                _future.done(Status.ACK);

            }

        });
    }

}
