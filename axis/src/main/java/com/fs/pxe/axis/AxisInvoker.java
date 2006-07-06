package com.fs.pxe.axis;

import com.fs.pxe.axis.epr.MutableEndpoint;
import com.fs.pxe.bpel.iapi.Message;
import com.fs.pxe.bpel.iapi.MessageExchange;
import com.fs.pxe.bpel.iapi.PartnerRoleMessageExchange;
import com.fs.pxe.bpel.scheduler.quartz.QuartzSchedulerImpl;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.Callable;

/**
 * Invoke external services using Axis2 and eventually gets the service
 * reply.
 */
public class AxisInvoker {

  private static final Log __log = LogFactory.getLog(AxisInvoker.class);

  private QuartzSchedulerImpl _scheduler;

  public AxisInvoker(QuartzSchedulerImpl scheduler) {
    _scheduler = scheduler;
  }

  public void invokePartner(PartnerRoleMessageExchange pxeMex) {
    boolean isTwoWay = pxeMex.getMessageExchangePattern() ==
            com.fs.pxe.bpel.iapi.MessageExchange.MessageExchangePattern.REQUEST_RESPONSE;
    try {
      OMElement payload = OMUtils.toOM(pxeMex.getRequest().getMessage());

      Options options = new Options();
      EndpointReference axisEPR = new EndpointReference(((MutableEndpoint)pxeMex.getEndpointReference()).getUrl());
      options.setTo(axisEPR);

      ServiceClient serviceClient = new ServiceClient();
      serviceClient.setOptions(options);

      if (isTwoWay)
        serviceClient.sendReceiveNonBlocking(payload, new AxisResponseCallback(pxeMex));
      else
        serviceClient.sendRobust(payload);
    } catch (AxisFault axisFault) {
      String errmsg = "Error sending message to Axis2 for PXE mex " + pxeMex;
      __log.error(errmsg, axisFault);
      pxeMex.replyWithFailure(MessageExchange.FailureType.COMMUNICATION_ERROR, errmsg, null);
    }
  }

  private class AxisResponseCallback extends Callback {

    private PartnerRoleMessageExchange _pxeMex;

    public AxisResponseCallback(PartnerRoleMessageExchange pxeMex) {
      _pxeMex = pxeMex;
    }

    public void onComplete(AsyncResult asyncResult) {
      final Message response = _pxeMex.createMessage(_pxeMex.getOperation().getOutput().getMessage().getQName());
      try {
        response.setMessage(OMUtils.toDOM(asyncResult.getResponseEnvelope().getBody()));
        _scheduler.execTransaction(new Callable<Object>() {
          public Object call() throws Exception {
            _pxeMex.reply(response);
            return null;
          }
        });
      } catch (AxisFault axisFault) {
        __log.error("Error translating message.", axisFault);
        _pxeMex.replyWithFailure(MessageExchange.FailureType.FORMAT_ERROR, axisFault.getMessage(), null);
      } catch (Exception e) {
        __log.error("Error delivering response.", e);
      }
    }

    public void onError(final Exception exception) {
      try {
        _scheduler.execTransaction(new Callable<Object>() {
          public Object call() throws Exception {
            _pxeMex.replyWithFailure(MessageExchange.FailureType.OTHER,
                    "Error received from invoked service: " + exception.toString(), null);
            return null;
          }
        });
      } catch (Exception e) {
        __log.error("Error delivering failure.", e);
      }
    }
  }
}
