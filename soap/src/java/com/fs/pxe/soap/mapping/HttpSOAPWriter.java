/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.soap.mapping;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Trivial extension of the {@link SOAPWriter} that understands the HTTP-specific
 * binding extensions.
 */
public class HttpSOAPWriter extends SOAPWriter {
  private static final String HTTP_SOAP_TRANSPORT_URI = "http://schemas.xmlsoap.org/soap/http";

  /** The value of the <code>SOAPAction</code> header. */
  private String _soapAction;

  public HttpSOAPWriter(SoapBindingModel soapBindingModel, SoapOperationBindingModel soapOperationBindingModel, boolean request)
          throws SoapFormatException
  {
    super(soapOperationBindingModel, request);

    if (!soapBindingModel.getTransportURI().equals(HTTP_SOAP_TRANSPORT_URI)) {
      throw new SoapFormatException("Non-HTTP SOAP binding!");
    }

    _soapAction = soapOperationBindingModel.getSOAPAction() == null ? "" : soapOperationBindingModel.getSOAPAction();
  }

  public void write(Document destMessage, Map<String, String> destHeaders, Map<String, Element> srcParts) throws SoapFormatException {
    super.write(destMessage, srcParts);
    destHeaders.put("SOAPAction", _soapAction);
  }

}
