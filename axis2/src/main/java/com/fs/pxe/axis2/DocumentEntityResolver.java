package com.fs.pxe.axis2;

import com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

import java.io.File;
import java.io.IOException;

/**
 * Resolves references inide the deployment unit.
 */
public class DocumentEntityResolver implements XMLEntityResolver {

  private File _docRoot;

  public DocumentEntityResolver(File docRoot) {
    _docRoot = docRoot;
  }

  public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
    XMLInputSource src = new XMLInputSource(resourceIdentifier);
    src.setByteStream(new File(_docRoot, resourceIdentifier.getLiteralSystemId()).toURL().openStream());
    return src;
  }
}
