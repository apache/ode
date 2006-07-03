/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.httpsoap;

import com.fs.pxe.sfwk.spi.ServiceProviderException;

import java.io.IOException;


/**
 * <p>
 * HTTP-SOAP Adapter interaction interface. This interface is used
 * to integrate the HTTP SOAP Adapter into an SOAP stack. Basically,
 * the SOAP stack would obtain an instance of this interaction
 * interface through JCA and then use the
 * {@link #handleHttpSoapRequest(HttpSoapRequest, long)}
 * method to notify the HTTP SOAP Adapter of incoming SOAP requests.
 * The return value of this method is the SOAP response.
 * </p>
 *
 * <p>
 * A typical usage scenario is shown in the source to {@link HttpSoapServlet}.
 * </p>
 *
 */
public interface HttpSoapInteraction {

  /**
   * Inform the HTTP-SOAP adapter that a SOAP request has arrived.
   *
   * @param request the HTTP-SOAP request
   * @param timeout if two-way call, maximum time in ms to block
   *
   * @return HTTP-SOAP response or <code>null</code>, if one-way service
   *
   * @throws ServiceProviderException in case of serious provider error
   */
  public HttpSoapResponse handleHttpSoapRequest(HttpSoapRequest request, long timeout)
                         throws ServiceProviderException, IOException;

}
