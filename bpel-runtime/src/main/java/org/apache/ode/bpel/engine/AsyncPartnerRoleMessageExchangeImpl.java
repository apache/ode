package org.apache.ode.bpel.engine;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.w3c.dom.Element;

/**
 * Implementation of the {@link PartnerRoleMessageExchange} interface that is used when the ASYNC invocation 
 * style is being used (see {@link InvocationStyle#ASYNC}). The basic idea here is that with this style, the
 * IL does not get the "message" (i.e. this object) until the ODE transaction has committed, and it does not
 * block during the performance of the operation. Hence, when a reply becomes available, we'll need to 
 * schedule a transaction to process it. 
 * 
 * @author Maciej Szefler
 *
 */
public class AsyncPartnerRoleMessageExchangeImpl extends PartnerRoleMessageExchangeImpl {

    private static final Log __log = LogFactory.getLog(AsyncPartnerRoleMessageExchangeImpl.class);
    
    AsyncPartnerRoleMessageExchangeImpl(BpelEngineImpl engine, String mexId, PortType portType, Operation operation, boolean inMem, EndpointReference epr, EndpointReference myRoleEPR, PartnerRoleChannel channel) {
        super(engine, mexId, portType, operation, inMem, epr, myRoleEPR, channel);
    }
    
    public void replyWithFault(QName faultType, Message outputFaultMessage) throws BpelEngineException {
        if(!isAsync())
            throw new BpelEngineException("Invalid action, message-exchange is not in ASYNC state!");
        
        super.replyWithFault(faultType,outputFaultMessage);
        scheduleContinuation();
    }

    public void reply(Message response) throws BpelEngineException {
        if(!isAsync())
            throw new BpelEngineException("Invalid action, message-exchange is not in ASYNC state!");

        super.reply(response);
        scheduleContinuation();

    }

    public void replyWithFailure(FailureType type, String description, Element details) throws BpelEngineException {
        if(!isAsync())
            throw new BpelEngineException("Invalid action, message-exchange is not in ASYNC state!");
        super.replyWithFailure(type, description, details);
        scheduleContinuation();
    }
        

    /**
     * Check if we are in the ASYNC state. 
     * 
     * @return
     */
    private boolean isAsync() {
        return getStatus() == Status.ASYNC;
    }


    /**
     * Continue from the ASYNC state by scheduling a continuation to process a response/fault/failure. 
     */
    private void scheduleContinuation() {
        // If there is no channel waiting for us, there is nothing to do.
        if (getPartnerRoleChannel() == null) {
            if (__log.isDebugEnabled()) {
                __log.debug("no channel on mex=" + getMessageExchangeId());
            }
            return;
        }
        

        WorkEvent we = new WorkEvent();
        we.setIID(_iid);
        we.setType(WorkEvent.Type.INVOKE_RESPONSE);
        we.setInMem(_inMem);
        we.setChannel(_responseChannel);
        we.setMexId(_mexId);

        if (__log.isDebugEnabled()) {
            __log.debug("scheduleContinuation: scheduling WorkEvent " + we);
        }
        
        if (_inMem)
            _contexts.scheduler.scheduleVolatileJob(true, we.getDetail());
        else
            _contexts.scheduler.schedulePersistedJob(we.getDetail(), null);
    }

}
