package com.fs.pxe.jbi.msgmap;

import java.util.Set;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Element;

import com.fs.pxe.bpel.iapi.Message;
import com.fs.utils.DOMUtils;

/**
 * Message mapper for dealing with the degenerate messages that servicemix
 * components such as servicemix-http provide. These messages are not normalized
 * and hence do not conform to the JBI specification. They are in fact whatever
 * the SOAP body element happens to be.  This mapper will make a reasonable
 * attempt to handle these messages, which effectively means that doc-lit
 * RPC SOAP messages will work. 
 *
 */
public class ServiceMixMapper extends BaseXmlMapper implements Mapper {

  @SuppressWarnings("unchecked")
  public Recognized isRecognized(NormalizedMessage nmsMsg, Operation op) {
    // First of all, if we are not in ServiceMix, we exclude this 
    // as a possibility.
    if (nmsMsg.getClass().getName().indexOf("servicemix") == -1)
      return Recognized.FALSE;
    
    Element msg;
    try {
      msg = parse(nmsMsg.getContent());
    } catch (MessageTranslationException e) {
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
      Element pdata = DOMUtils.findChildByName(msg,new QName(null,part.getName()));
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
    nmsMsg.setContent(new DOMSource(pxe));
  }

  public void toPXE(Message pxeMsg, NormalizedMessage nmsMsg,
      javax.wsdl.Message msgdef) throws MessageTranslationException {
    // Simple, just pass along the message
    Element nms = parse(nmsMsg.getContent());
    pxeMsg.setMessage(nms);
  }

}
