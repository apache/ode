package org.apache.ode.bpel.engine;

import javax.wsdl.Operation;
import javax.wsdl.PortType;

import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;

/**
 * Implementation of the {@link PartnerRoleMessageExchange} interface that is passed to the IL when the 
 * BLOCKING invocation style is used (see {@link InvocationStyle#BLOCKING}). The basic idea here is that 
 * with this style, the IL performs the operation while blocking in the 
 * {@link MessageExchangeContext#invokePartner(org.apache.ode.bpel.iapi.PartnerRoleMessageExchange)} method.
 *
 * This InvocationStyle makes this class rather trivial. 
 *  
 * @author Maciej Szefler
 *
 */
public class BlockingPartnerRoleMessageExchangeImpl extends PartnerRoleMessageExchangeImpl {

    BlockingPartnerRoleMessageExchangeImpl(BpelProcess process, String mexId, PortType portType, Operation operation, EndpointReference epr, EndpointReference myRoleEPR, PartnerRoleChannel channel) {
        super(process, mexId, portType, operation, epr, myRoleEPR, channel);
    }

    /**
     * The criteria for issuing a replyXXX call on BLOCKING message exchanges is that the replyXXX must come while the
     * engine is blocked in an  
     * {@link MessageExchangeContext#invokePartnerBlocking(org.apache.ode.bpel.iapi.PartnerRoleMessageExchange)}. 
     * method. 
     */
    @Override
    protected void checkReplyContextOk() {
        if (!_blocked)
            throw new BpelEngineException("replyXXX operation attempted outside of BLOCKING region!");
    }

    @Override
    public InvocationStyle getInvocationStyle() {
        return InvocationStyle.BLOCKING;
    }


    
}

