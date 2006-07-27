package org.apache.ode.jbi;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class JbiMessageExchangeEventRouter implements JbiMessageExchangeProcessor {
  private static final Log __log = LogFactory.getLog(JbiMessageExchangeEventRouter.class);
  
  private OdeContext _ode;
  
  JbiMessageExchangeEventRouter(OdeContext ode) {
    _ode = ode;
  }
  
  public void onJbiMessageExchange(MessageExchange mex) throws MessagingException {
    if (mex.getRole().equals(javax.jbi.messaging.MessageExchange.Role.CONSUMER)) {
      _ode._consumer.onJbiMessageExchange(mex);
    } else if (mex.getRole().equals(javax.jbi.messaging.MessageExchange.Role.PROVIDER)) {
      OdeService svc = _ode.getServiceByServiceName(mex.getEndpoint().getServiceName());
      if (svc == null)  {
        __log.error("Received message exchange for unknown service: " + mex.getEndpoint().getServiceName());
        return;
      }
      svc.onJbiMessageExchange(mex);
    } else {
      __log.debug("unexpected role: " + mex.getRole());
    }
   
  }

}
