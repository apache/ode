package com.fs.pxe.axis2;

import com.fs.pxe.bpel.iapi.Message;
import com.fs.pxe.bpel.iapi.MessageExchange;
import com.fs.pxe.bpel.iapi.PartnerRoleMessageExchange;
import com.fs.pxe.bpel.epr.MutableEndpoint;
import com.fs.utils.DOMUtils;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Invoke external services using Axis2 and eventually gets the service
 * reply.
 */
public class AxisInvoker {

  private static final Log __log = LogFactory.getLog(AxisInvoker.class);

  private ExecutorService _executorService;

  public AxisInvoker(ExecutorService executorService) {
    _executorService = executorService;
  }

  public void invokePartner(final PartnerRoleMessageExchange pxeMex) {
    boolean isTwoWay = pxeMex.getMessageExchangePattern() ==
            com.fs.pxe.bpel.iapi.MessageExchange.MessageExchangePattern.REQUEST_RESPONSE;
    try {
      Document doc = DOMUtils.newDocument();

      Element op = doc.createElement(pxeMex.getOperationName());
      op.appendChild(doc.importNode(DOMUtils.getFirstChildElement(pxeMex.getRequest().getMessage()), true));

      final OMElement payload = OMUtils.toOM(op);

      Options options = new Options();
      EndpointReference axisEPR = new EndpointReference(((MutableEndpoint)pxeMex.getEndpointReference()).getUrl());
      __log.debug("Axis2 sending message to " + axisEPR.getAddress() + " using MEX " + pxeMex);
      __log.debug("Message: " + payload);
      options.setTo(axisEPR);

      final ServiceClient serviceClient = new ServiceClient();
      serviceClient.setOptions(options);

      if (isTwoWay) {
        // Invoking in a separate thread even though we're supposed to wait for a synchronous reply
        // to force clear transaction separation.
        Future<OMElement> freply = _executorService.submit(new Callable<OMElement>() {
          public OMElement call() throws Exception {
            return serviceClient.sendReceive(payload);
          }
        });
        OMElement reply = null;
        try {
          reply = freply.get();
        } catch (Exception e) {
          __log.error("We've been interrupted while waiting for reply to MEX " + pxeMex + "!!!");
          String errmsg = "Error sending message to Axis2 for PXE mex " + pxeMex;
          __log.error(errmsg, e);
          pxeMex.replyWithFailure(MessageExchange.FailureType.COMMUNICATION_ERROR, errmsg, null);
        }

        final Message response = pxeMex.createMessage(pxeMex.getOperation().getOutput().getMessage().getQName());
        Element responseElmt = OMUtils.toDOM(reply);
        __log.debug("Received synchronous response for MEX " + pxeMex);
        __log.debug("Message: " + DOMUtils.domToString(responseElmt));
        response.setMessage(OMUtils.toDOM(reply));
        pxeMex.reply(response);
      } else
        serviceClient.sendRobust(payload);
    } catch (AxisFault axisFault) {
      String errmsg = "Error sending message to Axis2 for PXE mex " + pxeMex;
      __log.error(errmsg, axisFault);
      pxeMex.replyWithFailure(MessageExchange.FailureType.COMMUNICATION_ERROR, errmsg, null);
    }
  }

  // This code can be used later on if we start using Axis2 sendNonBlocking but for now
  // we have to block the calling thread as the engine expects an immediate (non delayed)
  // response.
  private class AxisResponseCallback extends Callback {

    private PartnerRoleMessageExchange _pxeMex;

    public AxisResponseCallback(PartnerRoleMessageExchange pxeMex) {
      _pxeMex = pxeMex;
    }

    public void onComplete(AsyncResult asyncResult) {
      final Message response = _pxeMex.createMessage(_pxeMex.getOperation().getOutput().getMessage().getQName());
      if (__log.isDebugEnabled())
        __log.debug("Received a synchronous response for service invocation on MEX " + _pxeMex);
      try {
        response.setMessage(OMUtils.toDOM(asyncResult.getResponseEnvelope().getBody()));
        _pxeMex.reply(response);
      } catch (AxisFault axisFault) {
        __log.error("Error translating message.", axisFault);
        _pxeMex.replyWithFailure(MessageExchange.FailureType.FORMAT_ERROR, axisFault.getMessage(), null);
      } catch (Exception e) {
        __log.error("Error delivering response.", e);
      }
    }

    public void onError(final Exception exception) {
      if (__log.isDebugEnabled())
        __log.debug("Received a synchronous failure for service invocation on MEX " + _pxeMex);
      try {
        _pxeMex.replyWithFailure(MessageExchange.FailureType.OTHER,
                "Error received from invoked service: " + exception.toString(), null);
      } catch (Exception e) {
        __log.error("Error delivering failure.", e);
      }
    }
  }
}
