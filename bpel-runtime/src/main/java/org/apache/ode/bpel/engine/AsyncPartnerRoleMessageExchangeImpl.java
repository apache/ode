package org.apache.ode.bpel.engine;

import javax.wsdl.Operation;
import javax.wsdl.PortType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;

/**
 * Implementation of the {@link PartnerRoleMessageExchange} interface that is used when the ASYNC invocation style is being used
 * (see {@link InvocationStyle#ASYNC}). The basic idea here is that with this style, the IL does not get the "message" (i.e. this
 * object) until the ODE transaction has committed, and it does not block during the performance of the operation. Hence, when a
 * reply becomes available, we'll need to schedule a transaction to process it.
 * 
 * @author Maciej Szefler
 * 
 */
public class AsyncPartnerRoleMessageExchangeImpl extends PartnerRoleMessageExchangeImpl {

    private static final Log __log = LogFactory.getLog(AsyncPartnerRoleMessageExchangeImpl.class);
    
    AsyncPartnerRoleMessageExchangeImpl(BpelProcess process, String mexId, PortType portType, Operation operation,
            EndpointReference epr, EndpointReference myRoleEPR, PartnerRoleChannel channel) {
        super(process, mexId, portType, operation, epr, myRoleEPR, channel);
    }

    @Override
    protected void resumeInstance() {
        assert !_contexts.scheduler.isTransacted() : "checkReplyContext() should have prevented us from getting here.";
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

        // Prevent user from attempting the replyXXXX calls while a transaction is active. 
        if (!_ownerThread.get() && _contexts.scheduler.isTransacted())
            throw new BpelEngineException("Cannot reply to ASYNC style invocation from a transactional context!");
        

    }

    @Override
    public void replyAsync(String foreignKey) {
        if (__log.isDebugEnabled()) 
            __log.debug("replyAsync mex=" + _mexId);

        sync();
        
        if (!_blocked)
            throw new BpelEngineException("Invalid context for replyAsync(); can only be called during MessageExchangeContext call. ");
        checkReplyContextOk();
        setStatus(Status.ASYNC);
        _foreignKey = foreignKey;
        sync();

    }

    @Override
    public InvocationStyle getInvocationStyle() {
        return InvocationStyle.ASYNC;
    }

}
