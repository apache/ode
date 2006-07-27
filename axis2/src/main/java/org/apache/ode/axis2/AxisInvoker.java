package org.apache.ode.axis2;

import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.epr.MutableEndpoint;
import org.apache.ode.utils.DOMUtils;
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

  public void invokePartner(final PartnerRoleMessageExchange odeMex) {
    boolean isTwoWay = odeMex.getMessageExchangePattern() ==
            org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern.REQUEST_RESPONSE;
    try {
      Document doc = DOMUtils.newDocument();

      Element op = doc.createElement(odeMex.getOperationName());
      op.appendChild(doc.importNode(DOMUtils.getFirstChildElement(odeMex.getRequest().getMessage()), true));

      final OMElement payload = OMUtils.toOM(op);

      Options options = new Options();
      EndpointReference axisEPR = new EndpointReference(((MutableEndpoint)odeMex.getEndpointReference()).getUrl());
      __log.debug("Axis2 sending message to " + axisEPR.getAddress() + " using MEX " + odeMex);
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
          __log.error("We've been interrupted while waiting for reply to MEX " + odeMex + "!!!");
          String errmsg = "Error sending message to Axis2 for ODE mex " + odeMex;
          __log.error(errmsg, e);
          odeMex.replyWithFailure(MessageExchange.FailureType.COMMUNICATION_ERROR, errmsg, null);
        }

        final Message response = odeMex.createMessage(odeMex.getOperation().getOutput().getMessage().getQName());
        Element responseElmt = OMUtils.toDOM(reply);
        __log.debug("Received synchronous response for MEX " + odeMex);
        __log.debug("Message: " + DOMUtils.domToString(responseElmt));
        response.setMessage(OMUtils.toDOM(reply));
        odeMex.reply(response);
      } else
        serviceClient.sendRobust(payload);
    } catch (AxisFault axisFault) {
      String errmsg = "Error sending message to Axis2 for ODE mex " + odeMex;
      __log.error(errmsg, axisFault);
      odeMex.replyWithFailure(MessageExchange.FailureType.COMMUNICATION_ERROR, errmsg, null);
    }
  }

  // This code can be used later on if we start using Axis2 sendNonBlocking but for now
  // we have to block the calling thread as the engine expects an immediate (non delayed)
  // response.
  private class AxisResponseCallback extends Callback {

    private PartnerRoleMessageExchange _odeMex;

    public AxisResponseCallback(PartnerRoleMessageExchange odeMex) {
      _odeMex = odeMex;
    }

    public void onComplete(AsyncResult asyncResult) {
      final Message response = _odeMex.createMessage(_odeMex.getOperation().getOutput().getMessage().getQName());
      if (__log.isDebugEnabled())
        __log.debug("Received a synchronous response for service invocation on MEX " + _odeMex);
      try {
        response.setMessage(OMUtils.toDOM(asyncResult.getResponseEnvelope().getBody()));
        _odeMex.reply(response);
      } catch (AxisFault axisFault) {
        __log.error("Error translating message.", axisFault);
        _odeMex.replyWithFailure(MessageExchange.FailureType.FORMAT_ERROR, axisFault.getMessage(), null);
      } catch (Exception e) {
        __log.error("Error delivering response.", e);
      }
    }

    public void onError(final Exception exception) {
      if (__log.isDebugEnabled())
        __log.debug("Received a synchronous failure for service invocation on MEX " + _odeMex);
      try {
        _odeMex.replyWithFailure(MessageExchange.FailureType.OTHER,
                "Error received from invoked service: " + exception.toString(), null);
      } catch (Exception e) {
        __log.error("Error delivering failure.", e);
      }
    }
  }
}
