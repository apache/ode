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

import javax.wsdl.xml.WSDLReader;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.Definition;
import javax.wsdl.Binding;
import javax.xml.namespace.QName;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.apache.ode.utils.DOMUtils;
import org.apache.ode.axis2.httpbinding.HttpBindingValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class HttpBindingValidatorTest extends TestCase {

    private static final Log log = LogFactory.getLog(HttpBindingValidatorTest.class);

    private String[] resources = new String[]{"/http-binding-validator.wsdl", "/http-binding-validator-ext.wsdl"};
    private Definition[] definitions;
    private static final String SHOULD_FAIL = "shouldFail";
    private static final String SHOULD_PASS = "shouldPass";

    protected void setUp() throws Exception {
        super.setUp();

        WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
        wsdlReader.setFeature("javax.wsdl.verbose", false);
        definitions = new Definition[resources.length];
        for (int i = 0; i < resources.length; i++) {
            URL wsdlURL = getClass().getResource(resources[i]);
            definitions[i] = wsdlReader.readWSDL(wsdlURL.toURI().toString());
        }
    }

    public void testAll() {
        for (int i = 0; i < definitions.length; i++) {
            Definition def = definitions[i];
            for (Iterator it = def.getBindings().entrySet().iterator(); it.hasNext();) {
                Map.Entry e = (Map.Entry) it.next();
                QName name = (QName) e.getKey();
                String localName = name.getLocalPart();
                Binding binding = (Binding) e.getValue();
                Element documentationElement = binding.getDocumentationElement();
                if (documentationElement == null) {
                    log.warn("Binding skipped : " + localName + ", <wsdl:documentation> missing ");
                    continue;
                }
                String doc = DOMUtils.getTextContent(documentationElement);
                boolean shouldFail = doc.startsWith(SHOULD_FAIL);
                boolean shouldPass = doc.startsWith(SHOULD_PASS);
                if (!shouldFail && !shouldPass) {
                    fail("Binding: " + localName + ", <wsdl:documentation> content must start with '" + SHOULD_FAIL + "' or '" + SHOULD_PASS + "'. ");
                }

                log.debug("Testing Binding : " + localName);
                String msg = localName + " : " + doc;
                try {
                    new HttpBindingValidator(binding).validate();
                    assertTrue(msg, shouldPass);
                } catch (IllegalArgumentException e1) {
                    msg += " / Exception Msg is : " + e1.getMessage();
                    assertTrue(msg, shouldFail);
                }
            }
        }

    }
}
