package org.apache.ode.store;

import org.apache.ode.utils.fs.FileUtils;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
        String resourceName = resourceIdentifier.getLiteralSystemId();
        String base;
        try {
            base = new URI(FileUtils.encodePath(resourceIdentifier.getBaseSystemId())).toURL().getFile();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Base system id incorrect, parser error", e);
        }

        if (new File(new File(base).getParent(), resourceName).exists())
            src.setByteStream(new File(new File(base).getParent(), resourceName).toURL().openStream());
        else src.setByteStream(new File(_docRoot, resourceName).toURL().openStream());

        return src;
    }
}
