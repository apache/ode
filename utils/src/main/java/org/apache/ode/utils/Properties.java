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

package org.apache.ode.utils;

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.axis2.transport.jms.JMSConstants;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.ProtocolException;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HostParams;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

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
    /**
     * @deprecated use org.apache.commons.httpclient.params.HttpMethodParams#HTTP_CONTENT_CHARSET (="http.protocol.content-charset")
     */
    public static final String PROP_HTTP_PROTOCOL_ENCODING = "http.protocol.encoding";

    /**
     * Property to override the location set in soap:address or http:address
     */
    public static final String PROP_ADDRESS = "address";

    // Httpclient specific
    public static final String PROP_HTTP_MAX_REDIRECTS = HttpClientParams.MAX_REDIRECTS;

    // Axis2-specific
    public static final String PROP_HTTP_REQUEST_CHUNK = "http.request.chunk";
    public static final String PROP_HTTP_REQUEST_GZIP = "http.request.gzip";
    public static final String PROP_HTTP_ACCEPT_GZIP = "http.accept.gzip";
    public static final String PROP_SECURITY_POLICY = "security.policy.file";
    public static final String PROP_JMS_REPLY_DESTINATION = "jms.reply.destination";
    public static final String PROP_JMS_REPLY_TIMEOUT = "jms.reply.timeout";
    public static final String PROP_JMS_DESTINATION_TYPE = "jms.destination.type";
    public static final String PROP_SEND_WS_ADDRESSING_HEADERS = "ws-addressing.headers";


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
                proxy = null;
                if (log.isDebugEnabled()) log.debug("Proxy host is null. Proxy will not be taken into account.");
            }
        }

        return new Object[]{proxy, headers};
    }

    public static class Axis2 {

        public static Options translate(Map<String, String> properties) {
            return translate(properties, new Options());
        }

        public static Options translate(Map<String, String> properties, Options options) {
            if (log.isDebugEnabled()) log.debug("Translating Properties for Axis2");
            if (properties.isEmpty()) return options;

            // First set any default values to make sure they can be overwriten
            // set the default encoding for HttpClient (HttpClient uses ISO-8859-1 by default)
            options.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, "UTF-8");

            /*then add all property pairs so that new properties (with string value)
                are automatically handled (i.e no translation needed) */
            for (Map.Entry<String, String> e : properties.entrySet()) {
                options.setProperty(e.getKey(), e.getValue());
            }
            if (properties.containsKey(PROP_HTTP_CONNECTION_TIMEOUT)) {
                final String value = properties.get(PROP_HTTP_CONNECTION_TIMEOUT);
                try {
                    options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, Integer.valueOf(value));
                } catch (NumberFormatException e) {
                    if (log.isWarnEnabled())
                        log.warn("Mal-formatted Property: [" + Properties.PROP_HTTP_CONNECTION_TIMEOUT + "=" + value + "]. Integer expected. Property will be skipped.");
                }
            }
            if (properties.containsKey(PROP_HTTP_SOCKET_TIMEOUT)) {
                final String value = properties.get(PROP_HTTP_SOCKET_TIMEOUT);
                try {
                    options.setProperty(HTTPConstants.SO_TIMEOUT, Integer.valueOf(value));
                } catch (NumberFormatException e) {
                    if (log.isWarnEnabled())
                        log.warn("Mal-formatted Property: [" + Properties.PROP_HTTP_SOCKET_TIMEOUT + "=" + value + "]. Integer expected. Property will be skipped.");
                }
            }
            if (properties.containsKey(PROP_HTTP_PROTOCOL_ENCODING)) {
                if(log.isWarnEnabled())log.warn("Deprecated property: http.protocol.encoding. Use http.protocol.content-charset");
                options.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, properties.get(PROP_HTTP_PROTOCOL_ENCODING));
            }
            if (properties.containsKey(HttpMethodParams.HTTP_CONTENT_CHARSET)) {
                options.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, properties.get(HttpMethodParams.HTTP_CONTENT_CHARSET));
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
            if (properties.containsKey(PROP_JMS_REPLY_DESTINATION)) {
                options.setProperty(JMSConstants.REPLY_PARAM, properties.get(PROP_JMS_REPLY_DESTINATION));
            }
            if (properties.containsKey(PROP_JMS_REPLY_TIMEOUT)) {
                String value = properties.get(PROP_JMS_REPLY_TIMEOUT);
                options.setProperty(JMSConstants.JMS_WAIT_REPLY, value);
                // The value of this property must be a string object, not a long object. 
//                try {
//                    options.setProperty(JMSConstants.JMS_WAIT_REPLY, Long.valueOf(value));
//                } catch (NumberFormatException e) {
//                    if (log.isWarnEnabled())
//                        log.warn("Mal-formatted Property: [" + Properties.PROP_JMS_REPLY_TIMEOUT + "=" + value + "]. Long expected. Property will be skipped.");
//                }
            }
            if (properties.containsKey(PROP_JMS_DESTINATION_TYPE)) {
                String value = properties.get(PROP_JMS_DESTINATION_TYPE);
                try {
                    options.setProperty(JMSConstants.DEST_TYPE_PARAM, Long.valueOf(value));
                } catch (NumberFormatException e) {
                    if (log.isWarnEnabled())
                        log.warn("Mal-formatted Property: [" + Properties.PROP_JMS_DESTINATION_TYPE + "=" + value + "]. Long expected. Property will be skipped.");
                }
            }
            if(properties.containsKey(PROP_SEND_WS_ADDRESSING_HEADERS)){
                String value = properties.get(PROP_SEND_WS_ADDRESSING_HEADERS);
                options.setProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, !Boolean.parseBoolean(value));
            }
            if (properties.containsKey("ws-adddressing.headers")) {
                if(log.isWarnEnabled())log.warn("Deprecated property: ws-adddressing.headers (Mind the 3 d's). Use ws-addressing.headers");                
                String value = properties.get("ws-adddressing.headers");
                options.setProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, !Boolean.parseBoolean(value));
            }

            // iterate through the properties to get Headers & Proxy information
            Object[] o = getProxyAndHeaders(properties);
            HttpTransportProperties.ProxyProperties proxy = (HttpTransportProperties.ProxyProperties) o[0];
            ArrayList<Header> headers = (ArrayList<Header>) o[1]; // /!\ Axis2 requires an ArrayList (not a List implementation)
            if (headers != null && !headers.isEmpty()) options.setProperty(HTTPConstants.HTTP_HEADERS, headers);
            if (proxy != null) options.setProperty(HTTPConstants.PROXY, proxy);

            // Set properties that canNOT be overridden
            if(JavaUtils.isTrueExplicitly(options.getProperty(HTTPConstants.REUSE_HTTP_CLIENT))){
                if (log.isWarnEnabled()) log.warn("This property cannot be overidden, and must always be false. "+ HTTPConstants.REUSE_HTTP_CLIENT);
            }
            options.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, "false");
            return options;
        }
    }


    public static class HttpClient {
        public static HttpParams translate(Map<String, String> properties) {
            return translate(properties, new DefaultHttpParams());
        }

        public static HttpParams translate(Map<String, String> properties, HttpParams p) {
            if (log.isDebugEnabled())
                log.debug("Translating Properties for HttpClient. Properties size=" + properties.size());
            if (properties.isEmpty()) return p;

            // First set any default values to make sure they can be overwriten
            // set the default encoding for HttpClient (HttpClient uses ISO-8859-1 by default)
            p.setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");

            /*then all property pairs so that new properties (with string value)
             are automatically handled (i.e no translation needed) */
            for (Map.Entry<String, String> e : properties.entrySet()) {
                p.setParameter(e.getKey(), e.getValue());
            }

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
                if(log.isWarnEnabled())log.warn("Deprecated property: http.protocol.encoding. Use http.protocol.content-charset");
                p.setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, properties.get(PROP_HTTP_PROTOCOL_ENCODING));
            }
            // the next one is redundant because HttpMethodParams.HTTP_CONTENT_CHARSET accepts a string and we use the same property name
            // so the property has already been added.
            if (properties.containsKey(HttpMethodParams.HTTP_CONTENT_CHARSET)) {
                p.setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, properties.get(HttpMethodParams.HTTP_CONTENT_CHARSET));
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
                if (log.isWarnEnabled())
                    log.warn("Property not supported by HTTP External Services: " + PROP_HTTP_REQUEST_GZIP);
            }

            if (Boolean.parseBoolean(properties.get(PROP_HTTP_ACCEPT_GZIP))) {
                // append gzip to the list of accepted encoding
                // HttpClient does not support compression natively
                // Additional code would be necessary to handle it.
//                ((Collection) p.getParameter(HostParams.DEFAULT_HEADERS)).add(new Header("Accept-Encoding", "gzip"));
                if (log.isWarnEnabled())
                    log.warn("Property not supported by HTTP External Services: " + PROP_HTTP_ACCEPT_GZIP);
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

            return new UnmodifiableHttpParams(p);
        }

        static class UnmodifiableHttpParams implements HttpParams {

            final HttpParams p;

            private UnmodifiableHttpParams(HttpParams p) {
                this.p = p;
            }

            public void setBooleanParameter(String name, boolean value) {
                throw new UnsupportedOperationException();
            }

            public void setDefaults(HttpParams params) {
                throw new UnsupportedOperationException();
            }

            public void setDoubleParameter(String name, double value) {
                throw new UnsupportedOperationException();
            }

            public void setIntParameter(String name, int value) {
                throw new UnsupportedOperationException();
            }

            public void setLongParameter(String name, long value) {
                throw new UnsupportedOperationException();
            }

            public void setParameter(String name, Object value) {
                throw new UnsupportedOperationException();
            }

            public boolean getBooleanParameter(String name, boolean defaultValue) {
                return p.getBooleanParameter(name, defaultValue);
            }

            public HttpParams getDefaults() {
                return null;
            }

            public double getDoubleParameter(String name, double defaultValue) {
                return p.getDoubleParameter(name, defaultValue);
            }

            public int getIntParameter(String name, int defaultValue) {
                return p.getIntParameter(name, defaultValue);
            }

            public long getLongParameter(String name, long defaultValue) {
                return p.getLongParameter(name, defaultValue);
            }

            public Object getParameter(String name) {
                return p.getParameter(name);
            }

            public boolean isParameterFalse(String name) {
                return p.isParameterFalse(name);
            }

            public boolean isParameterSet(String name) {
                return p.isParameterSet(name);
            }

            public boolean isParameterSetLocally(String name) {
                return p.isParameterSetLocally(name);
            }

            public boolean isParameterTrue(String name) {
                return p.isParameterTrue(name);
            }
        }
    }
}
