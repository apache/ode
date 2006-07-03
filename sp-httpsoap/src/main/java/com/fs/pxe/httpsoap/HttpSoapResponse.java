/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.httpsoap;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Properties;

/**
 * Generalized, serializable representation of an HTTP-SOAP response.
 */
public class HttpSoapResponse implements Serializable {
  private int _status;
  private String _errText;
  private Properties _httpHeaders = new Properties();

  private byte[] _payload;

  public boolean isErrorResponse() {
    return _status >= 400;
  }

  public String getErrorText() {
    return _errText;
  }

  public void writePayload(OutputStream os) throws IOException {
    if (_payload != null) {
      os.write(_payload);
    }
    os.close();
  }

  public int getStatus() {
    return _status;
  }

  public Properties getHeaders() {
    return _httpHeaders;
  }

  public void setErrorText(String errText) {
    _errText = errText;
  }


  public void setHeader(String name, String value) {
    _httpHeaders.put(name, value);
  }

  public void setStatus(int status) {
    _status = status;
  }

  public void setPayload(byte[] bytes) {
    _payload = bytes;
  }


  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("{HttpSoapResponse status=");
    buf.append(_status);
    if (_errText != null) {
      buf.append(" \"");
      buf.append(_errText);
      buf.append('"');
    }
    buf.append(", headers=");
    buf.append(_httpHeaders);
    if (_payload != null) {
      buf.append(", payload=");
      buf.append(new String(_payload));
    }
    buf.append('}');
    return buf.toString();
  }

  public byte[] getPayload() {
    return _payload;
  }

  public String getContentType() {
    return "text/xml";
  }
}
