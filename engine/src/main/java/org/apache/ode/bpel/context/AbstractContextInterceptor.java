package org.apache.ode.bpel.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.engine.DbBackedMessageImpl;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.rapi.ContextData;
import org.apache.ode.bpel.rapi.IOContext;
import org.w3c.dom.Element;

public abstract class AbstractContextInterceptor implements ContextInterceptor {
	static final Log __log = LogFactory.getLog(ContextInterceptor.class);
	
	public abstract void configure(Element configuration);
    
    /**
     * Translates the data stored within the context object into SOAP headers or
     * vice versa.
     * 
     * If direction is OUTBOUND, context data must be converted into message headers
     * if direction is INBOUND, context data must be extracted from the message.
     */
    public void process(ContextData ctx, MessageExchangeDAO mexdao, IOContext.Direction dir) throws ContextException {
    	if (dir == IOContext.Direction.INBOUND && mexdao.getRequest() != null) {
    		__log.debug("Delegating inbound request to context interceptor");
    		Message msg = new DbBackedMessageImpl(mexdao.getRequest());
    		onProcessInvoke(ctx, msg);
    	} else if (dir == IOContext.Direction.OUTBOUND  && mexdao.getRequest() != null) {
    		__log.debug("Delegating outbound request to context interceptor");
    		Message msg = new DbBackedMessageImpl(mexdao.getRequest());
    		onPartnerInvoke(ctx, msg);
    	} else if (dir == IOContext.Direction.OUTBOUND_REPLY && mexdao.getResponse() != null) {
    		__log.debug("Delegating inbound response to context interceptor");
    		Message msg = new DbBackedMessageImpl(mexdao.getResponse());
    		onPartnerReply(ctx, msg);
    	} else if (dir == IOContext.Direction.INBOUND_REPLY && mexdao.getResponse() != null) {
    	    Message msg = new DbBackedMessageImpl(mexdao.getResponse());
    	    onProcessReply(ctx, msg);
    	}
    }
    
    public abstract void onPartnerInvoke(ContextData ctx, Message msg);
    public abstract void onPartnerReply(ContextData ctx, Message msg);
    public abstract void onProcessInvoke(ContextData ctx, Message msg);
    public abstract void onProcessReply(ContextData ctx, Message msg);

}
