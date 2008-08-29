package org.apache.ode.bpel.engine;

import javax.wsdl.Operation;

import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.rapi.PartnerLinkModel;


/**
 * 
 * 
 * @author Maciej Szefler
 *
 */
public class TransactedPartnerRoleMessageExchangeImpl extends PartnerRoleMessageExchangeImpl {

    TransactedPartnerRoleMessageExchangeImpl(ODEProcess process, long iid, String mexId, PartnerLinkModel oplink,Operation operation, EndpointReference epr, EndpointReference myRoleEPR, PartnerRoleChannel channel) {
        super(process, iid, mexId, oplink,  operation, epr, myRoleEPR, channel);
    }
    
    
    /**
     * The criteria for issuing a replyXXX call on TRANSACTED message exchanges is that the replyXXX must come while the
     * engine is blocked in an  
     * {@link MessageExchangeContext#invokePartnerBlocking(org.apache.ode.bpel.iapi.PartnerRoleMessageExchange)}. 
     * method, AND the call must come from the engine thread. 
     */
    @Override
    protected void checkReplyContextOk() {
        if (_state != State.INVOKE_XXX)
            throw new BpelEngineException("replyXXX operation attempted outside of transacted region!");
        
        assert _contexts.isTransacted() : "Internal Error: owner thread must be transactional!?!?!!?"; 
    }


    @Override
    public InvocationStyle getInvocationStyle() {
        return InvocationStyle.TRANSACTED;
    }


    @Override
    protected void asyncACK() {
        throw new IllegalStateException("Async responses not supported for transaction invocations.");
        
    }



}
