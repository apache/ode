/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.httpsoap;

import com.fs.pxe.ra.PxeConnection;
import com.fs.pxe.ra.PxeConnectionFactory;
import com.fs.pxe.ra.PxeManagedConnectionFactory;
import com.fs.pxe.sfwk.spi.ServiceProviderException;
import com.fs.utils.msg.MessageBundle;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <p>
 * A servlet for routing SOAP-over-HTTP requests to a SOAP service provider
 * deployed in a PXE container.
 * This servlet assumes that any invocation contains a message destined for a
 * service deployed in a PXE domain.
 * The servlet uses the URI of the request to determine which PXE system
 * and service should receive the message, and always delivers the message
 * using the {@link com.fs.pxe.httpsoap.HttpSoapInteraction} interaction interface.
 * </p>
 *
 * <p>This servlet is meant to illustrate how a SOAP protocol stack may be
 * integrated with the PXE domain; high-level features such as message
 * authentication, encryption, and WS-XXX support are out of scope.
 * </p>
 *
 * <p>This servlet uses the following Servlet Init Parameters:
 * <ul>
 * <li><code>ConnectionFactoryName</code> - JNDI name of the  {@link PxeConnectionFactory}
 * that should be used to communicate with the PXE domain.
 * </li>
 * <li><code>ConnectionProperties</code> - Semicolon-sperated list of configuration
 * properties in <code>prop=value</code> used to configure the {@link PxeManagedConnectionFactory}
 * instance when the <code>ConnectionFactoryName</code> property is not specified.
 * </li>
 * <li><code>InvokeTimeoutMs</code> - Maximum amount of time (in ms) to wait for a request-response invocation
 * to complete.
 * </li>
 * </ul>
 * </p>
 * 
 * <p>
 *   An optional 'timeout' parameter may be included as a post parameter or query string to
 *   determine the wait time for a response; otherwise, the timeout configured with the 
 *   servlet will be used.
 * </p>
 */
public class HttpSoapServlet extends HttpServlet {
  /** Class-level logger. */
  private static final Log __log = LogFactory.getLog(HttpSoapServlet.class);

  private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

  /** Name of servlet property with the JNDI name of the {@link com.fs.pxe.ra.PxeConnectionFactory} */
  public static final String PROP_SPCF_CONNECTION_JNDI_NAME = "ConnectionFactoryName";

  /** Properties used to configure {@link com.fs.pxe.ra.PxeManagedConnectionFactory}. */
  public static final String PROP_SPCF_CONNECTION_PROPS = "ConnectionProperties";

  /** Name of servlet property containing the timeout in ms. */
  public static final String PROP_TIMEOUT = "InvokeTimeoutMs";

  /** The property containing the Service Provider's URI. */
  public static final String PROP_SPURI = "ServiceProviderURI";

  /** Connection timeout (how long are we going to wait for a response) */
  private long _timeout = 2 * 60 * 1000; // 2 minutes is default.

  private String _serviceProviderURI = null;

  private PxeConnectionFactory _spCF;

  /**
   * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
   */
  public void init(ServletConfig servletConfig)
            throws ServletException {
    super.init(servletConfig);

    String spcfJndiName = servletConfig.getInitParameter(PROP_SPCF_CONNECTION_JNDI_NAME);
    __log.debug(__msgs.msgServletInitPropertyValue(PROP_SPCF_CONNECTION_JNDI_NAME, spcfJndiName));
    String spcfCFProps = servletConfig.getInitParameter(PROP_SPCF_CONNECTION_PROPS);
    __log.debug(__msgs.msgServletInitPropertyValue(PROP_SPCF_CONNECTION_PROPS, spcfCFProps));
    String timeout = servletConfig.getInitParameter(PROP_TIMEOUT);
    __log.debug(__msgs.msgServletInitPropertyValue(PROP_TIMEOUT, timeout));
    _serviceProviderURI = servletConfig.getInitParameter(PROP_SPURI);
    __log.debug(__msgs.msgServletInitPropertyValue(PROP_SPURI, _serviceProviderURI));

    if(_serviceProviderURI == null){
      String msg = __msgs.msgServletInitPropertyNotSetOrInvalid(PROP_SPURI,null);
      __log.error(msg);
      throw new ServletException(msg);
    }

    if (timeout != null) {
      try {
        long to = Long.parseLong(timeout.trim());
        _timeout = to;
      } catch (NumberFormatException nfe) {
        String msg = __msgs.msgServletInitPropertyNotSetOrInvalid(PROP_TIMEOUT,timeout.trim());
        __log.error(msg);
        throw new ServletException(msg, nfe);
      }
    }

    if (spcfJndiName != null) {
      InitialContext ctx;
      try {
        ctx = new InitialContext();
      } catch (NamingException e) {
        String msg = __msgs.msgJndiInitialContextError();
        __log.error(msg, e);
        throw new ServletException(msg, e);
      }

      try {
        _spCF = (PxeConnectionFactory)ctx.lookup(spcfJndiName);
      } catch (NameNotFoundException e) {
        String msg = __msgs.msgPxeConnectionFactoryNotFound(spcfJndiName);
        __log.error(msg, e);
        throw new ServletException(msg, e);
      } catch (Exception ex) {
        String msg = __msgs.msgPxeConnectionFactoryError(spcfJndiName);
        __log.error(msg, ex);
        throw new ServletException(msg, ex);
      }
    } else if (spcfCFProps != null) {
      PxeManagedConnectionFactory mcf = new PxeManagedConnectionFactory();
      StringTokenizer stok = new StringTokenizer(spcfCFProps,";",false);
      while (stok.hasMoreTokens()) {
        String next = stok.nextToken();
        int equals = next.indexOf('=');
        try {
          if (equals == -1)
            mcf.setProperty(next, null);
          else
            mcf.setProperty(next.substring(0,equals), next.substring(equals+1));
        } catch (ResourceException ex) {
          String msg = __msgs.msgPxeConnectionFactoryError(spcfJndiName);
          __log.error(msg, ex);
          throw new ServletException(msg, ex);
        }
      }

      try {
        _spCF = (PxeConnectionFactory) mcf.createConnectionFactory();
      } catch (Exception ex) {
        String msg = __msgs.msgPxeConnectionFactoryError(spcfJndiName);
        __log.error(msg, ex);
        throw new ServletException(msg, ex);
      }
    } else {
      String msg = __msgs.msgServletInitPropertyNotSetOrInvalid(PROP_SPCF_CONNECTION_JNDI_NAME,null);
      __log.error(msg);
      throw new ServletException(msg);
    }
  }

  protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
    if (httpServletRequest.equals("test")) {
      doTest(httpServletRequest, httpServletResponse);
    } else if (httpServletRequest.equals("wsdl")) {
      doWsdl(httpServletRequest, httpServletResponse);
    } else {
      httpServletResponse.sendError(405, __msgs.msgMethodNotSupported("GET"));
    }
  }

  protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {

    long timeout = _timeout;
    // allows specifying timeout in query string or http post parameter
    String timeoutParam = httpServletRequest.getParameter("timeout");
    if(timeoutParam != null){
    	try{
    		int seconds = Integer.parseInt(timeoutParam);
    		timeout = Math.max(0, seconds) * 1000;
    	}catch(Exception e){
    		__log.error("Error parsing timeout from query parameter: " + timeoutParam);
    	}
    }

    HttpSoapInteraction interaction = getInteraction();
    HttpSoapRequest request = new HttpSoapRequest("POST", httpServletRequest.getRequestURI(),httpServletRequest.getQueryString());
    copyHeaders(request, httpServletRequest);
    request.setPayload(httpServletRequest.getInputStream());
    HttpSoapResponse response;
    try {
      if(__log.isDebugEnabled()) {
         __log.debug("HTTP REQUEST " + request.getRequestUri());
      }
      response = interaction.handleHttpSoapRequest(request, timeout);
    } catch (ServiceProviderException e) {
      String msg= __msgs.msgPxeInteractionError(request.getRequestUri());
      __log.error(msg, e);
      // "Client" errors, get error 400, no SOAP fault message.
      httpServletResponse.sendError(400,msg);
      return;
    }

    httpServletResponse.setContentType(response.getContentType());

    if (response.isErrorResponse() && response.getPayload() == null) {
      httpServletResponse.sendError(response.getStatus(), response.getErrorText());
    }
    else {
      httpServletResponse.setStatus(response.getStatus());
      copyHeaders(httpServletResponse, response);
      response.writePayload(httpServletResponse.getOutputStream());
    }
  }

  private void doWsdl(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // TODO: provide WSDL for the service.
    response.setStatus(200);
    response.getOutputStream().close();
  }

  private void doTest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    // TODO: provide a TEST window for users.
  }


  /**
   * Copy headers from an {@link HttpServletRequest} object to our {@link HttpSoapRequest}
   * object.
   * @param dest destination
   * @param src source
   */
  private static void copyHeaders(HttpSoapRequest dest, HttpServletRequest src) {
    Enumeration en = src.getHeaderNames();

    while (en.hasMoreElements()) {
      String headerName = (java.lang.String)en.nextElement();
      String headerValue = src.getHeader(headerName);
     dest.setHeader(headerName,headerValue);
    }
  }

  /**
   * Copy headers from one of our {@link com.fs.pxe.httpsoap.HttpSoapResponse} objects to an {@link HttpServletResponse}
   * object.
   * @param dest destination
   * @param src source
   */
  private static void copyHeaders(HttpServletResponse dest, HttpSoapResponse src) {
    for (Iterator i = src.getHeaders().entrySet().iterator(); i.hasNext(); ) {
      Map.Entry me = (Map.Entry)i.next();
      String headerName = (String)me.getKey();
      String headerValue = (String)me.getValue();
      dest.setHeader(headerName,headerValue);
    }
  }


  private HttpSoapInteraction getInteraction() throws ServletException {
    PxeConnection conn;
    try {
      conn = (PxeConnection) _spCF.getConnection();
    } catch (ResourceException pce) {
      String errmsg = __msgs.msgPxeConnectionError(PROP_SPCF_CONNECTION_JNDI_NAME+ ", " + PROP_SPCF_CONNECTION_PROPS);
      __log.error(errmsg, pce);
      throw new ServletException(errmsg, pce);
    }

    HttpSoapInteraction invoke;
    try {
      invoke = (HttpSoapInteraction)conn.createServiceProviderSession(
              _serviceProviderURI, HttpSoapInteraction.class);
    } catch (Exception ex) {
      String errmsg = __msgs.msgPxeConnectionError(PROP_SPCF_CONNECTION_JNDI_NAME+ ", " + PROP_SPCF_CONNECTION_PROPS);
      __log.error(errmsg, ex);
      throw new ServletException(errmsg, ex);
    }

    return invoke;
  }
}
