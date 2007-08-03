package org.apache.ode.bpel.engine;

import javax.wsdl.Operation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.engine.MessageExchangeImpl.InDbAction;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.o.OPartnerLink;

/**
 * Implementation of the {@link PartnerRoleMessageExchange} interface that is passed to the IL when the 
 * UNRELIABLE invocation style is used (see {@link InvocationStyle#UNRELIABLE}). The basic idea here is 
 * that with this style, the IL performs the operation outside of a transactional context. It can either
 * finish it right away (BLOCK) or indicate that the response will be provided later (replyASYNC).  
 *
 *  
 *  TODO: serious synchronization issues in this class.
 *  
 * @author Maciej Szefler <mszefler at gmail dot com>
 *
 */
public class UnreliablePartnerRoleMessageExchangeImpl extends PartnerRoleMessageExchangeImpl {
    private static final Log __log = LogFactory.getLog(UnreliablePartnerRoleMessageExchangeImpl.class);
    

    UnreliablePartnerRoleMessageExchangeImpl(BpelProcess process, String mexId, OPartnerLink oplink, Operation operation, EndpointReference epr, EndpointReference myRoleEPR, PartnerRoleChannel channel) {
        super(process, mexId, oplink, operation, epr, myRoleEPR, channel);
    }


    @Override
    public InvocationStyle getInvocationStyle() {
        return InvocationStyle.UNRELIABLE;
    }


    @Override
    protected void resumeInstance() {
        assert !_contexts.isTransacted() : "checkReplyContext() should have prevented us from getting here.";
        assert !_process.isInMemory() : "resumeInstance() for in-mem processes makes no sense.";

        final WorkEvent we = generateInvokeResponseWorkEvent();
        if (__log.isDebugEnabled()) {
            __log.debug("resumeInstance: scheduling WorkEvent " + we);
        }


        doInTX(new InDbAction<Void>() {

            public Void call(MessageExchangeDAO mexdao) {
                save(mexdao);
                _contexts.scheduler.schedulePersistedJob(we.getDetail(), null);
                return null;
            }
        });
    }

    @Override
    protected void checkReplyContextOk() {
        super.checkReplyContextOk();

        if (!_blocked && getStatus() != Status.ASYNC)
            throw new BpelEngineException("replyXXX operation attempted outside of BLOCKING region!");

        // Prevent user from attempting the replyXXXX calls while a transaction is active. 
        if (_contexts.isTransacted())
            throw new BpelEngineException("Cannot reply to UNRELIABLE style invocation from a transactional context!");
        

    }

    
    
    @Override
    public void replyAsync(String foreignKey) {
        if (__log.isDebugEnabled()) 
            __log.debug("replyAsync mex=" + _mexId);

        sync();
        
        if (!_blocked)
            throw new BpelEngineException("Invalid context for replyAsync(); can only be called during MessageExchangeContext call. ");
        
        // TODO: shouldn't this set _blocked? 
        
        checkReplyContextOk();
        setStatus(Status.ASYNC);
        _foreignKey = foreignKey;
        sync();

    }


    /**
     * Method used by server to wait until a response is available. 
     */
    Status waitForResponse() {
        // TODO: actually wait for response.
        return getStatus();
    }


    
}

