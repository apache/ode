package org.apache.ode.jbi.msgmap;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.ode.utils.DOMUtils;

/**
 * Mapper to handle messages that are single-part docliteral (WSDL2.0 style).
 */
public class DocLitMapper extends BaseXmlMapper implements Mapper {

  public Recognized isRecognized(NormalizedMessage nmsMsg, Operation op) {
    Message in = op.getInput() == null ? null : op.getInput().getMessage();
    Message out = op.getOutput() == null ? null : op.getOutput().getMessage();

    // First we check if the signature of the operation makes this mapper
    // a possibility.
    if (!checkMessageDef(in) || !checkMessageDef(out))
      return Recognized.FALSE;

    // Ok, if it is possible, then we have to check the content of the
    // input message to see if it matches doc lit style, if the input
    // message is empty, then we have an ambiguous match
    if (in == null)
      return Recognized.UNSURE;

    Part inpart = (Part) in.getParts().values().iterator().next();
    assert inpart != null;
    assert inpart.getElementName() != null;

    Element msg;
    try {
      msg = parse(nmsMsg.getContent());
    } catch (MessageTranslationException e) {
      __log.debug("Failed to parse NMS message: " + nmsMsg, e);
      return Recognized.FALSE;
    }

    QName elName = DOMUtils.getElementQName(msg);

    // If the message element matches the single part element, then
    // we are using doc-lit style messaging.
    return inpart.getElementName().equals(elName) ? Recognized.TRUE : Recognized.FALSE;
  }

  public void toNMS(NormalizedMessage nmsMsg,
      org.apache.ode.bpel.iapi.Message odeMsg, Message msgdef)
      throws MessagingException, MessageTranslationException {
    // For empty messages there is nothing to do.
    if (msgdef == null || msgdef.getParts().size() == 0)
      return;

    // The assertions assume isRecognized was called!
    assert msgdef.getParts().size() == 1 : "multi part!";
    Part partdef = (Part) msgdef.getParts().values().iterator().next();
    assert partdef.getElementName() != null : "non-element part!";

    Element part = DOMUtils.findChildByName(odeMsg.getMessage(),new QName(null, partdef.getName()));
    if (part == null) {
      String errmsg = "ODE message did not contain expected part: " +  partdef.getName();
      __log.debug(errmsg);
      throw new MessageTranslationException(errmsg);
    }
    
    Element content = DOMUtils.findChildByName(part, partdef.getElementName());
    if (content == null) {
      String errmsg = "ODE message did not contain element " + partdef.getElementName() + " in part: " +  partdef.getName();
      __log.debug(errmsg);
      throw new MessageTranslationException(errmsg);
    }

    nmsMsg.setContent(new DOMSource(content));
  }

  public void toODE(org.apache.ode.bpel.iapi.Message odeMsg,
      NormalizedMessage nmsMsg, Message msgdef)
      throws MessageTranslationException {

    // The assertions assume isRecognized was called!
    assert msgdef.getParts().size() == 1 : "multi part!";
    Part partdef = (Part) msgdef.getParts().values().iterator().next();
    assert partdef.getElementName() != null : "non-element part!";
    
    Element el = parse(nmsMsg.getContent());
    if (!DOMUtils.getElementQName(el).equals(partdef.getElementName())) {
      String errmsg = "NMS message did not contain element "  + partdef.getElementName();
      __log.debug(errmsg);
      throw new MessageTranslationException(errmsg);
    }
    

    Document doc = newDocument();
    Element msgel = doc.createElement("message");
    doc.appendChild(msgel);
    Element pel = doc.createElement(partdef.getName());
    msgel.appendChild(pel);
    pel.appendChild(doc.importNode(el,true));
    odeMsg.setMessage(msgel);
    
  }

  /**
   * Check if a message definition is compatible with doc-lit mapping. For
   * WSDL1.1 this means that the message definition needs to have exactly one
   * element part.
   * 
   * @param msg
   *          message definition to check
   * @return true if compatible, false otherwise
   */
  private boolean checkMessageDef(Message msg) {
    // Null messages are acceptable.
    if (msg == null)
      return true;
  
    // If we are non-empty, we need to have exactly one part.
    if (msg.getParts().size() != 1)
      return false;
  
    // The single part must also be an "element" typed part.
    if (((Part) msg.getParts().values().iterator().next()).getElementName() == null)
      return false;
  
    return true;
  
  }

}
