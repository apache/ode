/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.parser;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.apache.ode.bom.api.Process;
import org.apache.ode.sax.fsa.FsaHandler;
import org.apache.ode.utils.XMLParserUtils;

class BpelParser {
  
  private static final Log __log = LogFactory.getLog(BpelParser.class);

  public static final String WSDL = "http://schemas.xmlsoap.org/wsdl/";

  public static final String WSBPEL2_PLINK = "http://schemas.xmlsoap.org/ws/2004/03/partner-link/";

  public static final String XML = "http://www.w3.org/2001/xml.xsd";

  private XMLReader _xr;
  private RootFSA _b;
  private BpelParseErrorHandler _eh;
  
  
  public BpelParser() {
    _b = new RootFSA();
    _xr = XMLParserUtils.getXMLReader();
    try {
      XMLParserUtils.addExternalSchemaURL(_xr,BpelProcessBuilder.BPEL4WS_NS,
          getResource("/bpel4ws_1_1-fivesight.xsd").toExternalForm());
      XMLParserUtils.addExternalSchemaURL(_xr,BpelProcessBuilder.WSBPEL2_0_NS,
          getResource("/wsbpel_main-draft-Sep-06-2005.xsd").toExternalForm());
      XMLParserUtils.addExternalSchemaURL(_xr,XML,
          getResource("/xml.xsd").toExternalForm());
      XMLParserUtils.addExternalSchemaURL(_xr,WSDL,
          getResource("/wsdl.xsd").toExternalForm());
      XMLParserUtils.addExternalSchemaURL(_xr,WSBPEL2_PLINK,
          getResource("/wsbpel_plinkType-draft-Sep-06-2005.xsd").toExternalForm());
    } catch (SAXException se) {
      // complain but let it slide; who knows -- we might not be using Xerces...
      __log.error("Unable to configure XMLReader ("  + _xr.getClass().getName() +
          ") for schema validation.", se);
    }
    _xr.setContentHandler(new FsaHandler(_b));
  }
  
  private URL getResource(String name) {
    URL url = BpelParser.class.getResource( name );
    if ( url == null ) {
      throw new RuntimeException( "Unable to load resource: " + name );
    }
    return url;
  }

  public void setBpelParseErrorHandler(BpelParseErrorHandler bpeh) {
    _eh = bpeh;
  }
  
  public BpelParseErrorHandler getBpelParseErrorHandler() {
    return _eh;
  }
  
  public Process parse(InputSource is) throws SAXException, IOException {
    _b.reset();
    if (_eh != null) {
      _xr.setErrorHandler(_eh);
      _b.setParseContext(_eh);
    }
    _xr.parse(is);
    return _b.getProcess();
  }
  
}
