package com.fs.pxe.axis2;

import com.fs.pxe.bpel.compiler.WsdlFinder;
import com.fs.pxe.bpel.compiler.XsltFinder;
import com.fs.pxe.bom.wsdl.Definition4BPEL;
import com.fs.utils.StreamUtils;

import javax.wsdl.xml.WSDLReader;
import javax.wsdl.WSDLException;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.net.URI;
import java.net.MalformedURLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Finds WSDL documents within a deployment unit (no relative path shit,
 * everything's in the same directory).
 */
public class DUXsltFinder implements XsltFinder {

  private static final Log __log = LogFactory.getLog(DUXsltFinder.class);

  private File _suDir;

  public DUXsltFinder() {
    // no base URL
  }

  public DUXsltFinder(File suDir) {
    _suDir = suDir;
  }

  public void setBaseURI(URI u) {
    _suDir = new File(u);
  }

  public String loadXsltSheet(URI uri) {
    // Eliminating whatever path has been provided, we always look into our SU
    // deployment directory.
    String strUri = uri.toString();
    String filename = strUri.substring(strUri.lastIndexOf("/"), strUri.length());
    try {
      return new String(StreamUtils.read(new FileInputStream(new File(_suDir, filename))));
    } catch (IOException e) {
      if (__log.isDebugEnabled())
        __log.debug("error obtaining resource '" + uri + "' from repository.", e);
      return null;
    }
  }

}
