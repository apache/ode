package org.apache.ode.bpel.engine;

import javax.wsdl.Operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.rapi.PartnerLinkModel;

/**
 * Implementation of the {@link PartnerRoleMessageExchange} interface that is passed to the IL when the UNRELIABLE invocation style
 * is used (see {@link InvocationStyle#UNRELIABLE}). The basic idea here is that with this style, the IL performs the operation
 * outside of a transactional context. It can either finish it right away (BLOCK) or indicate that the response will be provided
 * later (replyASYNC).
 * @author Maciej Szefler <mszefler at gmail dot com>
 * 
 */
public class UnreliablePartnerRoleMessageExchangeImpl extends PartnerRoleMessageExchangeImpl {
    private static final Log __log = LogFactory.getLog(UnreliablePartnerRoleMessageExchangeImpl.class);
    boolean _asyncReply;

    UnreliablePartnerRoleMessageExchangeImpl(ODEProcess process, long iid, String mexId, PartnerLinkModel oplink, Operation operation,
            EndpointReference epr, EndpointReference myRoleEPR, PartnerRoleChannel channel) {
        super(process, iid, mexId, oplink, operation, epr, myRoleEPR, channel);
    }

    @Override
    public InvocationStyle getInvocationStyle() {
        return InvocationStyle.UNRELIABLE;
    }

    @Override
    protected void asyncACK() {
        assert !_contexts.isTransacted() : "checkReplyContext() should have prevented us from getting here.";
        assert !_process.isInMemory() : "resumeInstance() for in-mem processes makes no sense.";

        if (__log.isDebugEnabled()) {
            __log.debug("asyncResponseReceived: for IID " + getIID() );
        }

        _process.enqueueInstanceTransaction(getIID(), new Runnable() {
            public void run() {
                MessageExchangeDAO dao = getDAO();
                save(dao);
                _process.executeContinueInstancePartnerRoleResponseReceived(dao);
            }

        });
    }

    @Override
    protected void checkReplyContextOk() {
        super.checkReplyContextOk();

        // Prevent user from attempting the replyXXXX calls while a transaction is active.
        if (_contexts.isTransacted())
            throw new BpelEngineException("Cannot reply to UNRELIABLE style invocation from a transactional context!");

    }

    @Override
    public void replyAsync(String foreignKey) {
        if (__log.isDebugEnabled())
            __log.debug("replyAsync mex=" + _mexId);

        _accessLock.lock();
        try {
            checkReplyContextOk();

            if (_state != State.INVOKE_XXX)
                throw new IllegalStateException(
                        "Invalid context for replyAsync(); can only be called during MessageExchangeContext call. ");

            _asyncReply = true;
            _foreignKey = foreignKey;
        } finally {
            _accessLock.unlock();
        }
    }

    
 
}
