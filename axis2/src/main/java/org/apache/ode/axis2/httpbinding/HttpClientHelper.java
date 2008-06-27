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

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.commons.httpclient.params.HostParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.ode.axis2.Properties;
import org.apache.ode.axis2.util.URLEncodedTransformer;
import org.apache.ode.axis2.util.UrlReplacementTransformer;
import org.apache.ode.bpel.epr.MutableEndpoint;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.wsdl.Messages;
import org.apache.ode.utils.wsdl.WsdlUtils;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.extensions.http.HTTPOperation;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.xml.namespace.QName;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Collection;

public class HttpClientHelper {

    private static final Log log = LogFactory.getLog(HttpClientHelper.class);

    public void configure(HostConfiguration hostConfig, HttpState state, URI targetURI, HttpParams params) throws URIException {
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
    public Element statusLineToElement(String statusLine) throws HttpException {
        return statusLineToElement(new StatusLine(statusLine));
    }

    public Element statusLineToElement(StatusLine statusLine) {
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
    public Element statusLineToElement(Document doc, StatusLine statusLine) {
        Element statusLineEl = doc.createElementNS(null, "Status-Line");
        Element versionEl = doc.createElementNS(null, "HTTP-Version");
        Element codeEl = doc.createElementNS(null, "Status-Code");
        Element reasonEl = doc.createElementNS(null, "Reason-Phrase");

        // wiring
        doc.appendChild(statusLineEl);
        statusLineEl.appendChild(versionEl);
        statusLineEl.appendChild(codeEl);
        statusLineEl.appendChild(reasonEl);

        // values
        versionEl.setTextContent(statusLine.getHttpVersion());
        codeEl.setTextContent(String.valueOf(statusLine.getStatusCode()));
        reasonEl.setTextContent(statusLine.getReasonPhrase());

        return statusLineEl;
    }

    /**
     * Build a "details" element that looks like this:
     *
     * @param method
     * @return
     * @throws IOException
     */
    public Element prepareDetailsElement(HttpMethod method) throws IOException {
        return prepareDetailsElement(method, true);
    }

    /**
     *
     * @param method
     * @param bodyIsXml if true the body will be parsed as xml else the body will be inserted as string
     * @return
     * @throws IOException
     */
    public Element prepareDetailsElement(HttpMethod method, boolean bodyIsXml) throws IOException {
        Document doc = DOMUtils.newDocument();
        Element detailsEl = doc.createElementNS(null, "details");
        Element statusLineEl = statusLineToElement(doc, method.getStatusLine());
        detailsEl.appendChild(statusLineEl);

        // set the body if any
        final InputStream bodyAsStream = method.getResponseBodyAsStream();
        if (bodyAsStream != null) {
            Element bodyEl = doc.createElementNS(null, "responseBody");
            detailsEl.appendChild(bodyEl);
            // first, try to parse the body as xml
            // if it fails, put it as string in the body element
            boolean exceptionDuringParsing = false;
            if (bodyIsXml) {
                try {
                    Element parsedBodyEl = DOMUtils.parse(bodyAsStream).getDocumentElement();
                    bodyEl.appendChild(parsedBodyEl);
                } catch (Exception e) {
                    String errmsg = "Unable to parse the response body as xml. Body will be inserted as string.";
                    if (log.isDebugEnabled()) log.debug(errmsg, e);
                    exceptionDuringParsing = true;
                }
            }
            if (!bodyIsXml || exceptionDuringParsing) {
                bodyEl.setTextContent(method.getResponseBodyAsString());
            }
        }
        return detailsEl;
    }
}
