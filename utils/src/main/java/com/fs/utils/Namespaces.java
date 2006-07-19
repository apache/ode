package com.fs.utils;

import javax.xml.namespace.QName;

/**
 * Constant class to centralize all namespaces declarations.
 */
public class Namespaces {

  public static final String INTALIO_SESSION_NS = "http://www.intalio.com/type/session";

  public static final String WS_ADDRESSING_NS = "http://www.w3.org/2005/08/addressing";

  public static final String WS_ADDRESSING_WSDL_NS = "http://www.w3.org/2006/05/addressing/wsdl";

  public static final String SOAP_NS = "http://schemas.xmlsoap.org/wsdl/soap/";

  public static final String WS_BPEL_20_NS = "http://schemas.xmlsoap.org/ws/2004/03/business-process/";

  public static final String WSDL_11 = "http://schemas.xmlsoap.org/wsdl/";

  public static final String WSDL_20 = "http://www.w3.org/2006/01/wsdl";

  public static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
  
  public static final String JBI_END_POINT_REFERENCE = "http://java.sun.com/jbi/end-point-reference";  

  public static final QName WS_ADDRESSING_ENDPOINT = new QName(WS_ADDRESSING_NS, "EndpointReference");

}
