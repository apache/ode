package org.apache.ode.bpel.context;

import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.rapi.ContextData;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.uuid.UUID;
import org.w3c.dom.Element;

public class TracingContextInterceptor extends AbstractContextInterceptor {

	public void configure(Element configuration) {
		// TODO Auto-generated method stub

	}

	public void onPartnerInvoke(ContextData ctx, Message msg) {
	    String tid = ctx.get("tracing", "id");
	    if (tid != null) {
	        try {
                msg.setHeaderPart("TracingId", DOMUtils.stringToDOM("<TracingId xmlns=\"urn:trace\">" + tid + "</TracingId>"));
            } catch (Exception e) {
                e.printStackTrace();
            }
	    }
	}

	public void onPartnerReply(ContextData ctx, Message msg) {
        Element tide = msg.getHeaderPart("TracingId");
        if (tide != null) {
            String pids = ctx.get("tracing", "partner-ids");
            ctx.put("tracing", "partner-ids", (pids == null) ? tide.getTextContent() : pids + ", " + tide.getTextContent());    
        }
	}

	public void onProcessInvoke(ContextData ctx, Message msg) {
	    Element tide = msg.getHeaderPart("TracingId");
	    if (tide != null) {
	        ctx.put("tracing", "id", tide.getTextContent());    
	    } else {
	        ctx.put("tracing", "id", new UUID().toString());
	    }
	}

	public void onProcessReply(ContextData ctx, Message msg) {
        String tid = ctx.get("tracing", "id");
        if (tid != null) {
            try {
                msg.setHeaderPart("TracingId", DOMUtils.stringToDOM("<TracingId xmlns=\"urn:trace\">" + tid + "</TracingId>"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
