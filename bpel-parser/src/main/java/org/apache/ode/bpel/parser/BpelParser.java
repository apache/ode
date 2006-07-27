/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.parser;

import org.apache.ode.bom.api.Process;
import org.apache.ode.sax.fsa.FsaHandler;
import org.apache.ode.utils.XMLParserUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.net.URL;

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
    LocalEntityResolver resolver = new LocalEntityResolver();
    resolver.register(BpelProcessBuilder.BPEL4WS_NS, getResource("/bpel4ws_1_1-fivesight.xsd"));
    resolver.register(BpelProcessBuilder.WSBPEL2_0_NS, getResource("/wsbpel_main-draft-Apr-29-2006.xsd"));
    resolver.register(XML, getResource("/xml.xsd"));
    resolver.register(WSDL,getResource("/wsdl.xsd"));
    resolver.register(WSBPEL2_PLINK, getResource("/wsbpel_plinkType-draft-Apr-29-2006.xsd"));
    _xr.setEntityResolver(resolver);
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
