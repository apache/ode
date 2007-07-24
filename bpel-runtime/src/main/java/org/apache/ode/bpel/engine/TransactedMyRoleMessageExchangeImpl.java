package org.apache.ode.bpel.engine;

import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.Status;

/**
 * Transacted my-role message exchange.
 * 
 * TODO: IMPLEMENT!
 * 
 * @author Maciej Szefler
 * 
 */
public class TransactedMyRoleMessageExchangeImpl extends MyRoleMessageExchangeImpl {

    public TransactedMyRoleMessageExchangeImpl(BpelProcess target, String mexId) {
        super(target, mexId);
    }

    @Override
    public Status invokeTransacted() throws BpelEngineException {
        assertTransaction();
       
        _process.invokeProcess(getDAO());
        if (MessageExchange.Status.valueOf(getDAO().getStatus()) != Status.RESPONSE)
            throw new BpelEngineException("Transactional invoke on process did not yield a response.");
        return Status.valueOf(getDAO().getStatus());
        
    }

    @Override
    public InvocationStyle getInvocationStyle() {
        return InvocationStyle.TRANSACTED;
    }

}
