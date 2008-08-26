package org.apache.ode.bpel.engine;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;


/**
 * A reliable MEP that delegates messages to a list of subscribers  
 *
 * @author $author$
 * @version $Revision$
  */
public class BrokeredMyRoleMessageExchangeImpl
    extends MyRoleMessageExchangeImpl {
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
    public BrokeredMyRoleMessageExchangeImpl(BpelProcess process, BpelEngineImpl engine,
        List<MyRoleMessageExchange> subscribers, MessageExchangeDAO mexDao, MyRoleMessageExchange template) {
        super(process, engine, mexDao);
        this.subscribers = subscribers;
        this.template = template;
    }

    /**
     * Propagate the invoke reliable call to each subscriber
     */
    public Future invoke(Message request) {
    	Future myFuture = null;
        for (MyRoleMessageExchange subscriber : subscribers) {
            Future theirFuture = subscriber.invoke(request);
            if (myFuture == null) {
            	myFuture = theirFuture;
            }
        }
        return myFuture;
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
}
