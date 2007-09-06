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

import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

/**
 * HTTP Authentication Helper
 *
 * @author Alex Boisvert <boisvert at apache dot org>
 */
public class AuthenticationHelper {

    private static final Log __log = LogFactory.getLog(AuthenticationHelper.class);

    private static final String AUTHENTICATE_ELEMENT = "authenticate";

    private static final String AUTHENTICATION_NS = "urn:ode.apache.org/authentication";

    private static final String USERNAME_ELEMENT = "username";

    private static final String PASSWORD_ELEMENT = "password";

    private static final String DOMAIN_ELEMENT = "domain";

    private static final String REALM_ELEMENT = "realm";

    private static final String TOKEN_ELEMENT = "token";

    public static void setHttpAuthentication(PartnerRoleMessageExchange odeMex, Options options) {
        Element msg = odeMex.getRequest().getMessage();
        if (msg != null) {
            Element part = DOMUtils.getFirstChildElement(msg);
            while (part != null) {
                Element content = DOMUtils.getFirstChildElement(part);
                if (content != null) {
                    if (AUTHENTICATION_NS.equals(content.getNamespaceURI()) && AUTHENTICATE_ELEMENT.equals(content.getLocalName())) {
                        setOptions(options, content);
                        msg.removeChild(part);
                        break;
                    }
                }
                part = DOMUtils.getNextSiblingElement(part);
            }
        }
    }

    protected static void setOptions(Options options, Element auth) {
        String username = null;
        String password = null;
        String domain = null;
        String realm = null;
        Element e = DOMUtils.getFirstChildElement(auth);
        while (e != null) {
            if (USERNAME_ELEMENT.equals(e.getLocalName())) {
                username = DOMUtils.getTextContent(e);
            }
            if (PASSWORD_ELEMENT.equals(e.getLocalName())) {
                password = DOMUtils.getTextContent(e);
            }
            if (DOMAIN_ELEMENT.equals(e.getLocalName())) {
                domain = DOMUtils.getTextContent(e);
            }
            if (REALM_ELEMENT.equals(e.getLocalName())) {
                realm = DOMUtils.getTextContent(e);
            }
            e = DOMUtils.getNextSiblingElement(e);
        }

        HttpTransportProperties.Authenticator authenticator = new HttpTransportProperties.Authenticator();
        authenticator.setUsername(username);
        authenticator.setPassword(password);
        authenticator.setDomain(domain);
        authenticator.setRealm(realm);
        options.setProperty(HTTPConstants.AUTHENTICATE, authenticator);
    }

}
