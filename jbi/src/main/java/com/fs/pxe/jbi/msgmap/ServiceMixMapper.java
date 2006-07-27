package com.fs.pxe.jbi.msgmap;

import java.util.List;
import java.util.Set;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fs.pxe.bpel.iapi.Message;
import com.fs.utils.DOMUtils;

/**
 * Message mapper for dealing with the degenerate messages that servicemix
 * components such as servicemix-http provide. These messages are not normalized
 * and hence do not conform to the JBI specification. They are in fact whatever
 * the SOAP body element happens to be.  This mapper will make a reasonable
 * attempt to handle these messages, which means don't count on it working.
 *
 */
public class ServiceMixMapper extends BaseXmlMapper implements Mapper {

  @SuppressWarnings("unchecked")
  public Recognized isRecognized(NormalizedMessage nmsMsg, Operation op) {
    // First of all, if we are not in ServiceMix, we exclude this 
    // as a possibility.
    if (nmsMsg.getClass().getName().indexOf("servicemix") == -1) {
      __log.debug( "Unrecognized message class: " + nmsMsg.getClass() );
      return Recognized.FALSE;
    }
    
    Element msg;
    try {
      msg = parse(nmsMsg.getContent());
      if ( __log.isDebugEnabled() ) {
    	__log.debug("isRecognized() message: " + prettyPrint(msg));
      }
    } catch (MessageTranslationException e) {
      __log.debug( "Unable to parse message: ", e);
      return Recognized.FALSE;
    }
    
    if (op.getInput() == null) {
      __log.debug("no input def - unrecognized");
      return Recognized.FALSE;
    }
    
    if (op.getInput().getMessage() == null) {
      __log.debug("no message def - unrecognized");
      return Recognized.FALSE;
    }
    
    if (op.getInput().getMessage().getParts().size() == 0) {
      __log.debug("no message parts def - unsure");
      return Recognized.UNSURE;
    }
    
    for (String pname : ((Set<String>)op.getInput().getMessage().getParts().keySet())) {
      Part part = op.getInput().getMessage().getPart(pname);
      Element pdata = null;
      // servicemix-http has a (bad) habit of placing the SOAP body content directly in the normalized message
      QName elementName = part.getElementName();
      if ( elementName != null && elementName.getLocalPart().equals( msg.getLocalName())
    		  && elementName.getNamespaceURI().equals(msg.getNamespaceURI()) ) {
        pdata = msg;
      }
      if (pdata == null) {
        // with RPC semantic the body is wrapped by a partName which is same as bodyElementName
        pdata = DOMUtils.findChildByName(msg,new QName(null,part.getName()));
      }
      if (pdata == null) {
        __log.debug("no part data for " + part.getName() + " -- unrecognized.");
        return Recognized.FALSE;
      }
      if (part.getElementName() != null) { 
        Element child = DOMUtils.getFirstChildElement(pdata);
        if (child == null) {
          __log.debug("element part " + part.getName() +
              " does not contain element "  + part.getElementName() + " -- unrecognized");
          return Recognized.FALSE;
        }
        
      }
    }
    
    return Recognized.TRUE;
    
  }

  public void toNMS(NormalizedMessage nmsMsg, Message pxeMsg,
      javax.wsdl.Message msgdef) throws MessagingException,
      MessageTranslationException {

    // Simple, just pass along the message.
    Element pxe = pxeMsg.getMessage();
    if ( __log.isDebugEnabled() ) {
      __log.debug("toNMS() pxe message:\n" + prettyPrint(pxe));
    }
    Element part = DOMUtils.getFirstChildElement( pxe ); 
    Element content = DOMUtils.getFirstChildElement( part ); 
    if ( __log.isDebugEnabled() ) {
      __log.debug("toNMS() normalized message:\n" + prettyPrint(content));
    }
    nmsMsg.setContent(new DOMSource(content));
  }

  public void toPXE(Message pxeMsg, NormalizedMessage nmsMsg,
      javax.wsdl.Message msgdef) throws MessageTranslationException
  {
    Element nms = parse(nmsMsg.getContent());
    boolean docLit = false;
    
    if ( __log.isDebugEnabled() ) {
      __log.debug("toPXE() normalized message:\n" + prettyPrint(nms));
    }
    
    for (String pname : ((Set<String>)msgdef.getParts().keySet())) {
      Part part = msgdef.getPart(pname);
      // servicemix-http has a (bad) habit of placing the SOAP body content directly in the normalized message
      QName elementName = part.getElementName();
      if ( elementName != null && elementName.getLocalPart().equals( nms.getLocalName())
    		  && elementName.getNamespaceURI().equals(nms.getNamespaceURI()) ) {
        docLit = true;
        break;
      }
    }
    if ( docLit ) {
      // Simple, just pass along the message
      __log.debug("toPXE() use doc-lit conversion");
        
      Document doc = newDocument();
      Element message = doc.createElement("message");
      doc.appendChild(message);
    
      Part firstPart = (Part) msgdef.getOrderedParts(null).get(0);
      Element p = doc.createElement(firstPart.getName());
      message.appendChild(p);
      p.appendChild(doc.importNode(nms, true));
      pxeMsg.setMessage(message);
    } else {
      // Simple, just pass along the message
      if ( __log.isDebugEnabled() ) {
	    __log.debug("toPXE() pxe message:\n" + prettyPrint(nms));
	  }
      pxeMsg.setMessage(nms);
    }
  }

  private String prettyPrint( Element el ) {
      try {
          return DOMUtils.prettyPrint( el );
      } catch ( java.io.IOException ioe ) {
          return ioe.getMessage();
      }
  }
}
