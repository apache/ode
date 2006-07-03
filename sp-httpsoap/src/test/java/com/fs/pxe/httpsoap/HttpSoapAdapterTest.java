/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.httpsoap;

import com.fs.pxe.ra.PxeConnection;
import com.fs.pxe.ra.PxeConnectionFactory;

public class HttpSoapAdapterTest extends HttpSoapAdapterTCase {
  protected PxeConnectionFactory pcf;

  public void setUp() throws Exception {
    super.setUp();

//    pcf = _binding.getPxeConnectionFactory();
    conn = (PxeConnection) pcf.getConnection();
    interaction = (HttpSoapInteraction) conn.createServiceProviderSession(
            SP_URI, HttpSoapInteraction.class);

  }

  public void testBadGet() throws Exception {
    HttpSoapRequest request1 = new HttpSoapRequest("GET", REQUEST_URI, "");
    HttpSoapResponse response = interaction.handleHttpSoapRequest(request1, 1000);
    assertNotNull(response);
    assertTrue(response.getStatus() >= 400 && response.getStatus() < 500);
  }

  public void testBadAction() throws Exception {
    HttpSoapRequest request1 = new HttpSoapRequest("FOO", REQUEST_URI, "");
    HttpSoapResponse response = interaction.handleHttpSoapRequest(request1, 1000);
    assertNotNull(response);
    assertTrue(response.getStatus() >= 400 && response.getStatus() < 500);
  }

  public void testBadNullPost() throws Exception {
    HttpSoapRequest request1 = new HttpSoapRequest("POST", REQUEST_URI, "");
    HttpSoapResponse response = interaction.handleHttpSoapRequest(request1, 1000);
    assertNotNull(response);
    assertTrue(response.getStatus() >= 400 && response.getStatus() < 500);
  }

  public void testBadEmptyPost() throws Exception {
    HttpSoapRequest request1 = new HttpSoapRequest("POST", REQUEST_URI, "");
    request1.setHeader("Content-Type", "text/xml");
    request1.setPayload("".getBytes());
    HttpSoapResponse response = interaction.handleHttpSoapRequest(request1, 1000);
    assertNotNull(response);
    assertEquals(500, response.getStatus());
  }

  public void testBadURI() throws Exception {
    HttpSoapRequest request1 = new HttpSoapRequest("POST", "uri:notvalid", "");
    request1.setHeader("Content-Type", "text/xml");
    request1.setPayload(getClass().getResourceAsStream("testRequest.soap"));
    HttpSoapResponse response = interaction.handleHttpSoapRequest(request1, 1000000);
    assertEquals(404, response.getStatus());
  }

  public void testGoodPost() throws Exception {
    HttpSoapRequest request1 = new HttpSoapRequest("POST", REQUEST_URI, "");
    request1.setHeader("Content-Type", "text/xml");
    request1.setPayload(getClass().getResourceAsStream("testRequest.soap"));
    HttpSoapResponse response = interaction.handleHttpSoapRequest(request1, 1000000);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
  }

  public void testGoodPostMT1Conn() throws Exception {
    final HttpSoapRequest request1 = new HttpSoapRequest("POST", REQUEST_URI, "");
    request1.setHeader("Content-Type", "text/xml");
    request1.setPayload(getClass().getResourceAsStream("testRequest.soap"));
    Thread threads[] = new Thread[40];
    for (int i = 0 ; i < threads.length; ++i) {
      threads[i] = new Thread("MT#" +i) {
        public void run() {
          for (int j = 0 ; j < 10 ; ++j) {
            HttpSoapResponse response;
            try {
              response = interaction.handleHttpSoapRequest(request1, 1000000);
            } catch (Exception ex) {
              ex.printStackTrace();
              fail(ex.toString());
              throw new AssertionError("unreachable");
            }
            assertNotNull(response);
            assertEquals(200, response.getStatus());
          }
        }
      };
    }

    for (int i = 0 ; i < threads.length; ++i) {
      threads[i].start();
    }

    for (int i = 0 ; i < threads.length; ++i) {
      threads[i].join();
    }

  }

  public void testGoodPostMTMConn() throws Exception {
    final HttpSoapRequest request1 = new HttpSoapRequest("POST", REQUEST_URI, "");
    request1.setHeader("Content-Type", "text/xml");
    request1.setPayload(getClass().getResourceAsStream("testRequest.soap"));
    Thread threads[] = new Thread[40];
    for (int i = 0 ; i < threads.length; ++i) {
      threads[i] = new Thread("MT#" +i) {
        HttpSoapInteraction xi = (HttpSoapInteraction) conn.createServiceProviderSession(
                SP_URI, HttpSoapInteraction.class);
        public void run() {
          for (int j = 0 ; j < 10 ; ++j) {
            HttpSoapResponse response;
            try {
              response = xi.handleHttpSoapRequest(request1, 1000000);
            } catch (Exception ex) {
              ex.printStackTrace();
              fail(ex.toString());
              throw new AssertionError("unreachable");
            }
            assertNotNull(response);
            assertEquals(200, response.getStatus());
          }
        }
      };
    }

    for (int i = 0 ; i < threads.length; ++i) {
      threads[i].start();
    }

    for (int i = 0 ; i < threads.length; ++i) {
      threads[i].join();
    }

  }
	
}
