package com.fs.pxe.bpel.iapi;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

/**
 * Encapsulation of an end-point reference. Implementation of this interface
 * is provided by the integration layer. The implementing class <em>must</em>
 * provide an implementation of the 
 * {@link java.lang.Object#equals(java.lang.Object)} method that returns
 * <code>true</code> if and only if the EPRs are equivalent.
 */
public interface EndpointReference {

  public static final QName SERVICE_REF_QNAME =
    new QName("http://schemas.xmlsoap.org/ws/2004/03/business-process/", "service-ref");

  /**
   * Convert the EPR to an XML representation. The XML
   * structure is up to the integration layer. This method is
   * used by the BPEL engine to persist EPR references in the 
   * database. 
   * TODO: avoid using DOM
   * @return destination for the generated XML
   */
  Document toXML(); 
}
