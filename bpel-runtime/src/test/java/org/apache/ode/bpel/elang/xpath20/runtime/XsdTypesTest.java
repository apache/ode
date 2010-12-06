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

package org.apache.ode.bpel.elang.xpath20.runtime;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.xpath.XPathFactoryImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.DOMUtils;

import junit.framework.TestCase;

public class XsdTypesTest extends TestCase {
    private static Log __log = LogFactory.getLog(XsdTypesTest.class);

    public void testDateTime() throws Exception {
        XPathFactoryImpl xpf = new XPathFactoryImpl();
        JaxpVariableResolver jvr = new JaxpVariableResolver(null, null, xpf.getConfiguration());
        Object o = jvr.getSimpleContent(DOMUtils.stringToDOM("<temporary-simple-type-wrapper>2010-01-25T15:38:54.82Z</temporary-simple-type-wrapper>"), QName.valueOf("{http://www.w3.org/2001/XMLSchema}dateTime"));
        __log.debug(o);
        assertTrue(o.toString().contains("2010-01-25T15:38:54.82Z"));
    }

    public void testEmptyDateTime() throws Exception {
        XPathFactoryImpl xpf = new XPathFactoryImpl();
        JaxpVariableResolver jvr = new JaxpVariableResolver(null, null, xpf.getConfiguration());
        Object o = jvr.getSimpleContent(DOMUtils.stringToDOM("<temporary-simple-type-wrapper></temporary-simple-type-wrapper>"), QName.valueOf("{http://www.w3.org/2001/XMLSchema}dateTime"));
        __log.debug(o);
        assertTrue(o.toString().equals(""));
    }
}
