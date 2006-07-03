/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.soap.mapping;

import javax.xml.namespace.QName;

public abstract class Soap11Constants {
  public static final String NS_SOAP11 = "http://schemas.xmlsoap.org/soap/envelope/";
  public static final QName QNAME_ENVELOPE = new QName(NS_SOAP11, "Envelope", "soapenv");
  public static final QName QNAME_HEADER = new QName(NS_SOAP11, "Header", "soapenv");
  public static final QName QNAME_BODY = new QName(NS_SOAP11, "Body", "soapenv");
  public static final QName QNAME_FAULT = new QName(NS_SOAP11, "Fault", "soapenv");

  private Soap11Constants() {
  }
}
