package com.fs.pxe.jbi.msgmap;

import java.util.List;
import javax.jbi.messaging.*;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fs.pxe.bpel.iapi.Message;
import com.fs.utils.DOMUtils;

/**
 * Mapper for converting PXE messages to NMS messages using the WSDL 11 wrapper
 * format.
 */
public class JbiWsdl11WrapperMapper extends BaseXmlMapper implements Mapper {

  public static final String URI_WSDL11_WRAPPER = "http://java.sun.com/xml/ns/jbi/wsdl-11-wrapper";

  public static final QName WSDL11_W_MESSAGE = new QName(URI_WSDL11_WRAPPER,
      "message");


  public JbiWsdl11WrapperMapper() {
  }

  public Recognized isRecognized(NormalizedMessage nmsMsg,
      Operation op) {
    Element srcel;
    try {
      srcel = parse(nmsMsg.getContent());
    } catch (MessageTranslationException e) {
      // Well, maybe it is not XML.
      if (__log.isDebugEnabled())
        __log.debug("Exception parsing NMS message.", e);
      return Recognized.FALSE;
    }

    QName srcName = new QName(srcel.getNamespaceURI(), srcel.getLocalName());
    return WSDL11_W_MESSAGE.equals(srcName) ? Recognized.TRUE : Recognized.FALSE;
  }

 
  /**
   * 
   * Convert PXE normalized message to JBI normalized "WSDL 1.1 Wrapper" format.
   */
  public void toNMS(NormalizedMessage nmsMsg, Message pxeMsg,
      javax.wsdl.Message msgdef) throws MessagingException {
    if (msgdef == null)
      throw new NullPointerException("Null MessageDef");
    if (pxeMsg == null)
      throw new NullPointerException("Null src.");

    if (__log.isTraceEnabled())
      __log.trace("toNMS(pxeMsg=" + pxeMsg + ")");

    Element srcMsgEl = pxeMsg.getMessage();
    Document doc = newDocument();
    Element dstMsgEl = doc.createElementNS(URI_WSDL11_WRAPPER, "message");
    doc.appendChild(dstMsgEl);

    // The JBI NMS required attributes.
    dstMsgEl.setAttributeNS(DOMUtils.NS_URI_XMLNS, "xmlns:msgns", pxeMsg
        .getType().getNamespaceURI());
    dstMsgEl.setAttribute("version", "1.0");
    dstMsgEl.setAttribute("type", "msgns:" + pxeMsg.getType().getLocalPart());

    // The parts (hopefully they are in order, as NMS does not identify them!)
    Element srcPartEl = DOMUtils.getFirstChildElement(srcMsgEl);
    while (srcPartEl != null) {
      Element dstPartEl = doc.createElementNS(URI_WSDL11_WRAPPER, "part");
      dstMsgEl.appendChild(dstPartEl);
      Node srccontent = srcPartEl.getFirstChild();
      while (srccontent != null) {
        dstPartEl.appendChild(doc.importNode(srccontent, true));
        srccontent = srccontent.getNextSibling();
      }
      srcPartEl = DOMUtils.getNextSiblingElement(srcPartEl);
    }

    nmsMsg.setContent(new DOMSource(doc));

  }

  @SuppressWarnings("unchecked")
  public void toPXE(Message dest, NormalizedMessage src,
      javax.wsdl.Message msgdef) throws MessageTranslationException {
    if (msgdef == null)
      throw new NullPointerException("Null MessageDef");
    if (dest == null)
      throw new NullPointerException("Null dest.");
    if (src == null)
      throw new NullPointerException("Null src.");

    if (__log.isTraceEnabled())
      __log.trace("convertMessage<toPXE>(dest=" + dest + ",src=" + src);

    Element srcel = parse(src.getContent());

    Document pxemsgdoc = newDocument();
    Element pxemsg = pxemsgdoc.createElement("message");
    pxemsgdoc.appendChild(pxemsg);

    List<Part> expectedParts = msgdef.getOrderedParts(null);

    Element srcpart = DOMUtils.getFirstChildElement(srcel);
    for (int i = 0; i < expectedParts.size(); ++i) {
      Part pdef = expectedParts.get(i);
      Element p = pxemsgdoc.createElement(pdef.getName());
      pxemsg.appendChild(p);
      if (srcpart != null) {
        NodeList nl = srcpart.getChildNodes();
        for (int j = 0; j < nl.getLength(); ++j)
          p.appendChild(pxemsgdoc.importNode(nl.item(j), true));
        srcpart = DOMUtils.getNextSiblingElement(srcpart);
      } else {
        __log.error("Improperly formatted message, missing part: "
            + pdef.getName());
      }
    }

    dest.setMessage(pxemsg);

  }

}
