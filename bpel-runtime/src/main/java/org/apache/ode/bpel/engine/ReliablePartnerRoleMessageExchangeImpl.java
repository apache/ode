package org.apache.ode.bpel.engine;

import javax.wsdl.Operation;

import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.rapi.PartnerLinkModel;

public class ReliablePartnerRoleMessageExchangeImpl extends PartnerRoleMessageExchangeImpl {

    public ReliablePartnerRoleMessageExchangeImpl(ODEProcess process, long iid, String mexId, PartnerLinkModel oplink, Operation op,
            EndpointReference epr, EndpointReference myRoleEPR, PartnerRoleChannel partnerRoleChannel) {
        super(process, iid, mexId, oplink, op, epr, myRoleEPR, partnerRoleChannel);
    }

    @Override
    protected void checkReplyContextOk() {
        super.checkReplyContextOk();

        if (!_contexts.isTransacted())
            throw new BpelEngineException("Cannot replyXXX from non-transaction context!");
    }

    @Override
    public void replyAsync(String foreignKey) {
        _accessLock.lock();
        try {
            checkReplyContextOk();

            if (_state != State.INVOKE_XXX)
                throw new IllegalStateException(
                    "Invalid context for replyAsync(); can only be called during MessageExchangeContext call. ");
            
            _foreignKey = foreignKey;
        } finally {
            _accessLock.unlock();
        }
    }

    @Override
    protected void asyncACK() {
        // TODO Auto-generated method stub
        assert _contexts.isTransacted() : "checkReplyContext() should have prevented us from getting here.";
        assert !_process.isInMemory() : "resumeInstance() for reliable in-mem processes makes no sense.";

        MessageExchangeDAO mexdao = getDAO();
        final WorkEvent we = generatePartnerResponseWorkEvent(mexdao);
        save(mexdao);
        _contexts.scheduler.schedulePersistedJob(we.getDetail(), null);
    }


    @Override
    public InvocationStyle getInvocationStyle() {
        return InvocationStyle.RELIABLE;
    }

    private WorkEvent generatePartnerResponseWorkEvent(MessageExchangeDAO mexdao) {
        WorkEvent we = new WorkEvent();
        we.setProcessId(_process.getPID());
        we.setChannel(mexdao.getChannel());
        we.setIID(_iid);
        we.setMexId(_mexId);
        we.setType(WorkEvent.Type.PARTNER_RESPONSE);
        return we;
    }

}
