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
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.o.OPartnerLink;

/**
 * For invoking the engine using ASYNC style.
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 * 
 */
public class AsyncMyRoleMessageExchangeImpl extends MyRoleMessageExchangeImpl {
    private static final Log __log = LogFactory.getLog(ReliableMyRoleMessageExchangeImpl.class);

    ResponseFuture _future;
    
    public AsyncMyRoleMessageExchangeImpl(BpelProcess process, String mexId, OPartnerLink oplink, Operation operation, QName callee) {
        super(process, mexId, oplink, operation, callee);
    }

    /**
     * Override the setStatus(...) to notify our future when there is a response/failure.
     */
    protected void setStatus(Status status) {
        Status old = getStatus();
        super.setStatus(status);
        if (_future != null) {
            if (getMessageExchangePattern() == MessageExchangePattern.REQUEST_ONLY) {
                if (old == Status.REQUEST && old != status)
                    _future.done(status);
            } else /* two-way */ {
                if ((old == Status.ASYNC || old == Status.REQUEST) && status != Status.ASYNC)
                    _future.done(status);
            }
        }
    }

    public Future<Status> invokeAsync() {
        if (_future != null)
            return _future;
        
        _future = new ResponseFuture();
        _process.enqueueTransaction(new Callable<Void>() {

            public Void call() throws Exception {
                MessageExchangeDAO dao = _process.createMessageExchange(getMessageExchangeId(), MessageExchangeDAO.DIR_PARTNER_INVOKES_MYROLE);
                save(dao);
                if (_process.isInMemory()) 
                    _process.invokeProcess(dao);
                else
                    scheduleInvoke();
                return null;
            }
            
        });
      
        
        return _future;

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

    @Override
    public InvocationStyle getInvocationStyle() {
        return InvocationStyle.ASYNC;
    }

}
