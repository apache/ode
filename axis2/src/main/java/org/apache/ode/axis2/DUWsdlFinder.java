package org.apache.ode.axis2;

import org.apache.ode.bom.wsdl.Definition4BPEL;
import org.apache.ode.bpel.compiler.WsdlFinder;

import javax.wsdl.xml.WSDLReader;
import javax.wsdl.WSDLException;
import java.net.URI;
import java.net.MalformedURLException;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;

/**
 * Finds WSDL documents within a deployment unit (no relative path shit,
 * everything's in the same directory).
 */
public class DUWsdlFinder implements WsdlFinder {

  private File _suDir;

  public DUWsdlFinder() {
    // no base URL
  }

  public DUWsdlFinder(File suDir) {
    _suDir = suDir;
  }

  public void setBaseURI(URI u) {
    _suDir = new File(u);
  }

  public Definition4BPEL loadDefinition(WSDLReader r, URI uri) throws WSDLException {
    // Eliminating whatever path has been provided, we always look into our SU
    // deployment directory.
    String strUri = uri.toString();
    String filename = strUri.substring(strUri.lastIndexOf("/"), strUri.length());
    return (Definition4BPEL) r.readWSDL(new File(_suDir, filename).getPath());
  }

  public InputStream openResource(URI uri) throws MalformedURLException, IOException {
    return uri.toURL().openStream();
  }

}
