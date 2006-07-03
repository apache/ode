/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.compiler;

import com.fs.pxe.bom.wsdl.Definition4BPEL;
import com.fs.pxe.bpel.capi.CompilationException;
import com.fs.pxe.bpel.capi.CompilationMessage;
import com.fs.pxe.bpel.capi.CompileListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;

import junit.framework.TestCase;

import org.xml.sax.InputSource;


/**
 * JUnit {@link TestCase} of static-analysis checking in the PXE BPEL compiler.
 * These test cases are intended to be run as part of a suite. Each test case
 * instance is used to test a particular detectable error condition.
 */
class StaticCheckTCase extends TestCase implements CompileListener, WsdlFinder {

  private int idx = 0;
  private String name;
  private BpelC _compiler;
  private List<CompilationMessage> _errors = new ArrayList<CompilationMessage>();
  private Set<InputStream> _streams = new HashSet<InputStream>();
  private URL _bpelURL;
  private String _wsdlURI;

  StaticCheckTCase(String name) {
    super(name);
    this.name = name;
  }

  public StaticCheckTCase(String name, int idx) {
    super(name + idx);
    this.name = name;
    this.idx = idx;
  }

  protected void setUp() throws Exception {
    super.setUp();
    _compiler = BpelC.newBpelCompiler();
    _compiler.setWsdlFinder(this);
    _compiler.setCompileListener(this);
    _errors.clear();

    String baseFname = name + ((idx > 0)
                               ? Integer.toString(idx)
                               : "");
    _bpelURL = getClass().getResource(baseFname + ".bpel");
    _wsdlURI = (baseFname + ".wsdl");

    _compiler.setProcessWSDL(new URI(_wsdlURI));
  }

  protected void tearDown() throws Exception {
    for (InputStream s: _streams) {
      s.close();
    }
    _streams.clear();
    super.tearDown();
  }

  public void runTest() throws Exception {
    try {
      _compiler.compile(_bpelURL);
      fail("Expected compilation exception.");
    } catch (CompilationException ce) {
      _errors.add(ce.getCompilationMessage());
    }

    assertTrue(_errors.size()!=0);

    boolean found = false;
    for (Iterator<CompilationMessage> i = _errors.iterator(); i.hasNext(); ) {
      CompilationMessage msg = i.next();
      if (msg.severity == CompilationMessage.ERROR && msg.code.equals(name)) {
        found = true;
      }
    }

    assertTrue("Expected error \"" + name + "\" not found in " + _errors, found);
  }

  public void onCompilationMessage(CompilationMessage compilationMessage) {
    _errors.add(compilationMessage);
  }

  public void setBaseURI(URI u) {
  }
  
  public Definition4BPEL loadDefinition(WSDLReader f, URI uri) throws WSDLException {
    InputStream is;
    try {
      is = getClass().getResource(uri.toASCIIString()).openStream();
    } catch (IOException ioex) {
      throw new WSDLException(WSDLException.INVALID_WSDL,uri.toASCIIString());
    }
    
    try {
      return (Definition4BPEL) f.readWSDL(null, new InputSource(is));
    } finally {
      try {
        is.close();
      } catch (Exception ie) {
          throw new RuntimeException(ie);
      }
    }
  }

	public InputStream openResource(URI uri) throws MalformedURLException, IOException {
		try {
			InputStream is = getClass().getResource(uri.toASCIIString()).openStream();
      _streams.add(is);
      return is;
		} catch (NullPointerException npe) {
			throw new IOException("NotFound: " + uri);
		}
	}
  
  
}
