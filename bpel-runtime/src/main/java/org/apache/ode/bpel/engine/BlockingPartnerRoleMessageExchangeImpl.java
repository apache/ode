package org.apache.ode.bpel.engine;

import javax.wsdl.Operation;
import javax.wsdl.PortType;

import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;

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

    BlockingPartnerRoleMessageExchangeImpl(BpelEngineImpl engine, String mexId, PortType portType, Operation operation, boolean inMem, EndpointReference epr, EndpointReference myRoleEPR, PartnerRoleChannel channel) {
        super(engine, mexId, portType, operation, inMem, epr, myRoleEPR, channel);
    }

}
