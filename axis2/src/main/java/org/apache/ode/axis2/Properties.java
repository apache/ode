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

package org.apache.ode.axis2;

import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.ProtocolException;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HostParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Collection;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class Properties {

    /**
     * Property used to define how long (in miiliseconds) the message will wait for a response. Default value is {@link #DEFAULT_MEX_TIMEOUT}
     */
    public static final String PROP_MEX_TIMEOUT = "mex.timeout";
    // its default value
    public static final int DEFAULT_MEX_TIMEOUT = 2 * 60 * 1000;


    public static final String PROP_HTTP_CONNECTION_TIMEOUT = HttpConnectionParams.CONNECTION_TIMEOUT;
    public static final String PROP_HTTP_SOCKET_TIMEOUT = HttpMethodParams.SO_TIMEOUT;
    public static final String PROP_HTTP_PROTOCOL_VERSION = HttpMethodParams.PROTOCOL_VERSION;
    public static final String PROP_HTTP_HEADER_PREFIX = "http.default-headers.";
    public static final String PROP_HTTP_PROXY_PREFIX = "http.proxy.";
    public static final String PROP_HTTP_PROXY_HOST = PROP_HTTP_PROXY_PREFIX + "host";
    public static final String PROP_HTTP_PROXY_PORT = PROP_HTTP_PROXY_PREFIX + "port";
    public static final String PROP_HTTP_PROXY_DOMAIN = PROP_HTTP_PROXY_PREFIX + "domain";
    public static final String PROP_HTTP_PROXY_USER = PROP_HTTP_PROXY_PREFIX + "user";
    public static final String PROP_HTTP_PROXY_PASSWORD = PROP_HTTP_PROXY_PREFIX + "password";

    // Httpclient specific
    public static final String PROP_HTTP_MAX_REDIRECTS = HttpClientParams.MAX_REDIRECTS;

    // Axis2-specific
    public static final String PROP_HTTP_PROTOCOL_ENCODING = "http.protocol.encoding";
    public static final String PROP_HTTP_REQUEST_CHUNK = "http.request.chunk";
    public static final String PROP_HTTP_REQUEST_GZIP = "http.request.gzip";
    public static final String PROP_HTTP_ACCEPT_GZIP = "http.accept.gzip";


    protected static final Log log = LogFactory.getLog(Properties.class);

    public static Object[] getProxyAndHeaders(Map<String, String> properties) {
        ArrayList<Header> headers = null; // /!\ Axis2 requires an ArrayList (not a List implementation)
        HttpTransportProperties.ProxyProperties proxy = null;
        for (Map.Entry<String, String> e : properties.entrySet()) {
            final String k = e.getKey();
            final String v = e.getValue();
            if (k.startsWith(PROP_HTTP_HEADER_PREFIX)) {
                if (headers == null) headers = new ArrayList<Header>();
                // extract the header name
                String name = k.substring(PROP_HTTP_HEADER_PREFIX.length());
                headers.add(new Header(name, v));
            } else if (k.startsWith(PROP_HTTP_PROXY_PREFIX)) {
                if (proxy == null) proxy = new HttpTransportProperties.ProxyProperties();

                if (PROP_HTTP_PROXY_HOST.equals(k)) proxy.setProxyName(v);
                else if (PROP_HTTP_PROXY_PORT.equals(k)) proxy.setProxyPort(Integer.parseInt(v));
                else if (PROP_HTTP_PROXY_DOMAIN.equals(k)) proxy.setDomain(v);
                else if (PROP_HTTP_PROXY_USER.equals(k)) proxy.setUserName(v);
                else if (PROP_HTTP_PROXY_PASSWORD.equals(k)) proxy.setPassWord(v);
                else if (log.isWarnEnabled())
                    log.warn("Unknown proxy properties [" + k + "]. " + PROP_HTTP_PROXY_PREFIX + " is a refix reserved for proxy properties.");
            }
        }
        if (proxy != null) {
            String host = proxy.getProxyHostName();
            if (host == null || host.length() == 0) {
                // disable proxy if the host is not null
                proxy=null;
                if(log.isDebugEnabled()) log.debug("Proxy host is null. Proxy will not be taken into account.");
            }
        }

        return new Object[]{proxy, headers};
    }

    public static class Axis2 {

        public static Options translate(Map<String, String> properties) {
            return translate(properties, new Options());
        }

        public static Options translate(Map<String, String> properties, Options options) {
            if (log.isDebugEnabled()) log.debug("Translating IL Properties for Axis2");
            if (properties.isEmpty()) return options;
            if (properties.containsKey(PROP_HTTP_CONNECTION_TIMEOUT)) {
                final String value = properties.get(PROP_HTTP_CONNECTION_TIMEOUT);
                try {
                    options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, Integer.valueOf(value));
                } catch (NumberFormatException e) {
                    if (log.isWarnEnabled())
                        log.warn("Mal-formatted Property: [" + Properties.PROP_HTTP_CONNECTION_TIMEOUT + "=" + value + "] Property will be skipped.");
                }
            }
            if (properties.containsKey(PROP_HTTP_SOCKET_TIMEOUT)) {
                final String value = properties.get(PROP_HTTP_SOCKET_TIMEOUT);
                try {
                    options.setProperty(HTTPConstants.SO_TIMEOUT, Integer.valueOf(value));
                } catch (NumberFormatException e) {
                    if (log.isWarnEnabled())
                        log.warn("Mal-formatted Property: [" + Properties.PROP_HTTP_SOCKET_TIMEOUT + "=" + value + "] Property will be skipped.");
                }
            }
            if (properties.containsKey(PROP_HTTP_PROTOCOL_ENCODING)) {
                options.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, properties.get(PROP_HTTP_PROTOCOL_ENCODING));
            }
            if (properties.containsKey(PROP_HTTP_PROTOCOL_VERSION)) {
                options.setProperty(HTTPConstants.HTTP_PROTOCOL_VERSION, properties.get(PROP_HTTP_PROTOCOL_VERSION));
            }
            if (properties.containsKey(PROP_HTTP_REQUEST_CHUNK)) {
                options.setProperty(HTTPConstants.CHUNKED, properties.get(PROP_HTTP_REQUEST_CHUNK));
            }
            if (properties.containsKey(PROP_HTTP_REQUEST_GZIP)) {
                options.setProperty(HTTPConstants.MC_GZIP_REQUEST, properties.get(PROP_HTTP_REQUEST_GZIP));
            }
            if (properties.containsKey(PROP_HTTP_ACCEPT_GZIP)) {
                options.setProperty(HTTPConstants.MC_ACCEPT_GZIP, properties.get(PROP_HTTP_ACCEPT_GZIP));
            }
            if (properties.containsKey(PROP_HTTP_MAX_REDIRECTS)) {
                if (log.isWarnEnabled()) log.warn("Property Not Supported: " + PROP_HTTP_MAX_REDIRECTS);
            }

            // iterate through the properties to get Headers & Proxy information
            Object[] o = getProxyAndHeaders(properties);
            HttpTransportProperties.ProxyProperties proxy = (HttpTransportProperties.ProxyProperties) o[0];
            ArrayList<Header> headers = (ArrayList<Header>) o[1]; // /!\ Axis2 requires an ArrayList (not a List implementation)
            if (headers != null && !headers.isEmpty()) options.setProperty(HTTPConstants.HTTP_HEADERS, headers);
            if (proxy != null) options.setProperty(HTTPConstants.PROXY, proxy);

            return options;
        }
    }


    public static class HttpClient {
        public static HttpParams translate(Map<String, String> properties) {
            return translate(properties, new DefaultHttpParams());
        }

        public static HttpParams translate(Map<String, String> properties, HttpParams p) {
            if (log.isDebugEnabled()) log.debug("Translating IL Properties for HttpClient. Properties size="+properties.size());
            if (properties.isEmpty()) return p;

            // initialize the collection of headers
            p.setParameter(HostParams.DEFAULT_HEADERS, new ArrayList());

            if (properties.containsKey(PROP_HTTP_CONNECTION_TIMEOUT)) {
                final String value = properties.get(PROP_HTTP_CONNECTION_TIMEOUT);
                try {
                    p.setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, Integer.valueOf(value));
                } catch (NumberFormatException e) {
                    if (log.isWarnEnabled())
                        log.warn("Mal-formatted Property: [" + Properties.PROP_HTTP_CONNECTION_TIMEOUT + "=" + value + "] Property will be skipped.");
                }
            }
            if (properties.containsKey(PROP_HTTP_SOCKET_TIMEOUT)) {
                final String value = properties.get(PROP_HTTP_SOCKET_TIMEOUT);
                try {
                    p.setParameter(HttpMethodParams.SO_TIMEOUT, Integer.valueOf(value));
                } catch (NumberFormatException e) {
                    if (log.isWarnEnabled())
                        log.warn("Mal-formatted Property: [" + Properties.PROP_HTTP_SOCKET_TIMEOUT + "=" + value + "] Property will be skipped.");
                }
            }
            if (properties.containsKey(PROP_HTTP_PROTOCOL_ENCODING)) {
                p.setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, properties.get(PROP_HTTP_PROTOCOL_ENCODING));
            }
            if (properties.containsKey(PROP_HTTP_PROTOCOL_VERSION)) {
                try {
                    p.setParameter(HttpMethodParams.PROTOCOL_VERSION, HttpVersion.parse(properties.get(PROP_HTTP_PROTOCOL_VERSION)));
                } catch (ProtocolException e) {
                    if (log.isWarnEnabled())
                        log.warn("Mal-formatted Property: [" + PROP_HTTP_PROTOCOL_VERSION + "]", e);
                }
            }
            if (properties.containsKey(PROP_HTTP_REQUEST_CHUNK)) {
                // see org.apache.commons.httpclient.methods.EntityEnclosingMethod.setContentChunked()
                p.setBooleanParameter(PROP_HTTP_REQUEST_CHUNK, Boolean.parseBoolean(properties.get(PROP_HTTP_REQUEST_CHUNK)));
            }
            if (properties.containsKey(PROP_HTTP_REQUEST_GZIP)) {
                if (log.isWarnEnabled()) log.warn("Property Not Supported: " + PROP_HTTP_REQUEST_GZIP);
            }

            if (Boolean.parseBoolean(properties.get(PROP_HTTP_ACCEPT_GZIP))) {
                // append gzip to the list of accepted encoding
                ((Collection) p.getParameter(HostParams.DEFAULT_HEADERS)).add(new Header("Accept-Encoding", "gzip"));
            }

            if (properties.containsKey(PROP_HTTP_MAX_REDIRECTS)) {
                final String value = properties.get(PROP_HTTP_MAX_REDIRECTS);
                try {
                    p.setParameter(HttpClientParams.MAX_REDIRECTS, Integer.valueOf(value));
                } catch (NumberFormatException e) {
                    if (log.isWarnEnabled())
                        log.warn("Mal-formatted Property: [" + Properties.PROP_HTTP_MAX_REDIRECTS + "=" + value + "] Property will be skipped.");
                }
            }

            Object[] o = getProxyAndHeaders(properties);
            HttpTransportProperties.ProxyProperties proxy = (HttpTransportProperties.ProxyProperties) o[0];
            Collection headers = (Collection) o[1];
            if (headers != null && !headers.isEmpty())
                ((Collection) p.getParameter(HostParams.DEFAULT_HEADERS)).addAll(headers);
            if (proxy != null) p.setParameter(PROP_HTTP_PROXY_PREFIX, proxy);

            return p;
        }

    }
}
