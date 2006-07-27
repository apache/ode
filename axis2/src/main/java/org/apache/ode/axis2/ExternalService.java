package org.apache.ode.axis2;

import org.apache.ode.bpel.epr.MutableEndpoint;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.utils.DOMUtils;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Acts as a service not provided by ODE. Used mainly for invocation as a way to
 * maintain the WSDL decription of used services.
 */
public class ExternalService {

  private static final Log __log = LogFactory.getLog(ExternalService.class);

  private ExecutorService _executorService;

  private Definition _definition;
  private QName _serviceName;
  private String _portName;
  private AxisConfiguration _axisConfig;

  public ExternalService(Definition definition, QName serviceName,
                         String portName, ExecutorService executorService, AxisConfiguration axisConfig) {
    _definition = definition;
    _serviceName = serviceName;
    _portName = portName;
    _executorService = executorService;
    _axisConfig = axisConfig;
  }

  public void invoke(final PartnerRoleMessageExchange odeMex) {
    boolean isTwoWay = odeMex.getMessageExchangePattern() ==
            org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern.REQUEST_RESPONSE;
    try {
      Element msgContent = SOAPUtils.wrap(odeMex.getRequest().getMessage(), _definition, _serviceName,
              odeMex.getOperation(), odeMex.getOperation().getInput().getMessage());

      final OMElement payload = OMUtils.toOM(msgContent);

      Options options = new Options();
      EndpointReference axisEPR = new EndpointReference(((MutableEndpoint)odeMex.getEndpointReference()).getUrl());
      __log.debug("Axis2 sending message to " + axisEPR.getAddress() + " using MEX " + odeMex);
      __log.debug("Message: " + payload);
      options.setTo(axisEPR);

      ConfigurationContext ctx = new ConfigurationContext(_axisConfig);
      final ServiceClient serviceClient = new ServiceClient(ctx, null);
      serviceClient.setOptions(options);
      // Override options are passed to the axis MessageContext so we can
      // retrieve them in our session out handler.
      Options mexOptions = new Options();
      writeHeader(mexOptions, odeMex);
      serviceClient.setOverrideOptions(mexOptions);

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
        serviceClient.fireAndForget(payload);
    } catch (AxisFault axisFault) {
      String errmsg = "Error sending message to Axis2 for ODE mex " + odeMex;
      __log.error(errmsg, axisFault);
      odeMex.replyWithFailure(MessageExchange.FailureType.COMMUNICATION_ERROR, errmsg, null);
    }

  }

  /**
   * Extracts endpoint information from ODE message exchange to stuff them into
   * Axis MessageContext.
   */
  private void writeHeader(Options options, PartnerRoleMessageExchange odeMex) {
    if (odeMex.getEndpointReference() != null) {
      options.setProperty("targetSessionEndpoint", odeMex.getEndpointReference());
    }
    if (odeMex.getCallbackEndpointReference() != null) {
      options.setProperty("callbackSessionEndpoint", odeMex.getCallbackEndpointReference());
    }
  }

}
