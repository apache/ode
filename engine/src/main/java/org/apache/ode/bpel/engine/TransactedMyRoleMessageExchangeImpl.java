package org.apache.ode.bpel.engine;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.rapi.PartnerLinkModel;

/**
 * Transacted my-role message exchange.
 * 

 * @author Maciej Szefler
 * 
 */
public class TransactedMyRoleMessageExchangeImpl extends MyRoleMessageExchangeImpl {

    public TransactedMyRoleMessageExchangeImpl(ODEProcess process, String mexId, PartnerLinkModel mplink, Operation operation, QName callee) {
        super(process, mexId, mplink, operation, callee);
    }

    @Override
    public Status invokeTransacted() throws BpelEngineException {
        assertTransaction();
       
        
        MessageExchangeDAO mexdao = doInvoke();
        if (mexdao.getStatus() != Status.ACK) {
            throw new BpelEngineException("Transactional invoke on process did not yield a response.");
        }

        return mexdao.getStatus();
        
    }

    @Override
    public InvocationStyle getInvocationStyle() {
        return InvocationStyle.TRANSACTED;
    }

    @Override
    protected void onAsyncAck(MessageExchangeDAO mexdao) {
        assert false;
    }

}
