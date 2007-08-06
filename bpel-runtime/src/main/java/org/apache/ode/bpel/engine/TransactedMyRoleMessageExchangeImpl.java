package org.apache.ode.bpel.engine;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.o.OPartnerLink;

/**
 * Transacted my-role message exchange.
 * 
 * TODO: IMPLEMENT!
 * 
 * @author Maciej Szefler
 * 
 */
public class TransactedMyRoleMessageExchangeImpl extends MyRoleMessageExchangeImpl {

    public TransactedMyRoleMessageExchangeImpl(BpelProcess process, String mexId, OPartnerLink oplink, Operation operation, QName callee) {
        super(process, mexId, oplink, operation, callee);
    }

    @Override
    public Status invokeTransacted() throws BpelEngineException {
        assertTransaction();
       
        _process.invokeProcess(getDAO());
        if (MessageExchange.Status.valueOf(getDAO().getStatus()) != Status.ACK)
            throw new BpelEngineException("Transactional invoke on process did not yield a response.");
        return Status.valueOf(getDAO().getStatus());
        
    }

    @Override
    public InvocationStyle getInvocationStyle() {
        return InvocationStyle.TRANSACTED;
    }

}
