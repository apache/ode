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

package org.apache.ode.utils.http;

import java.util.regex.Pattern;

/**
 *
 *
 */
public class HttpUtils {
    public static final String XML_MIME_TYPE_REGEX = "((text/xml)|(application/xml)|((.*)\\+xml))(;.*)*";
    public static final String TEXT_MIME_TYPE_REGEX = "text/(?!xml(;(.*)*))(.*)";
    public static final Pattern XML_MIME_TYPE_PATTERN = Pattern.compile(XML_MIME_TYPE_REGEX);
    public static final Pattern TEXT_MIME_TYPE_PATTERN = Pattern.compile(TEXT_MIME_TYPE_REGEX);

    public static boolean isXml(String contentType) {
        return XML_MIME_TYPE_PATTERN.matcher(contentType).matches();
    }

    public static boolean isText(String contentType) {
        return TEXT_MIME_TYPE_PATTERN.matcher(contentType).matches();
    }

    /**
     * Per <a href="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616 section 4.3</a>, some responses can never contain a message
     * body.
     *
     * @param status - the HTTP status code
     * @return <tt>true</tt> if the message may contain a body, <tt>false</tt> if it can not
     *         contain a message body
     */
    public static boolean bodyAllowed(int status) {
        if (status >= 100 && status < 200
                || status == StatusCode._204_NO_CONTENT
                || status == StatusCode._304_NOT_MODIFIED) {
            return false;
        }
        return true;
    }
}
