package com.fs.pxe.jbi;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fs.pxe.bpel.iapi.BpelEngineException;
import com.fs.pxe.bpel.iapi.ContextException;
import com.fs.pxe.bpel.iapi.MessageExchangeContext;
import com.fs.pxe.bpel.iapi.MyRoleMessageExchange;
import com.fs.pxe.bpel.iapi.PartnerRoleMessageExchange;

/**
 * Implementation of the PXE {@link com.fs.pxe.bpel.iapi.MessageExchangeContext}
 * interface. This class is used by the PXE engine to make invocation on JBI
 * services provided by other engines (i.e. the BPEL engine is acting as
 * client/consumer of services). 
 */
public class MessageExchangeContextImpl implements MessageExchangeContext {

  private static final Log __log = LogFactory
      .getLog(MessageExchangeContextImpl.class);

  private PxeContext _pxe;

  public MessageExchangeContextImpl(PxeContext pxe) {
    _pxe = pxe;
  }

  public void onAsyncReply(MyRoleMessageExchange myrolemex)
      throws BpelEngineException {
    PxeService pxe = _pxe.getService(myrolemex.getServiceName());
    if (pxe !=  null)
      pxe.onResponse(myrolemex);
    else {
      __log.error("No active service for message exchange: "  + myrolemex);
    }
  }

  public void invokePartner(PartnerRoleMessageExchange mex) throws ContextException {
    _pxe._consumer.invokePartner(mex);
  }
}
