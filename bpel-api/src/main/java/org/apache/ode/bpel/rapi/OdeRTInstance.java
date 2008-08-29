package org.apache.ode.bpel.rapi;

import java.io.IOException;
import java.io.OutputStream;


public interface OdeRTInstance {

    enum InvokeResponseType {
        REPLY,
        FAULT,
        FAILURE
    }

    /**
     * Set the execution context.
     * @param ctx
     */
    void setContext(OdeRTInstanceContext ctx);


    /**
     * Called when the engine creates an instance (i.e. a create-instance mex is received).
     * @param messageExchangeId message exchange id for create-instance mex
     */
    void onCreateInstance(String messageExchangeId);


    /**
     * Called when the engine detects a matching selector (i.e. when a partner invokes the process).
     *
     * @param selectId selector identifier
     * @param messageExchangeId message exchange identifier
     * @param selectorIdx which selector in the set matched
     */
    void onSelectEvent(String selectId, String messageExchangeId, int selectorIdx);

    /**
     * Called when an invoke received a response.
     * @param invokeId
     * @param mexid
     */
    void onInvokeResponse(String invokeId, InvokeResponseType irt, String mexid);


    /**
     * Called when the engine determines that a registered timer is ready to fire.
     *
     * @param timerId
     */
    void onTimerEvent(String timerId);

    /**
     * @return
     */
    boolean execute();

    /**
     * @param channel
     * @param activityId
     * @param action
     */
    void recoverActivity(String channel, long activityId, String action, FaultInfo fault);

    /**
     * Save the execution state into the given output stream, and return a cached representation of the state. The cached
     * representation will be used by the engine to speed up state recovery (i.e. when de-serializing can be avoided).
     *
     * @return cached
     */
    Object saveState(OutputStream os) throws IOException ;

}