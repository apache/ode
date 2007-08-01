package org.apache.ode.bpel.engine;

import javax.wsdl.Operation;

import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.engine.MessageExchangeImpl.InDbAction;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.o.OPartnerLink;

public class ReliablePartnerRoleMessageExchangeImpl extends PartnerRoleMessageExchangeImpl {

    public ReliablePartnerRoleMessageExchangeImpl(BpelProcess process, String mexId, OPartnerLink oplink, Operation op, EndpointReference epr, EndpointReference myRoleEPR, PartnerRoleChannel partnerRoleChannel) {
        super(process, mexId, oplink, op, epr, myRoleEPR, partnerRoleChannel);
    }

    
    @Override
    protected void checkReplyContextOk() {
        super.checkReplyContextOk();
        
        if (!_contexts.isTransacted())
            throw new BpelEngineException("Cannot replyXXX from non-transaction context!");
    }


    @Override
    public void replyAsync(String foreignKey) {
        if (!_blocked)
            throw new BpelEngineException("Invalid context for replyAsync(); can only be called during MessageExchangeContext call. ");
        checkReplyContextOk();
        setStatus(Status.ASYNC);
        _foreignKey = foreignKey;
    }


    @Override
    protected void resumeInstance() {
        // TODO Auto-generated method stub
        assert _contexts.isTransacted() : "checkReplyContext() should have prevented us from getting here.";
        assert !_process.isInMemory() : "resumeInstance() for reliable in-mem processes makes no sense.";

        final WorkEvent we = generateInvokeResponseWorkEvent();

        save(getDAO());
        _contexts.scheduler.schedulePersistedJob(we.getDetail(), null);
    }

    @Override
    public InvocationStyle getInvocationStyle() {
        return InvocationStyle.RELIABLE;
    }
    
    
}
