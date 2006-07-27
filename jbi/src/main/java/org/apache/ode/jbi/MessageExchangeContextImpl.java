package org.apache.ode.jbi;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;

/**
 * Implementation of the ODE {@link org.apache.ode.bpel.iapi.MessageExchangeContext}
 * interface. This class is used by the ODE engine to make invocation on JBI
 * services provided by other engines (i.e. the BPEL engine is acting as
 * client/consumer of services). 
 */
public class MessageExchangeContextImpl implements MessageExchangeContext {

  private static final Log __log = LogFactory
      .getLog(MessageExchangeContextImpl.class);

  private OdeContext _ode;

  public MessageExchangeContextImpl(OdeContext ode) {
    _ode = ode;
  }

  public void onAsyncReply(MyRoleMessageExchange myrolemex)
      throws BpelEngineException {
    OdeService ode = _ode.getService(myrolemex.getServiceName());
    if (ode !=  null)
      ode.onResponse(myrolemex);
    else {
      __log.error("No active service for message exchange: "  + myrolemex);
    }
  }

  public void invokePartner(PartnerRoleMessageExchange mex) throws ContextException {
    _ode._consumer.invokePartner(mex);
  }
}
