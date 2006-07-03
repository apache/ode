package com.fs.pxe.bpel.parser;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Resolver implementation that restricts us to a known set of resources.
 */
class LocalEntityResolver implements EntityResolver {
  private static final Log __log = LogFactory.getLog(LocalEntityResolver.class);

  private final HashMap<String, URL> _mappings = new HashMap<String,URL>();
  
  public InputSource resolveEntity(String publicId, String systemId) 
    throws SAXException, IOException {
    if (__log.isTraceEnabled())
      __log.trace("resolveEntity(" + publicId + "," + systemId + ")") ;
    
    URL target = _mappings.get(systemId);
    if (target == null)
      target = _mappings.get(publicId);
    
    if (target == null) {
      if (__log.isDebugEnabled())
        __log.debug("resolveEntity(" + publicId + "," + systemId + 
            ") failed (resource not found) ");
      throw new IOException("Resource not found: " + publicId + " : "  + systemId);
    }
    
    if (__log.isDebugEnabled())
       __log.debug("resolveEntity(" + publicId + "," + systemId + 
            ") ==> target" );
    
    return new InputSource(target.openStream());
  }

  
  void register(String id, URL location) {
    if (id == null)
      throw new NullPointerException("id arg must not be null!");
    if (location == null)
      throw new NullPointerException("location arg must not be null");
    
    if (__log.isDebugEnabled())
      __log.debug("mapping " + id + " ==> " + location);
    _mappings.put(id, location);
  }
}
