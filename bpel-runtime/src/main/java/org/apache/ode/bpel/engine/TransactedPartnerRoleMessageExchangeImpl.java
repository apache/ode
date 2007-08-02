package org.apache.ode.bpel.engine;

import javax.wsdl.Operation;

import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.o.OPartnerLink;


/**
 * 
 * 
 * @author Maciej Szefler
 *
 */
public class TransactedPartnerRoleMessageExchangeImpl extends PartnerRoleMessageExchangeImpl {

    TransactedPartnerRoleMessageExchangeImpl(BpelProcess process, String mexId, OPartnerLink oplink, Operation operation,EndpointReference epr, EndpointReference myRoleEPR, PartnerRoleChannel channel) {
        super(process, mexId, oplink, operation,  epr, myRoleEPR, channel);
    }
    
    
    /**
     * The criteria for issuing a replyXXX call on TRANSACTED message exchanges is that the replyXXX must come while the
     * engine is blocked in an  
     * {@link MessageExchangeContext#invokePartnerBlocking(org.apache.ode.bpel.iapi.PartnerRoleMessageExchange)}. 
     * method, AND the call must come from the engine thread. 
     */
    @Override
    protected void checkReplyContextOk() {
        if (!_blocked)
            throw new BpelEngineException("replyXXX operation attempted outside of BLOCKING region!");
        if (!_ownerThread.get())
            throw new BpelEngineException("replyXXX operation attempted from foreign thread!");
        
        assert _contexts.isTransacted() : "Internal Error: owner thread must be transactional!?!?!!?"; 
    }


    @Override
    public InvocationStyle getInvocationStyle() {
        return InvocationStyle.TRANSACTED;
    }



}
