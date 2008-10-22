package org.apache.ode.bpel.iapi;

/**
 * Web RESTful resource, includes URL, method and content type (defaults to XML).
 */
public class Resource {

    private String url;
    private String contentType;
    private String method;

    public Resource() { }

    public Resource(String url, String contentType, String method) {
        this.url = url;
        this.contentType = contentType;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
