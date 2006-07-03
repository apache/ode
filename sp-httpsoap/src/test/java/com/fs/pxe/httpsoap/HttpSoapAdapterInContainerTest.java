/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.httpsoap;

import javax.xml.soap.*;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.util.InetAddrPort;

public class HttpSoapAdapterInContainerTest extends HttpSoapAdapterTCase {
  private SOAPConnectionFactory scf;
  private MessageFactory mf;

  public void setUp() throws Exception {
    super.setUp();
    HttpServer httpServer = new HttpServer();
    httpServer.addListener(new InetAddrPort(8099));

    HttpContext context = httpServer.getContext("/");
    ServletHandler handler= new ServletHandler();
    ServletHolder servletHolder = handler.addServlet("Test","/*",
                       HttpSoapServlet.class.getName());
    context.addHandler(handler);
    servletHolder.setInitParameter(HttpSoapServlet.PROP_SPURI, SP_URI);
//    servletHolder.setInitParameter(HttpSoapServlet.PROP_SPCF_CONNECTION_PROPS, "URL=" +
//            "rmi://localhost:1099/" + _binding.getDomainNode().getDomainId());
    httpServer.start();

    scf = SOAPConnectionFactory.newInstance();
    mf = MessageFactory.newInstance();

  }

  public void testGoodPost() throws Exception {
    SOAPConnection soapConn = scf.createConnection();
    MimeHeaders mh = new MimeHeaders();
    mh.setHeader("Content-Type", "text/xml");
    SOAPMessage msg = mf.createMessage(mh, goodMessage.openStream());
    SOAPMessage responseMsg = soapConn.call(msg, "http://localhost:8099/baz");
    assertNotNull(responseMsg);
    responseMsg.writeTo(System.err);
  }
}
