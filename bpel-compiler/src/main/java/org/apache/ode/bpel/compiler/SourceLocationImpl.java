package org.apache.ode.bpel.compiler;

import java.net.URI;

import org.apache.ode.bpel.compiler.api.SourceLocation;

class SourceLocationImpl implements SourceLocation {

    private int _col;
    private int _line;
    private String _path;
    private URI _uri;

    SourceLocationImpl(URI uri) {
        _uri = uri;
    }
    
    public int getColumnNo() {
        return _col;
    }

    public int getLineNo() {
        return _line;
    }

    public String getPath() {
        return _path;
    }

    public URI getURI() {
        return _uri;
    }

}
