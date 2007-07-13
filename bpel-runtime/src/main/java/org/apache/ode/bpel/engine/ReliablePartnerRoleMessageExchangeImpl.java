package org.apache.ode.bpel.engine;

import javax.wsdl.Operation;
import javax.wsdl.PortType;

import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;

public class ReliablePartnerRoleMessageExchangeImpl extends PartnerRoleMessageExchangeImpl {

    public ReliablePartnerRoleMessageExchangeImpl(BpelProcess process, String mexId, PortType ptype, Operation op, EndpointReference epr, EndpointReference myRoleEPR, PartnerRoleChannel partnerRoleChannel) {
        super(process, mexId, ptype, op, epr, myRoleEPR, partnerRoleChannel);
    }

    @Override
    protected void resumeInstance() {
        // TODO Auto-generated method stub
        
    }
}
