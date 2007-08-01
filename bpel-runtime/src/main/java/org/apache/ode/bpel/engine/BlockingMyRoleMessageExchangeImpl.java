package org.apache.ode.bpel.engine;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.o.OPartnerLink;

/**
 * Non-transaction blocking MyRole message-exchange implementation.
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 */
public class BlockingMyRoleMessageExchangeImpl extends AsyncMyRoleMessageExchangeImpl {
    Future<Status> _future;
    boolean _done = false;
    
    public BlockingMyRoleMessageExchangeImpl(BpelProcess process, String mexId, OPartnerLink oplink, Operation operation, QName callee) {
        super(process, mexId, oplink, operation, callee);
    }

    @Override
    public Future<Status> invokeAsync() {
        throw new BpelEngineException("Invalid invocation style, use invokeBlocking() instead.");
    }

    @Override
    public Status invokeBlocking() throws BpelEngineException, TimeoutException {
        if (_done) 
            return getStatus();

        Future<Status> future = _future != null ? _future : super.invokeAsync();
        
        try {
            future.get(Math.max(_timeout,1), TimeUnit.MILLISECONDS);
            _done = true;
            return getStatus();
        } catch (InterruptedException e) {
            throw new BpelEngineException(e);
        } catch (ExecutionException e) {
            throw new BpelEngineException(e.getCause());
        } 
    }    
    
    @Override
    public InvocationStyle getInvocationStyle() {
        return InvocationStyle.BLOCKING;
    }
}
