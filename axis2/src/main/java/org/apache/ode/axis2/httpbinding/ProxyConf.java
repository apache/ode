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
                if (match(splitted[i], targetHost, false)) return true;
            }
        }
        return false;
    }
    
    /**
     * Matches a string against a pattern. The pattern contains two special
     * characters:
     * '*' which means zero or more characters,
     *
     * @param pattern the (non-null) pattern to match against
     * @param str     the (non-null) string that must be matched against the
     *                pattern
     * @param isCaseSensitive
     *
     * @return <code>true</code> when the string matches against the pattern,
     *         <code>false</code> otherwise.
     */
    protected static boolean match(String pattern, String str,
                                   boolean isCaseSensitive) {

        char[] patArr = pattern.toCharArray();
        char[] strArr = str.toCharArray();
        int patIdxStart = 0;
        int patIdxEnd = patArr.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strArr.length - 1;
        char ch;
        boolean containsStar = false;

        for (int i = 0; i < patArr.length; i++) {
            if (patArr[i] == '*') {
                containsStar = true;
                break;
            }
        }
        if (!containsStar) {

            // No '*'s, so we make a shortcut
            if (patIdxEnd != strIdxEnd) {
                return false;        // Pattern and string do not have the same size
            }
            for (int i = 0; i <= patIdxEnd; i++) {
                ch = patArr[i];
                if (isCaseSensitive && (ch != strArr[i])) {
                    return false;    // Character mismatch
                }
                if (!isCaseSensitive
                        && (Character.toUpperCase(ch)
                        != Character.toUpperCase(strArr[i]))) {
                    return false;    // Character mismatch
                }
            }
            return true;             // String matches against pattern
        }
        if (patIdxEnd == 0) {
            return true;    // Pattern contains only '*', which matches anything
        }

        // Process characters before first star
        while ((ch = patArr[patIdxStart]) != '*'
                && (strIdxStart <= strIdxEnd)) {
            if (isCaseSensitive && (ch != strArr[strIdxStart])) {
                return false;    // Character mismatch
            }
            if (!isCaseSensitive
                    && (Character.toUpperCase(ch)
                    != Character.toUpperCase(strArr[strIdxStart]))) {
                return false;    // Character mismatch
            }
            patIdxStart++;
            strIdxStart++;
        }
        if (strIdxStart > strIdxEnd) {

            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }
            return true;
        }

        // Process characters after last star
        while ((ch = patArr[patIdxEnd]) != '*' && (strIdxStart <= strIdxEnd)) {
            if (isCaseSensitive && (ch != strArr[strIdxEnd])) {
                return false;    // Character mismatch
            }
            if (!isCaseSensitive
                    && (Character.toUpperCase(ch)
                    != Character.toUpperCase(strArr[strIdxEnd]))) {
                return false;    // Character mismatch
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if (strIdxStart > strIdxEnd) {

            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }
            return true;
        }

        // process pattern between stars. padIdxStart and patIdxEnd point
        // always to a '*'.
        while ((patIdxStart != patIdxEnd) && (strIdxStart <= strIdxEnd)) {
            int patIdxTmp = -1;

            for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
                if (patArr[i] == '*') {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == patIdxStart + 1) {

                // Two stars next to each other, skip the first one.
                patIdxStart++;
                continue;
            }

            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patIdxStart - 1);
            int strLength = (strIdxEnd - strIdxStart + 1);
            int foundIdx = -1;

            strLoop:
            for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    ch = patArr[patIdxStart + j + 1];
                    if (isCaseSensitive
                            && (ch != strArr[strIdxStart + i + j])) {
                        continue strLoop;
                    }
                    if (!isCaseSensitive && (Character
                            .toUpperCase(ch) != Character
                            .toUpperCase(strArr[strIdxStart + i + j]))) {
                        continue strLoop;
                    }
                }
                foundIdx = strIdxStart + i;
                break;
            }
            if (foundIdx == -1) {
                return false;
            }
            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }

        // All characters in the string are used. Check if only '*'s are left
        // in the pattern. If so, we succeeded. Otherwise failure.
        for (int i = patIdxStart; i <= patIdxEnd; i++) {
            if (patArr[i] != '*') {
                return false;
            }
        }
        return true;
    }
}

