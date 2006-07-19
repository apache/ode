package com.fs.pxe.jbi;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class JbiMessageExchangeEventRouter implements JbiMessageExchangeProcessor {
  private static final Log __log = LogFactory.getLog(JbiMessageExchangeEventRouter.class);
  
  private PxeContext _pxe;
  
  JbiMessageExchangeEventRouter(PxeContext pxe) {
    _pxe = pxe;
  }
  
  public void onJbiMessageExchange(MessageExchange mex) throws MessagingException {
    if (mex.getRole().equals(javax.jbi.messaging.MessageExchange.Role.CONSUMER)) {
      _pxe._consumer.onJbiMessageExchange(mex);
    } else if (mex.getRole().equals(javax.jbi.messaging.MessageExchange.Role.PROVIDER)) {
      PxeService svc = _pxe.getServiceByServiceName(mex.getEndpoint().getServiceName());
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
