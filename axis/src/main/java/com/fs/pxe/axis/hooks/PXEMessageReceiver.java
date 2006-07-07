package com.fs.pxe.axis.hooks;

import com.fs.pxe.axis.PXEService;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.ExecutorService;

/**
 * Receives messages forwarded by Axis.
 */
public class PXEMessageReceiver extends AbstractMessageReceiver {

  private static final Log __log = LogFactory.getLog(PXEMessageReceiver.class);

  private PXEService _service;
  private ExecutorService _executorService;

  public final void receive(final MessageContext msgContext) throws AxisFault {
    if (__log.isDebugEnabled())
      __log.debug("Received message for " + msgContext.getAxisService().getName() +
              "." + msgContext.getAxisOperation().getName());
    if (hasResponse(msgContext.getAxisOperation())) {
      // Expecting a response, running in the same thread
      MessageContext outMsgContext = Utils.createOutMessageContext(msgContext);
      outMsgContext.getOperationContext().addMessageContext(outMsgContext);
      invokeBusinessLogic(msgContext, outMsgContext);
      if (__log.isDebugEnabled()) {
        __log.debug("Reply for " + msgContext.getAxisService().getName() +
              "." + msgContext.getAxisOperation().getName());
        __log.debug("Reply message " + outMsgContext.getEnvelope());
      }
      AxisEngine engine = new AxisEngine(
              msgContext.getOperationContext().getServiceContext().getConfigurationContext());
      engine.send(outMsgContext);
    } else {
      // No response expected, this thread doesn't need us
      _executorService.submit(new Runnable() {
        public void run() {
          try {
            invokeBusinessLogic(msgContext, null);
          } catch (AxisFault axisFault) {
            __log.error("Error process in-only message.", axisFault);
          }
        }
      });
    }
  }

  private void invokeBusinessLogic(MessageContext msgContext, MessageContext outMsgContext)
          throws AxisFault {
    _service.onAxisMessageExchange(msgContext, outMsgContext, getSOAPFactory(msgContext));
  }

  public void setService(PXEService service) {
    _service = service;
  }

  public void setExecutorService(ExecutorService executorService) {
    _executorService = executorService;
  }

  private boolean hasResponse(AxisOperation op) {
    switch(op.getAxisSpecifMEPConstant()) {
      case AxisOperation.MEP_CONSTANT_IN_OUT: return true;
      case AxisOperation.MEP_CONSTANT_OUT_ONLY: return true;
      case AxisOperation.MEP_CONSTANT_OUT_OPTIONAL_IN: return true;
      case AxisOperation.MEP_CONSTANT_ROBUST_OUT_ONLY: return true;
      default: return false;
    }
  }
}
