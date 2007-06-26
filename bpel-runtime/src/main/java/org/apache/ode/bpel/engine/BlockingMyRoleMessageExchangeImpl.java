package org.apache.ode.bpel.engine;

import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.MessageExchange.Status;

public class BlockingMyRoleMessageExchangeImpl extends AsyncMyRoleMessageExchangeImpl {
    Future<Status> _future;
    boolean _done = false;
    
    public BlockingMyRoleMessageExchangeImpl(BpelEngineImpl engine, String mexId) {
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
        if (_future != null)
            _future.get();
        Future<Status> future = super.invokeAsync();
        
        future.get(timeout, unit)
    }

    
}
