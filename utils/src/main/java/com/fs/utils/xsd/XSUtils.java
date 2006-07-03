/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils.xsd;

import com.fs.utils.msg.MessageBundle;
import com.sun.org.apache.xerces.internal.dom.DOMInputImpl;
import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaLoader;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import com.sun.org.apache.xerces.internal.xs.XSModel;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.ls.LSInput;

/**
 * Various utility methods related to XML Schema processing.
 */
public class XSUtils {

  private static final Log __log = LogFactory.getLog(XSUtils.class);

  private static final XsdMessages __msgs = MessageBundle.getMessages(XsdMessages.class);

  /**
   * Recursively "capture" XSD documents starting at the given URI and
   * using an {@link XMLEntityResolver} to obtain document streams. The
   * result is a mapping from the XSD URI to a byte array containing the
   * "captured" document stream.
   *  
   * @param initialUri URI of the schema
   * @param resolver {@link XMLEntityResolver} used to obtain XSD document streams 
   *
   * @return mapping between schema URI and the "captured" schema text (in byte form)
   */
  public static Map<URI, byte[]> captureSchema(String initialUri, XMLEntityResolver resolver)
      throws XsdException {
    DOMInputImpl input = new DOMInputImpl();
    input.setSystemId(initialUri);
    return captureSchema(input, resolver);
  }

  /**
   * Capture the schemas supplied by the reader.  <code>systemURI</code> is
   * required to resolve any relative uris encountered during the parse.
   *
   * @param systemURI Used to resolve relative uris.
   * @param schemaData the top level schema.
   * @param resolver entity resolver
   *
   * @return
   */
  public static Map<URI, byte[]> captureSchema(URI systemURI, String schemaData,
      XMLEntityResolver resolver) throws XsdException {
    
    if (__log.isDebugEnabled()) 
      __log.debug("captureSchema(URI,Text,...): systemURI=" + systemURI);
    
    DOMInputImpl input = new DOMInputImpl();
    input.setSystemId(systemURI.toString());
    input.setStringData(schemaData);

    Map<URI, byte[]> ret = captureSchema(input, resolver);
    // Let's not forget the root schema.
    try {
      // TODO don't assume UTF-8 - but which encoding is required?
      // either we need another parameter or the entire idea of
      // passing in a String needs to be revised.
      ret.put(systemURI, schemaData.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException uenc) {
      throw new RuntimeException(uenc);
    }
    return ret;
  }

  private static Map<URI, byte[]> captureSchema(LSInput input, XMLEntityResolver resolver)
      throws XsdException {
    
    if (__log.isDebugEnabled()) 
      __log.debug("captureSchema(LSInput,...): input.systemId=" + input.getSystemId());
    
    Map<URI, byte[]> captured = new HashMap<URI, byte[]>();

    if (resolver == null) {
      resolver = new DefaultXMLEntityResolver();
    }

    CapturingXMLEntityResolver cr = new CapturingXMLEntityResolver(captured, resolver);

    XMLSchemaLoader schemaLoader = new XMLSchemaLoader();
    schemaLoader.setEntityResolver(cr);

    LoggingXmlErrorHandler eh = new LoggingXmlErrorHandler(__log);
    schemaLoader.setErrorHandler(eh);

    XSModel model = schemaLoader.load(input);

    // The following mess is due to XMLSchemaLoaders funkyness in error
    // reporting: sometimes it throws an exception, sometime it returns
    // null, sometimes it just prints bs to the screen.
    if (model == null) {
      /*
       * Someone inside Xerces will have eaten this exception, for no good
       * reason.
       */
      List<XMLParseException> errors = eh.getErrors();
      if (errors.size() != 0) {
        if (__log.isDebugEnabled()) 
          __log.debug("captureSchema: XMLParseException(s) in " + input);
        
        XsdException ex = null;
        for (XMLParseException xpe : errors) {
          ex = new XsdException(ex, xpe.getMessage(), xpe.getLineNumber(), xpe.getColumnNumber(),
              xpe.getLiteralSystemId());
        }
        assert ex != null;
        throw ex;
      }
      
      if (__log.isDebugEnabled())
        __log.debug("captureSchema: NULL model (unknown error) for " + input.getSystemId());
      
      throw new XsdException(null, __msgs.msgXsdUnknownError(input.getSystemId()), 0, 0, input.getSystemId());
    }

    return captured;
  }

  /**
   * Implementation of {@link LoggingXmlErrorHandler} that outputs messages to
   * a log.
   */
  static class LoggingXmlErrorHandler implements XMLErrorHandler {

    private Log _log;

    private ArrayList<XMLParseException> _errors = new ArrayList<XMLParseException>();

    /**
     * Create a new instance that will output to the specified {@link Log}
     * instance.
     * @param log the target log, which much be non-<code>null</code>
     */
    public LoggingXmlErrorHandler(Log log) {
      assert log != null;
      _log = log;
    }

    public List<XMLParseException> getErrors() {
      return _errors;
    }
    
    /**
     * @see XMLErrorHandler#warning(java.lang.String, java.lang.String, org.apache.xerces.xni.parser.XMLParseException)
     */
    public void warning(String domain, String key, XMLParseException ex) throws XNIException {
      if (_log.isDebugEnabled())
        _log.debug("XSDErrorHandler.warning: domain=" + domain + ", key=" + key,ex);
      
      if (ex != null) {
        _errors.add(ex);
        throw ex;
      }
    }

    /**
     * @see org.apache.xerces.xni.parser.XMLErrorHandler#error(java.lang.String, java.lang.String, org.apache.xerces.xni.parser.XMLParseException)
     */
    public void error(String domain, String key, XMLParseException ex) throws XNIException {
      if (_log.isDebugEnabled())
        _log.debug("XSDErrorHandler.error: domain=" + domain + ", key=" + key,ex);

      if (ex != null) {
        _errors.add(ex);
        throw ex;
      }

      // Should not reach here, but just in case...
      throw new XNIException("Unknown XSD error state; domain=" + domain + ", key=" +key);
    }

    /**
     * @see org.apache.xerces.xni.impl.XMLErrorHandler#fatalError(java.lang.String, java.lang.String, org.apache.xerces.xni.impl.XMLParseException)
     */
    public void fatalError(String domain, String key, XMLParseException ex) throws XNIException {
      if (_log.isDebugEnabled())
        _log.debug("XSDErrorHandler.fatal: domain=" + domain + ", key=" + key,ex);

      if (ex != null) {
        _errors.add(ex);
        throw ex;
      }

      // Should not reach here, but just in case...
      throw new XNIException("Unknown XSD error state; domain=" + domain + ", key=" +key);
    } 
  }
}
