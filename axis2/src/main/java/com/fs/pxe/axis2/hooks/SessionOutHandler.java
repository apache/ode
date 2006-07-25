package com.fs.pxe.axis2.hooks;

import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.fs.utils.Namespaces;
import com.fs.pxe.bpel.iapi.EndpointReference;
import com.fs.pxe.bpel.epr.WSAEndpoint;

/**
 * An outgoing handler adding session id information in the message
 * context.
 */
public class SessionOutHandler extends AbstractHandler {

  private static final Log __log = LogFactory.getLog(SessionOutHandler.class);

  public void invoke(MessageContext messageContext) throws AxisFault {
    Object otargetSession = messageContext.getProperty("targetSessionEndpoint");
    Object ocallbackSession = messageContext.getProperty("callbackSessionEndpoint");
    if (otargetSession == null)
      otargetSession = messageContext.getOptions().getProperty("targetSessionEndpoint");
    if (ocallbackSession == null)
      ocallbackSession = messageContext.getOptions().getProperty("callbackSessionEndpoint");

    if (otargetSession != null || ocallbackSession != null) {
      SOAPHeader header = messageContext.getEnvelope().getHeader();
      SOAPFactory factory = (SOAPFactory) messageContext.getEnvelope().getOMFactory();
      OMNamespace intalioSessNS = factory.createOMNamespace(Namespaces.INTALIO_SESSION_NS, "intalio");
      OMNamespace wsAddrNS = factory.createOMNamespace(Namespaces.WS_ADDRESSING_NS, "addr");
      if (header == null) {
        header = factory.createSOAPHeader(messageContext.getEnvelope());
      }
      if (otargetSession != null) {
        WSAEndpoint targetEpr = (WSAEndpoint) otargetSession;
        OMElement to = factory.createOMElement("To", wsAddrNS);
        header.addChild(to);
        to.setText(targetEpr.getUrl());

//        String soapAction = (String) messageContext.getProperty("soapAction");
//        OMElement wsaAction = factory.createOMElement("Action", wsAddrNS);
//        header.addChild(wsaAction);
//        wsaAction.setText(soapAction);

        if (targetEpr.getSessionId() != null) {
          OMElement session = factory.createOMElement("session", intalioSessNS);
          header.addChild(session);
          session.setText(targetEpr.getSessionId());
        }
        __log.debug("Sending stateful TO epr in message header using session " + targetEpr.getSessionId());
      }
      if (ocallbackSession != null) {
        WSAEndpoint callbackEpr = (WSAEndpoint) ocallbackSession;
        OMElement callback = factory.createOMElement("callback", intalioSessNS);
        header.addChild(callback);
        OMElement address = factory.createOMElement("Address", wsAddrNS);
        callback.addChild(address);
        address.setText(callbackEpr.getUrl());
        if (callbackEpr.getSessionId() != null) {
          OMElement session = factory.createOMElement("session", intalioSessNS);
          session.setText(callbackEpr.getSessionId());
          callback.addChild(session);
        }
        __log.debug("Sending stateful FROM epr in message header using session " + callbackEpr.getSessionId());
      }

      __log.debug("Sending a message containing wsa endpoints in headers for session passing.");
      __log.debug(messageContext.getEnvelope().toString());

    }
  }
}
