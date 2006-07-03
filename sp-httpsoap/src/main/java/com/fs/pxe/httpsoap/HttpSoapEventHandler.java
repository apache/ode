/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.httpsoap;

import com.fs.pxe.sfwk.spi.ServiceProviderException;
import com.fs.pxe.soap.mapping.SoapFormatException;
import com.fs.pxe.soap.mapping.SoapMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;

/**
 * Utility methods for dealing with SOAP-over-HTTP.
 */
class HttpSoapEventHandler {
  static final int HTTPERR_BAD_REQUEST = 400;
  static final int HTTPERR_METHOD_NOT_ALLOWED = 405;
  static final int HTTPERR_UNSUPPORTED_MEDIA = 415;
  static final int HTTPERR_SERVER_ERROR = 500;

  static final String FAULT_NS = "http://schemas.xmlsoap.org/soap/envelope/";
  static final String FAULT_SERVER = "Server";
  static final String FAULT_CLIENT = "Client";

  protected final Log _log;

  protected HttpSoapEventHandler(Log log) throws ServiceProviderException {
    _log = log;
  }

  protected void createFault(HttpSoapResponse response, String faultCode, String faultString, Exception ex) {

    response.setStatus(HTTPERR_SERVER_ERROR);
    response.setErrorText(faultString);
    SoapMessage msg;
    try {
      msg = createFaultSoapMessage(faultCode, faultString, ex);
    } catch (SoapFormatException soapEx) {
      _log.fatal("Unexepcted error (createFault#1)! Contact FiveSight Technical Support!", soapEx);
      response.setStatus(555);
      response.setPayload(null);
      return;
    }

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      msg.writeTo(bos);
      bos.close();
      response.setPayload(bos.toByteArray());
    } catch (IOException ioex) {
      _log.fatal("Unexpected error (createFault#2). Contact FiveSight Technical Support!", ioex);
      response.setStatus(555);
      response.setPayload(null);
    }
  }

  protected SoapMessage createFaultSoapMessage(String faultCode, String faultString, Exception ex)
          throws SoapFormatException
  {
    SoapMessage soapMessage = new SoapMessage(faultCode, faultString, getClass().getName());
    return soapMessage;
  }
}
