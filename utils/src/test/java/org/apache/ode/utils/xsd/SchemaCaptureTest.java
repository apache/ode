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
package org.apache.ode.utils.xsd;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ode.utils.StreamUtils;
import org.apache.ode.utils.TestResources;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.junit.Test;

/**
 * Test schema capture functionality.
 */
public class SchemaCaptureTest {
    private static Logger __log = LoggerFactory.getLogger(SchemaCaptureTest.class);

    @Test
    public void testSchemaCapture() throws Exception {
        __log.debug("GETTING RESOURCE " + TestResources.getRetailerSchema());
        InputStream xsdStream = TestResources.getRetailerSchema().openStream();
        byte[] data;
        try {
            data = StreamUtils.read(xsdStream);
        } finally {
            xsdStream.close();
        }

        Map<URI, byte[]> s = XSUtils.captureSchema(URI.create("schema.xsd"), data, new XMLEntityResolver() {
            public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
                XMLInputSource src = new XMLInputSource(resourceIdentifier);
                String literalUri = resourceIdentifier.getLiteralSystemId();

                if (literalUri != null) {
                    src.setByteStream(getClass().getClassLoader().getResourceAsStream(literalUri));
                }

                return src;
            }
        }, 0);
        // we expect the root schema and three includes
        __log.debug("loaded " + s.keySet());
        assertEquals(5, s.size());
    }

    /**
     * Test for ODE-1019, provided by Igor Vorobiov
     */
    @Test(expected = Exception.class)
    public void testSchemaCaptureException() throws Exception {
        InputStream xsdStream = new FileInputStream(TestResources.getRetailerSchema().getFile());
        byte[] data;
        try {
            data = StreamUtils.read(xsdStream);
        } finally {
            xsdStream.close();
        }
        XSUtils.captureSchema(URI.create("schema.xsd"), data, new XMLEntityResolver() {
            public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
                // !!! cause NullPointerException
                return null;
            }
        }, 0);

        __log.error("mustn't reach this place");
    }

}
