package com.fs.pxe.axis;

import com.fs.pxe.bpel.iapi.BpelEngineException;
import com.fs.pxe.bpel.iapi.ContextException;
import com.fs.pxe.bpel.iapi.MessageExchangeContext;
import com.fs.pxe.bpel.iapi.MyRoleMessageExchange;
import com.fs.pxe.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the PXE {@link com.fs.pxe.bpel.iapi.MessageExchangeContext}
 * interface. This class is used by the PXE engine to make invocation of external
 * services using Axis.
 */
public class MessageExchangeContextImpl implements MessageExchangeContext {

  private static final Log __log = LogFactory.getLog(MessageExchangeContextImpl.class);

  private PXEServer _server;
  private AxisInvoker _invoker;

  public MessageExchangeContextImpl(PXEServer server) {
    _server = server;
    _invoker = _server.createInvoker();
  }

  public void invokePartner(PartnerRoleMessageExchange partnerRoleMessageExchange) throws ContextException {
    _invoker.invokePartner(partnerRoleMessageExchange);
  }

  public void onAsyncReply(MyRoleMessageExchange myRoleMessageExchange) throws BpelEngineException {
    PXEService service = _server.getService(myRoleMessageExchange.getServiceName());
    service.notifyResponse(myRoleMessageExchange);
  }
}
