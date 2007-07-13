package org.apache.ode.bpel.engine;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.iapi.InvocationStyle;

/**
 * For invoking the engine using ASYNC style.
 * 
 * @author Maciej Szefler
 * 
 */
public class AsyncMyRoleMessageExchangeImpl extends MyRoleMessageExchangeImpl {
    private static final Log __log = LogFactory.getLog(ReliableMyRoleMessageExchangeImpl.class);

    ResponseFuture _future;
    
    public AsyncMyRoleMessageExchangeImpl(BpelProcess process, String mexId) {
        super(process, mexId);
    }

    public Future<Status> invokeAsync() {
        if (_future != null)
            return _future;
        
        _future = new ResponseFuture();

        if (_process.isInMemory()) {
            _process.invokeProcess(_process.getInMemMexDAO(_mexId));
        } else {
            scheduleInvoke(_process);
        }
      
        if (getOperation().getOutput() == null) {
            _future.done(getStatus());
        }

        return _future;

    }

    protected void onMessageExchangeComplete(MessageExchangeDAO mexdao) {
        load(mexdao);
        _future.done(getStatus());         
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
