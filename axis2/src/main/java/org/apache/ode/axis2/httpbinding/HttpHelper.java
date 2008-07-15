/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ode.axis2.httpbinding;

import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.ode.axis2.Properties;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.PipedOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpHelper {

    private static final Log log = LogFactory.getLog(HttpHelper.class);

    public static void configure(HostConfiguration hostConfig, HttpState state, URI targetURI, HttpParams params) throws URIException {
        if (log.isDebugEnabled()) log.debug("Configuring http client...");
        // proxy configuration
        if (ProxyConf.isProxyEnabled(params, targetURI.getHost())) {
            if (log.isDebugEnabled()) log.debug("ProxyConf");
            ProxyConf.configure(hostConfig, state, (HttpTransportProperties.ProxyProperties) params.getParameter(Properties.PROP_HTTP_PROXY_PREFIX));
        }

        // security
        // ...

    }

    /**
     * Parse and convert a HTTP status line into an aml element.
     *
     * @param statusLine
     * @return
     * @throws HttpException
     * @see #statusLineToElement(org.w3c.dom.Document, org.apache.commons.httpclient.StatusLine)
     */
    public static Element statusLineToElement(String statusLine) throws HttpException {
        return statusLineToElement(new StatusLine(statusLine));
    }

    public static Element statusLineToElement(StatusLine statusLine) {
        return statusLineToElement(DOMUtils.newDocument(), statusLine);
    }

    /**
     * Convert a <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html#sec6.1">HTTP status line</a> into an xml element like this:
     * <p/>
     * < Status-line>
     * < HTTP-Version>HTTP/1.1< /HTTP-Version>
     * < Status-Code>200< /Status-Code>
     * < Reason-Phrase>Success - The action was successfully received, understood, and accepted< /Reason-Phrase>
     * < /Status-line></br>
     *
     * @param statusLine - the {@link org.apache.commons.httpclient.StatusLine} instance to be converted
     * @param doc        - the document to use to create new nodes
     * @return an Element
     */
    public static Element statusLineToElement(Document doc, StatusLine statusLine) {
        Element statusLineEl = doc.createElementNS(null, "Status-Line");
        Element versionEl = doc.createElementNS(null, "HTTP-Version");
        Element codeEl = doc.createElementNS(null, "Status-Code");
        Element reasonEl = doc.createElementNS(null, "Reason-Phrase");
        Element originalEl = doc.createElementNS(null, "original");

        // wiring
        doc.appendChild(statusLineEl);
        statusLineEl.appendChild(versionEl);
        statusLineEl.appendChild(codeEl);
        statusLineEl.appendChild(reasonEl);
        statusLineEl.appendChild(originalEl);

        // values
        versionEl.setTextContent(statusLine.getHttpVersion());
        codeEl.setTextContent(String.valueOf(statusLine.getStatusCode()));
        reasonEl.setTextContent(statusLine.getReasonPhrase());
        // the line as received, not parsed
        originalEl.setTextContent(statusLine.toString());

        return statusLineEl;
    }

    /**
     * Build a "details" element that looks like this:
     *
     * @param method
     * @return
     * @throws IOException
     */
    public static Element prepareDetailsElement(HttpMethod method) {
        Header h = method.getResponseHeader("Content-Type");
        String receivedType = h != null ? h.getValue() : null;
        boolean bodyIsXml = receivedType != null && HttpHelper.isXml(receivedType);


        Document doc = DOMUtils.newDocument();
        Element detailsEl = doc.createElementNS(null, "details");
        Element statusLineEl = statusLineToElement(doc, method.getStatusLine());
        detailsEl.appendChild(statusLineEl);

        // set the body if any
        try {
            final String body = method.getResponseBodyAsString();
            if (StringUtils.isNotEmpty(body)) {
                Element bodyEl = doc.createElementNS(null, "responseBody");
                detailsEl.appendChild(bodyEl);
                // first, try to parse the body as xml
                // if it fails, put it as string in the body element
                boolean exceptionDuringParsing = false;
                if (bodyIsXml) {
                    try {
                        Element parsedBodyEl = DOMUtils.stringToDOM(body);
                        bodyEl.appendChild(doc.importNode(parsedBodyEl, true));
                    } catch (Exception e) {
                        String errmsg = "Unable to parse the response body as xml. Body will be inserted as string.";
                        if (log.isDebugEnabled()) log.debug(errmsg, e);
                        exceptionDuringParsing = true;
                    }
                }
                if (!bodyIsXml || exceptionDuringParsing) {
                    bodyEl.setTextContent(body);
                }
            }
        } catch (IOException e) {
            if (log.isWarnEnabled()) log.warn("Exception while loading response body", e);
        }
        return detailsEl;
    }

    private static final Pattern NON_LWS_PATTERN = Pattern.compile("\r\n([^\\s])");

    /**
     * This method ensures that a header value containing CRLF does not mess up the HTTP request.
     * Actually CRLF is the end-of-line marker for headers.
     * <p/>
     * To do so, all CRLF followed by a non-whitespace character are replaced by CRLF HT.
     * <p/>
     * This is possible because the
     * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec2.html#sec2.2">Section 2.2</a> of HTTP standard (RFC2626) states that:
     * <p/>
     * <quote>
     * HTTP/1.1 header field values can be folded onto multiple lines if the
     * continuation line begins with a space or horizontal tab. All linear
     * white space, including folding, has the same semantics as SP. A
     * recipient MAY replace any linear white space with a single SP before
     * interpreting the field value or forwarding the message downstream.
     * <p/>
     * LWS            = [CRLF] 1*( SP | HT )
     * <p/>
     * </quote>
     * <p/>
     * FYI, HttpClient 3.x.x does not check this.
     *
     * @param header
     * @return the string properly ready to be used as an HTTP header field-content
     */
    public static String replaceCRLFwithLWS(String header) {
        Matcher m = NON_LWS_PATTERN.matcher(header);
        StringBuffer sb = new StringBuffer(header.length());
        while (m.find()) {
            m.appendReplacement(sb, "\r\n\t");
            sb.append(m.group(1));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static final String XML_MIME_TYPE_REGEX = "(text/xml)|(application/xml)|((.*)\\+xml)";
    private static final String TEXT_MIME_TYPE_REGEX = "text/(?!xml$).*";
    private static final Pattern XML_MIME_TYPE_PATTERN = Pattern.compile(XML_MIME_TYPE_REGEX);
    private static final Pattern TEXT_MIME_TYPE_PATTERN = Pattern.compile(TEXT_MIME_TYPE_REGEX);

    public static boolean isXml(String contentType) {
        return XML_MIME_TYPE_PATTERN.matcher(contentType).matches();
    }

    public static boolean isText(String contentType) {
        return TEXT_MIME_TYPE_PATTERN.matcher(contentType).matches();
    }


    public static String requestToString(HttpMethod m) {
        StringBuilder sb = new StringBuilder(256);
        try {
            sb.append("HTTP Request Details: \n").append(m.getName()).append(" ").append(m.getURI());
        } catch (URIException e) {
            // not that important
            if (log.isDebugEnabled()) log.debug(e);
        }
        sb.append("\nRequest Headers:");
        Header[] headers = m.getRequestHeaders();
        if (headers.length == 0) sb.append(" n/a");
        for (int i = 0; i < headers.length; i++) {
            Header h = headers[i];
            sb.append("\n\t").append(h.getName()).append(": ").append(h.getValue());
        }
        if (m instanceof EntityEnclosingMethod) {
            EntityEnclosingMethod eem = (EntityEnclosingMethod) m;
            if (eem.getRequestEntity() != null) {
                sb.append("\nRequest Entity:");
                sb.append("\n\tContent-Type:").append(eem.getRequestEntity().getContentType());
                sb.append("\n\tContent-Length:").append(eem.getRequestEntity().getContentLength());
                if (eem.getRequestEntity() instanceof StringRequestEntity) {
                    StringRequestEntity sre = (StringRequestEntity) eem.getRequestEntity();
                    sb.append("\n\tContent-Charset:").append(sre.getCharset());
                    sb.append("\n\tRequest Entity:\n").append(sre.getContent());
                }
            }
        }
        return sb.toString();
    }

    public static String responseToString(HttpMethod m) {
        StringBuilder sb = new StringBuilder(256);
        try {
            sb.append("HTTP Response Details: \n").append(m.getName()).append(" ").append(m.getURI());
        } catch (URIException e) {
            // not that important
            if (log.isDebugEnabled()) log.debug(e);
        }
        sb.append("\nStatus-Line: ").append(m.getStatusLine());
        Header[] headers = m.getResponseHeaders();
        if (headers.length != 0) sb.append("\nResponse Headers: ");
        for (int i = 0; i < headers.length; i++) {
            Header h = headers[i];
            sb.append("\n\t").append(h.getName()).append(": ").append(h.getValue());
        }
        try {
            if (StringUtils.isNotEmpty(m.getResponseBodyAsString())) {
                sb.append("\nResponse Entity:\n").append(m.getResponseBodyAsString());
            }
        } catch (IOException e) {
            log.error(e);
        }
        Header[] footers = m.getResponseFooters();
        if (footers.length != 0) sb.append("\nResponse Footers: ");
        for (int i = 0; i < footers.length; i++) {
            Header h = footers[i];
            sb.append("\n\t").append(h.getName()).append(": ").append(h.getValue());
        }
        return sb.toString();
    }
}
