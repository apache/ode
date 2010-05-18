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
 * <a href="http://www.ietf.org/rfc/rfc2616.txt">HTTP/1.1 Status Codes</a>
 */
public class StatusCode {


    // --- 1xx Informational ---

    /** <tt>100 Continue</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _100_CONTINUE = 100;
    /** <tt>101 Switching Protocols</tt> (HTTP/1.1 - RFC 2616)*/
    public static final int _101_SWITCHING_PROTOCOLS = 101;

    // --- 2xx Success ---

    /** <tt>200 OK</tt> (HTTP/1.0 - RFC 1945) */
    public static final int _200_OK = 200;
    /** <tt>201 Created</tt> (HTTP/1.0 - RFC 1945) */
    public static final int _201_CREATED = 201;
    /** <tt>202 Accepted</tt> (HTTP/1.0 - RFC 1945) */
    public static final int _202_ACCEPTED = 202;
    /** <tt>203 Non Authoritative Information</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _203_NON_AUTHORITATIVE_INFORMATION = 203;
    /** <tt>204 No Content</tt> (HTTP/1.0 - RFC 1945) */
    public static final int _204_NO_CONTENT = 204;
    /** <tt>205 Reset Content</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _205_RESET_CONTENT = 205;
    /** <tt>206 Partial Content</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _206_PARTIAL_CONTENT = 206;

    // --- 3xx Redirection ---

    /** <tt>300 Mutliple Choices</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _300_MULTIPLE_CHOICES = 300;
    /** <tt>301 Moved Permanently</tt> (HTTP/1.0 - RFC 1945) */
    public static final int _301_MOVED_PERMANENTLY = 301;
    /** <tt>302 Moved Temporarily</tt> (Sometimes <tt>Found</tt>) (HTTP/1.0 - RFC 1945) */
    public static final int _302_MOVED_TEMPORARILY = 302;
    /** <tt>303 See Other</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _303_SEE_OTHER = 303;
    /** <tt>304 Not Modified</tt> (HTTP/1.0 - RFC 1945) */
    public static final int _304_NOT_MODIFIED = 304;
    /** <tt>305 Use Proxy</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _305_USE_PROXY = 305;
    /** <tt>307 Temporary Redirect</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _307_TEMPORARY_REDIRECT = 307;

    // --- 4xx Client Error ---

    /** <tt>400 Bad Request</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _400_BAD_REQUEST = 400;
    /** <tt>401 Unauthorized</tt> (HTTP/1.0 - RFC 1945) */
    public static final int _401_UNAUTHORIZED = 401;
    /** <tt>402 Payment Required</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _402_PAYMENT_REQUIRED = 402;
    /** <tt>403 Forbidden</tt> (HTTP/1.0 - RFC 1945) */
    public static final int _403_FORBIDDEN = 403;
    /** <tt>404 Not Found</tt> (HTTP/1.0 - RFC 1945) */
    public static final int _404_NOT_FOUND = 404;
    /** <tt>405 Method Not Allowed</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _405_METHOD_NOT_ALLOWED = 405;
    /** <tt>406 Not Acceptable</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _406_NOT_ACCEPTABLE = 406;
    /** <tt>407 Proxy Authentication Required</tt> (HTTP/1.1 - RFC 2616)*/
    public static final int _407_PROXY_AUTHENTICATION_REQUIRED = 407;
    /** <tt>408 Request Timeout</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _408_REQUEST_TIMEOUT = 408;
    /** <tt>409 Conflict</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _409_CONFLICT = 409;
    /** <tt>410 Gone</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _410_GONE = 410;
    /** <tt>411 Length Required</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _411_LENGTH_REQUIRED = 411;
    /** <tt>412 Precondition Failed</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _412_PRECONDITION_FAILED = 412;
    /** <tt>413 Request Entity Too Large</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _413_REQUEST_TOO_LONG = 413;
    /** <tt>414 Request-URI Too Long</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _414_REQUEST_URI_TOO_LONG = 414;
    /** <tt>415 Unsupported Media Type</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _415_UNSUPPORTED_MEDIA_TYPE = 415;
    /** <tt>416 Requested Range Not Satisfiable</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _416_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    /** <tt>417 Expectation Failed</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _417_EXPECTATION_FAILED = 417;

    // --- 5xx Server Error ---

    /** <tt>500 Server Error</tt> (HTTP/1.0 - RFC 1945) */
    public static final int _500_INTERNAL_SERVER_ERROR = 500;
    /** <tt>501 Not Implemented</tt> (HTTP/1.0 - RFC 1945) */
    public static final int _501_NOT_IMPLEMENTED = 501;
    /** <tt>502 Bad Gateway</tt> (HTTP/1.0 - RFC 1945) */
    public static final int _502_BAD_GATEWAY = 502;
    /** <tt>503 Service Unavailable</tt> (HTTP/1.0 - RFC 1945) */
    public static final int _503_SERVICE_UNAVAILABLE = 503;
    /** <tt>504 Gateway Timeout</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _504_GATEWAY_TIMEOUT = 504;
    /** <tt>505 HTTP Version Not Supported</tt> (HTTP/1.1 - RFC 2616) */
    public static final int _505_HTTP_VERSION_NOT_SUPPORTED = 505;

}
