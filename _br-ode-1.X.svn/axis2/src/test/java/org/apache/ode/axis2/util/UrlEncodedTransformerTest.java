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

package org.apache.ode.axis2.util;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.ode.utils.DOMUtils;

import java.util.Map;
import java.util.HashMap;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class UrlEncodedTransformerTest extends TestCase {
    protected URLEncodedTransformer transformer = new URLEncodedTransformer();


    public void testSimple() {
        Document doc = DOMUtils.newDocument();
        Map<String, Element> m = new HashMap<String, Element>();
        Element element = doc.createElement("part1");
        element.setTextContent("42 3.14159");
        m.put("part1", element);

        element = doc.createElement("part2");
        element.setTextContent("hello word @#$% &*");
        m.put("part2", element);

        element = doc.createElement("emptyPart");
        m.put("emptyPart", element);

        String res = transformer.transform(m);
        assertTrue(res.contains("part1=42+3.14159"));
        assertTrue(res.contains("part2=hello+word+%40%23%24%25+%26*"));
        assertTrue(res.contains("emptyPart="));
        assertTrue(res.split("&").length == m.size());
    }


    public void testComplexType() {
        Document doc = DOMUtils.newDocument();
        Element element = doc.createElement("part1");
        element.appendChild(doc.createElement("kid"));
        Map<String, Element> m = new HashMap<String, Element>();
        m.put("part1", element);

        assertEquals("Result should empty because the only part is associated to a complex type", "", transformer.transform(m));
    }
}
