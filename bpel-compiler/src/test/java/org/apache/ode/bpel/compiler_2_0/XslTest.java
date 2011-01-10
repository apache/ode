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
package org.apache.ode.bpel.compiler_2_0;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.BpelCompiler20;
import org.apache.ode.bpel.compiler.DefaultResourceFinder;
import org.apache.ode.bpel.compiler.ResourceFinder;
import org.apache.ode.bpel.compiler.bom.BpelObjectFactory;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OXslSheet;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.StreamUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;


public class XslTest {

    private final Log __log = LogFactory.getLog(getClass());
    private BpelCompiler20 _compiler;
    private ResourceFinder _resfinder;

    @Before
    public void setUp() throws Exception {
        _compiler = new BpelCompiler20();
        File dir = new File(getClass().getResource(".").toURI());
        _resfinder = new DefaultResourceFinder(dir, dir);
        _compiler.setResourceFinder(_resfinder);
    }
    
    @After
    public void tearDown() throws Exception {
        _compiler = null;
    }
    
    @Test
    public void testUTFEncoding() throws Exception {
        Document original = DOMUtils.parse(getClass().getResourceAsStream("/xslt/test-utf8.xslt"));
        OProcess op = compile("xsl-utf8");
        OXslSheet sheet = op.xslSheets.get(URI.create("test-utf8.xslt"));
        Assert.assertNotNull(sheet);
        Assert.assertEquals(DOMUtils.domToString(original), sheet.sheetBody);        
    }

    @Test
    public void testISOEncoding() throws Exception {
        Document original = DOMUtils.parse(getClass().getResourceAsStream("/xslt/test-iso.xslt"));
        OProcess op = compile("xsl-iso");
        OXslSheet sheet = op.xslSheets.get(URI.create("test-iso.xslt"));
        Assert.assertNotNull(sheet);
        Assert.assertEquals(DOMUtils.domToString(original), sheet.sheetBody);
    }

    private OProcess compile(String bpelFile) throws Exception {
        URL bpelURL = getClass().getResource("/xslt/" + bpelFile + ".bpel");

        InputSource isrc = new InputSource(bpelURL.openStream());
        isrc.setSystemId(bpelURL.toExternalForm());

        org.apache.ode.bpel.compiler.bom.Process process = BpelObjectFactory.getInstance().parse(isrc, bpelURL.toURI());

        return _compiler.compile(process, _resfinder, 0);
    }
}
