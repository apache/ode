package org.apache.ode.bpel.engine;

import java.util.List;

import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.o.OPartnerLink;


/**
 * A reliable MEP that delegates messages to a list of subscribers  
 *
 * @author $author$
 * @version $Revision$
  */
public class BrokeredReliableMyRoleMessageExchangeImpl
    extends ReliableMyRoleMessageExchangeImpl {
    private List<MyRoleMessageExchange> subscribers;
    private MyRoleMessageExchange template;

    /**
     * Creates a new BrokeredReliableMyRoleMessageExchangeImpl object.
     *
     * @param process 
     * @param subscribers 
     * @param mexId 
     * @param oplink 
     * @param template 
     */
    public BrokeredReliableMyRoleMessageExchangeImpl(BpelProcess process,
        List<MyRoleMessageExchange> subscribers, String mexId,
        OPartnerLink oplink, MyRoleMessageExchange template) {
        super(process, mexId, oplink, template.getOperation(),
            template.getServiceName());
        this.subscribers = subscribers;
        this.template = template;
    }

    /**
     * Propagate the invoke reliable call to each subscriber
     */
    public void invokeReliable() {
        for (MyRoleMessageExchange subscriber : subscribers) {
            subscriber.invokeReliable();
        }
    }

    /**
     * Fool the engine into thinking I'm one-way, wherever possible
     *
     * @return type
     */
    @Override
    public AckType getAckType() {
        return AckType.ONEWAY;
    }

    /**
     * Use the EPR of one of the subscribers as my EPR
     *
     * @return type
     *
     * @throws BpelEngineException BpelEngineException 
     */
    @Override
    public EndpointReference getEndpointReference() throws BpelEngineException {
        return template.getEndpointReference();
    }

    /**
     * Use the response from one of the subscribers as my response 
     *
     * @return type
     */
    @Override
    public Message getResponse() {
        return template.getResponse();
    }

    /**
     * Propagate set request call to every subscriber
     *
     * @param request request 
     */
    @Override
    public void setRequest(Message request) {
        for (MyRoleMessageExchange subscriber : subscribers) {
            subscriber.setRequest(cloneMessage(request));
        }
    }

    /**
     * Propagate set timeout call to every subscriber
     *
     * @param timeout timeout 
     */
    @Override
    public void setTimeout(long timeout) {
        for (MyRoleMessageExchange subscriber : subscribers) {
            subscriber.setTimeout(timeout);
        }
    }
}
