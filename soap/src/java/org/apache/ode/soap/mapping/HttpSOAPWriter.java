/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.soap.mapping;

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
