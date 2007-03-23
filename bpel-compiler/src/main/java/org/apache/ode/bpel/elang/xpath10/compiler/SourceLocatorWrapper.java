package org.apache.ode.bpel.elang.xpath10.compiler;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.SourceLocator;

import org.apache.ode.bpel.compiler.api.SourceLocation;

public class SourceLocatorWrapper implements SourceLocation {

    private SourceLocator _sloc;

    public SourceLocatorWrapper(SourceLocator sloc) {
        _sloc = sloc;
    }
    
    public int getColumnNo() {
        return _sloc.getColumnNumber();
    }

    public int getLineNo() {
        return _sloc.getLineNumber();
    }

    public String getPath() {
        return "";
    }

    public URI getURI() {
        try {
            return new URI(_sloc.getSystemId());
        } catch (Exception e) {
            return null;
        }
    }

}
