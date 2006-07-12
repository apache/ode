package com.fs.pxe.axis;

import com.fs.pxe.bpel.iapi.PartnerRoleMessageExchange;
import com.fs.pxe.bpel.iapi.MessageExchange;
import com.fs.pxe.bpel.iapi.Message;
import com.fs.pxe.bpel.epr.MutableEndpoint;
import com.fs.utils.DOMUtils;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * Acts as a service not provided by PXE. Used mainly for invocation as a way to
 * maintain the WSDL decription of used services.
 */
public class ExternalService {

  private static final Log __log = LogFactory.getLog(ExternalService.class);

  private ExecutorService _executorService;

  private Definition _definition;
  private QName _serviceName;
  private String _portName;

  public ExternalService(Definition definition, QName serviceName,
                         String portName, ExecutorService executorService) {
    _definition = definition;
    _serviceName = serviceName;
    _portName = portName;
    _executorService = executorService;
  }

  public void invoke(final PartnerRoleMessageExchange pxeMex) {
    boolean isTwoWay = pxeMex.getMessageExchangePattern() ==
            com.fs.pxe.bpel.iapi.MessageExchange.MessageExchangePattern.REQUEST_RESPONSE;
    try {
      Element msgContent = SOAPUtils.wrap(pxeMex.getRequest().getMessage(), _definition, _serviceName,
              pxeMex.getOperation(), pxeMex.getOperation().getInput().getMessage());

      final OMElement payload = OMUtils.toOM(msgContent);

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
}
