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
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.ode.utils.Properties;


/**
 * The purpose of this class is to configure proxy for HttpClient.
 */
public class ProxyConf {


    // these properties are java system properties
    // see http://java.sun.com/j2se/1.5.0/docs/guide/net/properties.html
    public static final String HTTP_PROXY_HOST = "http.proxyHost";
    public static final String HTTP_PROXY_PORT = "http.proxyPort";
    public static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

    public static void configure(HostConfiguration hostConfig, HttpState state, HttpTransportProperties.ProxyProperties proxyProperties) {
        String proxyHost = proxyProperties.getProxyHostName();
        int proxyPort = proxyProperties.getProxyPort();

        //Setting credentials
        String userName = proxyProperties.getUserName();
        String password = proxyProperties.getPassWord();
        String domain = proxyProperties.getDomain();

        Credentials proxyCred;
        if (userName != null && password != null && domain != null) {
            proxyCred = new NTCredentials(userName, password, proxyHost, domain);
        } else if (userName != null) {
            proxyCred = new UsernamePasswordCredentials(userName, password);
        } else {
            proxyCred = new UsernamePasswordCredentials("", "");
        }

        //Using Java Networking Properties
        String host = System.getProperty(HTTP_PROXY_HOST);
        if (host != null) {
            proxyHost = host;
            proxyCred = new UsernamePasswordCredentials("", "");
        }
        String port = System.getProperty(HTTP_PROXY_PORT);
        if (port != null) {
            proxyPort = Integer.parseInt(port);
        }
        state.setProxyCredentials(AuthScope.ANY, proxyCred);
        hostConfig.setProxy(proxyHost, proxyPort);
    }

    /**
     * @return true if a proxy is set in the params, or in the system property "http.proxyHost"
     * and the host is not mentionnned in the system property "http.nonProxyHosts"  
     * @see Properties#PROP_HTTP_PROXY_PREFIX
     */
    public static boolean isProxyEnabled(HttpParams params, String targetHost) throws URIException {
        // from IL properties
        boolean isSet = params.isParameterSet(Properties.PROP_HTTP_PROXY_PREFIX);
        // from Java Networking Properties
        isSet |= System.getProperty(HTTP_PROXY_HOST) != null;

        boolean isNonProxyHost = isNonProxyHost(targetHost);
        return isSet && !isNonProxyHost;
    }

    /**
     *
     * @return true if the targetHost is mentioned in the system properties "http.nonProxyHosts"
     * @see http://java.sun.com/j2se/1.5.0/docs/guide/net/properties.html
     */
    public static boolean isNonProxyHost(String targetHost) {
        String nonProxyHosts = System.getProperty(HTTP_NON_PROXY_HOSTS);
        if (nonProxyHosts != null) {
            String[] splitted = nonProxyHosts.split("\\|");
            for (int i = 0; i < splitted.length; i++) {
                if (targetHost.matches(splitted[i])) return true;
            }
        }
        return false;
    }
}

