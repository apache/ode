package org.apache.ode.bpel.engine;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.ode.bpel.iapi.BpelEngineException;

/**
 * Non-transaction blocking MyRole message-exchange implementation.
 * 
 * @author Maciej Szefler 
 */
public class BlockingMyRoleMessageExchangeImpl extends AsyncMyRoleMessageExchangeImpl {
    Future<Status> _future;
    boolean _done = false;
    
    public BlockingMyRoleMessageExchangeImpl(BpelServerImpl engine, String mexId) {
        super(engine, mexId);
    }

    @Override
    public Future<Status> invokeAsync() {
        throw new BpelEngineException("Invalid invocation style, use invokeBlocking() instead.");
    }

    @Override
    public Status invokeBlocking() throws BpelEngineException, TimeoutException {
        if (_done) 
            return _status;

        Future<Status> future = _future != null ? _future : super.invokeAsync();
        
        try {
            future.get(Math.max(System.currentTimeMillis()-_timeout.getTime(),1), TimeUnit.MILLISECONDS);
            _done = true;
            return _status;
        } catch (InterruptedException e) {
            throw new BpelEngineException(e);
        } catch (ExecutionException e) {
            throw new BpelEngineException(e.getCause());
        } 
    }    
}
