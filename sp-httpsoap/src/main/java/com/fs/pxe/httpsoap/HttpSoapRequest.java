/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.httpsoap;

import com.fs.utils.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

/**
 * Generalized, serializable representation of an HTTP-SOAP request.
 */
public class HttpSoapRequest implements Serializable {
  private String _action;
  private String _requestUri;
  private String _queryString;
  private byte[] _payload;
  private Properties _httpHeaders = new Properties();

  public HttpSoapRequest(String action, String requestURI, String queryString) {
    _action = action;
    _requestUri = requestURI;
    _queryString = queryString;
  }

  /** Get the action, i.e. "GET", "PUT", etc... */
  public String getAction() {
    return _action;
  }

  /** Set an HTTP header. */
  public void setHeader(String headerName, String value) {
    _httpHeaders.setProperty(headerName, value);
  }

  /** Get an HTTP header. */
  public String getHeader(String headerName) {
    return _httpHeaders.getProperty(headerName);
  }

  /** Get the (full) request URI. */
  public String getRequestUri() { return _requestUri; }

  /** Get the query string portion of the request URI. */
  public String getQueryString() { return _queryString; }

  /* Get the SOAP/MIME payload bytes. */
  public byte[] getPaload() { return _payload; }

  /**
   * Set the payload (SOAP or MIME) from a byte array.
   * @param bytes payload bytes
   */
  public void setPayload(byte[] bytes) {
    _payload = bytes;
  }

  /**
   * Set the payload using an input byte stream.
   * @param inputStream input byte stream
   * @throws IOException in case of I/O errors
   */
  public void setPayload(InputStream inputStream) throws IOException {
    this.setPayload(StreamUtils.read(inputStream));
  }

  public Map getHeaders() {
    return _httpHeaders;
  }

  public InputStream getPayloadInputStream() {
    return _payload == null ? null : new ByteArrayInputStream(_payload);
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("{HttpSoapRequest ");
    buf.append(_action);
    buf.append(' ');
    buf.append(_requestUri);
    buf.append(" / QUERY=");
    buf.append(_queryString);
    buf.append(", headers=");
    buf.append(_httpHeaders);
    if (_payload != null) {
      buf.append(", payload=");
      buf.append(new String(_payload));
    }
    buf.append('}');
    return buf.toString();
  }

}


