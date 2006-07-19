package com.fs.pxe.axis2.hooks;

import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import com.fs.utils.Namespaces;
import com.fs.utils.DOMUtils;

import javax.xml.namespace.QName;

/**
 * An incoming handler adding session id information in the message
 * context.
 */
public class SessionInHandler extends AbstractHandler {

  private static final Log __log = LogFactory.getLog(SessionInHandler.class);

  public void invoke(MessageContext messageContext) throws AxisFault {
    SOAPHeader header = messageContext.getEnvelope().getHeader();
    if (header != null) {
      if (__log.isDebugEnabled())
        __log.debug("Found a header in incoming message, checking if there are endpoints there.");
      // Checking if a session identifier has been provided for a stateful endpoint
      OMElement wsaToSession = header.getFirstChildWithName(new QName(Namespaces.INTALIO_SESSION_NS, "session"));
      if (wsaToSession != null) {
        // Building an endpoint supposed to target the right instance
        Document doc = DOMUtils.newDocument();
        Element serviceEpr = doc.createElementNS(Namespaces.WS_ADDRESSING_NS, "EndpointReference");
        Element sessionId = doc.createElementNS(Namespaces.INTALIO_SESSION_NS, "session");
        doc.appendChild(serviceEpr);
        serviceEpr.appendChild(sessionId);
        sessionId.setTextContent(wsaToSession.getText());
        if (__log.isDebugEnabled())
          __log.debug("A TO endpoint has been found in the header with session: " + wsaToSession.getText());

        // Did the client provide an address too?
        OMElement wsaToAddress = header.getFirstChildWithName(new QName(Namespaces.WS_ADDRESSING_NS, "To"));
        if (wsaToAddress != null) {
          Element addressElmt = doc.createElementNS(Namespaces.WS_ADDRESSING_NS, "Address");
          addressElmt.setTextContent(wsaToAddress.getText());
          serviceEpr.appendChild(addressElmt);
        }
        if (__log.isDebugEnabled())
          __log.debug("Constructed a TO endpoint: " + DOMUtils.domToString(serviceEpr));
        messageContext.setProperty("targetSessionEndpoint", serviceEpr);
      }

      // Seeing if there's a callback, in case our client would be stateful as well
      OMElement callback = header.getFirstChildWithName(new QName(Namespaces.INTALIO_SESSION_NS, "callback"));
      if (callback != null) {
        OMElement callbackSession = callback.getFirstChildWithName(new QName(Namespaces.INTALIO_SESSION_NS, "session"));
        if (callbackSession != null) {
          // Building an endpoint that represents our client (we're supposed to call him later on)
          Document doc = DOMUtils.newDocument();
          Element serviceEpr = doc.createElementNS(Namespaces.WS_ADDRESSING_NS, "EndpointReference");
          Element sessionId = doc.createElementNS(Namespaces.INTALIO_SESSION_NS, "session");
          doc.appendChild(serviceEpr);
          serviceEpr.appendChild(sessionId);
          sessionId.setTextContent(callbackSession.getText());
          if (__log.isDebugEnabled())
            __log.debug("A CALLBACK endpoint has been found in the header with session: " + callbackSession.getText());

          // Did the client give his address as well?
          OMElement wsaToAddress = callback.getFirstChildWithName(new QName(Namespaces.WS_ADDRESSING_NS, "Address"));
          if (wsaToAddress != null) {
            Element addressElmt = doc.createElementNS(Namespaces.WS_ADDRESSING_NS, "Address");
            addressElmt.setTextContent(wsaToAddress.getText());
            serviceEpr.appendChild(addressElmt);
          }
          if (__log.isDebugEnabled())
            __log.debug("Constructed a CALLBACK endpoint: " + DOMUtils.domToString(serviceEpr));
          messageContext.setProperty("callbackSessionEndpoint", serviceEpr);
        }
      }

    }
  }

}
