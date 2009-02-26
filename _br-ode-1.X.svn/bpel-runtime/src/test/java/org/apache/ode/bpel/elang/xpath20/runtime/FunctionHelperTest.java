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

import junit.framework.TestCase;
import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.dom.NodeWrapper;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

import javax.xml.xpath.XPathFunctionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class FunctionHelperTest extends TestCase {

    public void testExtractString() throws XPathFunctionException {
        List inputs = new ArrayList();
        final String data = " a dummy text with leading and trailing whitespaces ";

        // if a raw string is passed, the result must not be trimmed
        assertEquals(data, JaxpFunctionResolver.Helper.extractString(data));

        // else, the result mut be trimmed
        final String expectedResult = data.trim();
        Document doc = DOMUtils.newDocument();
        Text textNode = doc.createTextNode(data);
        inputs.add(textNode);
        inputs.add(Arrays.asList(textNode));

        DocumentWrapper dw = new DocumentWrapper(doc, "", new Configuration());
        NodeWrapper nw = dw.wrap(textNode);
        inputs.add(nw);

        for (int i = 0; i < inputs.size(); i++) {
            assertEquals(expectedResult, JaxpFunctionResolver.Helper.extractString(inputs.get(i)));
        }
    }

}
