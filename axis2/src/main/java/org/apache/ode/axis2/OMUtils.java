package org.apache.ode.axis2;

import org.apache.ode.utils.DOMUtils;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.w3c.dom.Element;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Utility methods to convert from/to AxiOM and DOM.
 */
public class OMUtils {

  public static Element toDOM(OMElement element) throws AxisFault {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      element.serialize(baos);
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      return DOMUtils.parse(bais).getDocumentElement();
    } catch (Exception e) {
      throw new AxisFault("Unable to read Axis input messag.e", e);
    }
  }

  public static OMElement toOM(Element element) throws AxisFault {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      DOMUtils.serialize(element, baos);
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(bais);
      StAXOMBuilder builder = new StAXOMBuilder(parser);
      return builder.getDocumentElement();
    } catch (Exception e) {
      throw new AxisFault("Unable to read Axis input messag.e", e);
    }
  }

}
