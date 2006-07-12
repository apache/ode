package com.fs.pxe.axis;

import com.fs.utils.stl.CollectionsX;
import com.fs.utils.DOMUtils;

import javax.wsdl.Binding;
import javax.wsdl.Port;
import javax.wsdl.Definition;
import javax.wsdl.Service;
import javax.wsdl.Part;
import javax.wsdl.Operation;
import javax.wsdl.Message;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Utility class to handle SOAP messages wrapping/unwrapping depending
 * on binding style Document vs. RPC.
 */
public class SOAPUtils {

  private static final Messages __msgs = Messages.getMessages(Messages.class);

  private static final Log __log = LogFactory.getLog(SOAPUtils.class);

  public static Element wrap(Element message, Definition def, QName serviceName, Operation op, Message msgDef) throws AxisFault {
    Service serviceDef = def.getService(serviceName);
    boolean isRPC = isRPC(serviceDef);
    Document doc = DOMUtils.newDocument();
    if (isRPC) {
      Element responseRoot = doc.createElementNS(null, op.getName());
      doc.appendChild(responseRoot);

      Element srcPartEl = DOMUtils.getFirstChildElement(message);
      while (srcPartEl != null) {
        responseRoot.appendChild(doc.importNode(srcPartEl, true));
        srcPartEl = DOMUtils.getNextSiblingElement(srcPartEl);
      }
    } else {
      // Extracting element name: parts can't be a type as per WS-BP
      QName elmtName = ((Part)msgDef.getParts().get(0)).getElementName();
      Element responseRoot = doc.createElementNS(elmtName.getNamespaceURI(), elmtName.getLocalPart());
      // Message style has only one part, directly included in the body.
      doc.appendChild(responseRoot);
    }
    return doc.getDocumentElement();
  }

  public static Element unwrap(Element bodyElmt, Definition def, Message msgDef, QName serviceName) throws AxisFault {
    Service serviceDef = def.getService(serviceName);
    boolean isRPC = isRPC(serviceDef);
    if (isRPC) {
      // In RPC the body element is the operation name, wrapping parts. Just checking
      // the parts are alright.
      Document doc = DOMUtils.newDocument();
      Element msgElmt = doc.createElement("message");
      doc.appendChild(msgElmt);
      copyParts(bodyElmt, msgElmt, msgDef);
      return msgElmt;
    } else {
      // In document style the body element is the unique part. Wrapping is necessary.
      Document doc = DOMUtils.newDocument();
      Element msgElmt = doc.createElement("message");
      doc.appendChild(msgElmt);
      // Just making sure the part has no namespace
      Element destPart = doc.createElement(bodyElmt.getLocalName());
      msgElmt.appendChild(destPart);
      destPart.appendChild(doc.importNode(DOMUtils.getFirstChildElement(bodyElmt), true));
      return msgElmt;
    }
  }

  private static void copyParts(Element source, Element target, javax.wsdl.Message msgdef) {
    List<Part> expectedParts = msgdef.getOrderedParts(null);

    Element srcpart = DOMUtils.getFirstChildElement(source);
    for (Part pdef : expectedParts) {
      Element p = target.getOwnerDocument().createElement(pdef.getName());
      target.appendChild(p);
      if (srcpart != null) {
        NodeList nl = srcpart.getChildNodes();
        for (int j = 0; j < nl.getLength(); ++j)
          p.appendChild(target.getOwnerDocument().importNode(nl.item(j), true));
        srcpart = DOMUtils.getNextSiblingElement(srcpart);
      } else {
        __log.error("Improperly formatted message, missing part: " + pdef.getName());
      }
    }

  }

  private static boolean isRPC(Service serviceDef) throws AxisFault {
    for (Object oport : serviceDef.getPorts().values()) {
      Port wsdlPort = (Port)oport;
      Binding binding = wsdlPort.getBinding();

      if (binding == null) continue;

      Collection soapBindings = CollectionsX.filter(binding.getExtensibilityElements(), SOAPBinding.class);
      if (soapBindings.isEmpty()) continue;
      else if (soapBindings.size() > 1) {
        throw new AxisFault(__msgs.msgMultipleSoapBindingsForPort(wsdlPort.getName()));
      }

      SOAPBinding soapBinding = (SOAPBinding) soapBindings.iterator().next();
      String style  = soapBinding.getStyle();
      return style != null && style.equals("rpc");
    }
    throw new AxisFault(__msgs.msgNoBindingForService(serviceDef.getQName()));
  }
}
