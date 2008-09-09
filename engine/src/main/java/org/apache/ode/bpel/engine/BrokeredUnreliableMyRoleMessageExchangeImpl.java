package org.apache.ode.bpel.engine;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;

/**
 * An unreliable MEP that delegates messages to a list of subscribers  
 *
 * @author $author$
 * @version $Revision$
  */
public class BrokeredUnreliableMyRoleMessageExchangeImpl
    extends UnreliableMyRoleMessageExchangeImpl {
    private List<MyRoleMessageExchange> subscribers;
    private MyRoleMessageExchange template;

    /**
     * Creates a new BrokeredUnreliableMyRoleMessageExchangeImpl object.
     *
     * @param process 
     * @param subscribers 
     * @param mexId 
     * @param template
     */
    public BrokeredUnreliableMyRoleMessageExchangeImpl(ODEProcess process,
        List<MyRoleMessageExchange> subscribers, String mexId, MyRoleMessageExchange template) {
        super(process, mexId, null, template.getOperation(), template.getServiceName());
        this.subscribers = subscribers;
        this.template = template;
    }

    /**
     * Propagate the invoke asynchronous call to each subscriber
     *
     * @return type
     */
    public Future<Status> invokeAsync() {
        for (MyRoleMessageExchange subscriber : subscribers) {
            subscriber.invokeAsync();
        }
        return new CompletedFuture();
    }

    /**
     * Propagate the invoke blocking call to each subscriber
     *
     * @return type
     *
     * @throws BpelEngineException BpelEngineException 
     * @throws TimeoutException TimeoutException 
     */
    public Status invokeBlocking() throws BpelEngineException, TimeoutException {
        for (MyRoleMessageExchange subscriber : subscribers) {
            subscriber.invokeBlocking();
        }
        return Status.COMPLETED;
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

    /**
     * An implementation of Future that acts as if its "done".  
     *
     * @author $author$
     * @version $Revision$
      */
    private class CompletedFuture implements Future<Status> {
        /**
         * Nothing to cancel
         *
         * @param mayInterruptIfRunning mayInterruptIfRunning 
         *
         * @return type
         */
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        /**
         * I'm done, thanks for asking
         *
         * @return type
         *
         * @throws InterruptedException InterruptedException 
         * @throws ExecutionException ExecutionException 
         */
        public Status get() throws InterruptedException, ExecutionException {
            return Status.COMPLETED;
        }

        /**
         * I'm done, thanks for asking
         *
         * @param timeout timeout 
         * @param unit unit 
         *
         * @return type
         *
         * @throws InterruptedException InterruptedException 
         * @throws ExecutionException ExecutionException 
         * @throws TimeoutException TimeoutException 
         */
        public Status get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
            return Status.COMPLETED;
        }

        /**
         * No, I'm not cancelled
         *
         * @return type
         */
        public boolean isCancelled() {
            return false;
        }

        /**
         * Yes, for crying out loud, I'm done
         *
         * @return type
         */
        public boolean isDone() {
            return true;
        }
    }
}
