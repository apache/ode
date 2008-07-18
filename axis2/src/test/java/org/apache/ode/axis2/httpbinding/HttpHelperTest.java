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

import junit.framework.TestCase;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;

/**
 *
 *
 */
public class HttpHelperTest extends TestCase {
    private static final String[] IS_XML = new String[]{
            "application/atom+xml"
            , "text/xml"
            , "application/xml"
            , "application/xhtml+xml"
            , "image/foo+xml"
    };
    private static final String[] IS_TEXT = new String[]{
            "text/xml-external-parsed-entity"
            , "text/plain"
            , "text/css"
            , "text/csv"
    };
    private static final String[] IS_IMAGE = new String[]{
            "image/jpeg"
            , "image/png"
            , "image/gif"
    };


    public void testIsXml() {

        for (String s : IS_XML) {
            assertTrue(s+" is an xml type", HttpHelper.isXml(s));
        }
        for (String s : IS_TEXT) {
            assertFalse(s+" is not an xml type", HttpHelper.isXml(s));
        }
        for (String s : IS_IMAGE) {
            assertFalse(s+" is not an xml type", HttpHelper.isXml(s));
        }

    }

    public void testIsText() {
        for (String s : IS_TEXT) {
            assertTrue(s+" is a text type", HttpHelper.isText(s));
        }
        for (String s : IS_XML) {
            assertFalse(s+" is not a text type", HttpHelper.isText(s));
        }
        for (String s : IS_IMAGE) {
            assertFalse(s+" is not a text type", HttpHelper.isXml(s));
        }
    }
}