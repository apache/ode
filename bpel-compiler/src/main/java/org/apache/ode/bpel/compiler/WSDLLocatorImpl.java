package org.apache.ode.bpel.compiler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import javax.wsdl.xml.WSDLLocator;

import org.xml.sax.InputSource;

public class WSDLLocatorImpl implements WSDLLocator {

    private ResourceFinder _resourceFinder;
    private URI _base;
    private String _latest;

    public WSDLLocatorImpl(ResourceFinder resourceFinder, URI base) {
        _resourceFinder = resourceFinder;
        _base = base;
    }
    
    public InputSource getBaseInputSource() {
        try {
            InputSource is = new InputSource();
            is.setByteStream(_resourceFinder.openResource(_base));
            is.setSystemId(_base.toString());
            return is;
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public InputSource getImportInputSource(String parent, String imprt) {
        URI uri = parent == null ? _base.resolve(imprt) : _base.resolve(parent).resolve(imprt);
        InputSource is = new InputSource();
        try {
            is.setByteStream(_resourceFinder.openResource(uri));
        } catch (Exception e) {
            return null;
        }
        is.setSystemId(uri.toString());
        _latest = uri.toString();
        return is;
    }

    public String getBaseURI() {
        return _base.toString();
    }

    public String getLatestImportURI() {
        return _latest;
    }

}
