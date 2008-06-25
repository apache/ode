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
import org.apache.ode.axis2.util.UrlReplacementTransformer;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class UrlReplacementTransformerTest extends TestCase {


    public void testWithParentheses() {
        String[][] a = new String[][]{
                new String[]{"with alphabetical chars", "o1/A(part1)B(part2)/(part3)", "o1/AtutuBtiti/toto", "part1", "tutu", "part2", "titi", "part3", "toto"}
                , new String[]{"parts are ends", "(part1)B(part2)/(part3)", "3B14/159", "part1", "3", "part2", "14", "part3", "159"}
                , new String[]{"a single part", "(part1)", "314159", "part1", "314159"}
                , new String[]{"parts surrounded with ()", "o1/A((part1))B((part2))/((part3))", "o1/A(3)B(14)/(159)", "part1", "3", "part2", "14", "part3", "159"}
                , new String[]{"with numeric chars", "o1/A(part1)B(part2)/(part3)", "o1/A3B14/159%20and%20an%20epsilon", "part1", "3", "part2", "14", "part3", "159 and an epsilon"}
                , new String[]{"with empty values", "o1/A(part1)B(part2)/(part3)", "o1/AB/", "part1", "", "part2", "", "part3", ""}
                , new String[]{"with special chars", "o1/A(part1)B(part2)/(part3)", "o1/AWhatB%2410%2C000/~!%40%23%24%25%5E%26*()_%2B%3D-%60%5B%5D%7B%7D%7C%5C.", "part1", "What", "part2", "$10,000", "part3", "~!@#$%^&*()_+=-`[]{}|\\."}
                , new String[]{"with values containing key names", "o1/A(part1)B(part2)/(part3)", "o1/Avalue_of_part1_is_(part2)_and_should_not_be_replacedBsame_for_part2(part3)/foo", "part1", "value_of_part1_is_(part2)_and_should_not_be_replaced", "part2", "same_for_part2(part3)", "part3", "foo"}
        };

        Document doc = DOMUtils.newDocument();
        UrlReplacementTransformer encoder = new UrlReplacementTransformer();
        for (String[] data : a) {
            // convert into map
            Map<String, Element> parts = new HashMap<String, Element>();
            for (int k = 3; k < data.length; k = k + 2) {
                Element element = doc.createElement(data[k]);
                element.setTextContent(data[k + 1]);
                parts.put(data[k], element);
            }
            assertEquals(data[0], data[2], encoder.transform(data[1], parts));
        }
    }


    public void testWithBraces() {
        String[][] a = new String[][]{
                new String[]{"with alphabetical chars", "o1/A{part1}B{part2}/{part3}", "o1/AtutuBtiti/toto", "part1", "tutu", "part2", "titi", "part3", "toto"}
                , new String[]{"parts are ends", "{part1}B{part2}/{part3}", "3B14/159", "part1", "3", "part2", "14", "part3", "159"}
                , new String[]{"a single part", "{part1}", "314159", "part1", "314159"}
                , new String[]{"parts surrounded with {}", "o1/A{{part1}}B{{part2}}/{{part3}}", "o1/A{3}B{14}/{159}", "part1", "3", "part2", "14", "part3", "159"}
                , new String[]{"with numeric chars", "o1/A{part1}B{part2}/{part3}", "o1/A3B14/159%20and%20an%20epsilon", "part1", "3", "part2", "14", "part3", "159 and an epsilon"}
                , new String[]{"with empty values", "o1/A{part1}B{part2}/{part3}", "o1/AB/", "part1", "", "part2", "", "part3", ""}
                , new String[]{"with special chars", "o1/A{part1}B{part2}/{part3}", "o1/AWhatB%2410%2C000/~!%40%23%24%25%5E%26*()_%2B%3D-%60%5B%5D%7B%7D%7C%5C.", "part1", "What", "part2", "$10,000", "part3", "~!@#$%^&*()_+=-`[]{}|\\."}
                , new String[]{"with values containing key names", "o1/A{part1}B{part2}/{part3}", "o1/Avalue_of_part1_is_%7Bpart2%7D_and_should_not_be_replacedBsame_for_part2%7Bpart3%7D/foo", "part1", "value_of_part1_is_{part2}_and_should_not_be_replaced", "part2", "same_for_part2{part3}", "part3", "foo"}
        };

        Document doc = DOMUtils.newDocument();
        UrlReplacementTransformer encoder = new UrlReplacementTransformer();
        for (String[] data : a) {
            // convert into map
            Map<String, Element> parts = new HashMap<String, Element>();
            for (int k = 3; k < data.length; k = k + 2) {
                Element element = doc.createElement(data[k]);
                element.setTextContent(data[k + 1]);
                parts.put(data[k], element);
            }
            assertEquals(data[0], data[2], encoder.transform(data[1], parts));
        }

    }

    public void testWithMixnMatch() {
        String[][] a = new String[][]{
                new String[]{"with alphabetical chars", "o1/A(part1)B{part2}/{part3}", "o1/AtutuBtiti/toto", "part1", "tutu", "part2", "titi", "part3", "toto"}
                , new String[]{"parts are ends", "{part1}B{part2}/(part3)", "3B14/159", "part1", "3", "part2", "14", "part3", "159"}
                , new String[]{"a single part", "{part1}", "314159", "part1", "314159"}
                , new String[]{"with empty values", "o1/A{part1}B(part2)/{part3}", "o1/AB/", "part1", "", "part2", "", "part3", ""}
        };

        Document doc = DOMUtils.newDocument();
        UrlReplacementTransformer encoder = new UrlReplacementTransformer();
        for (String[] data : a) {
            // convert into map
            Map<String, Element> parts = new HashMap<String, Element>();
            for (int k = 3; k < data.length; k = k + 2) {
                Element element = doc.createElement(data[k]);
                element.setTextContent(data[k + 1]);
                parts.put(data[k], element);
            }
            assertEquals(data[0], data[2], encoder.transform(data[1], parts));
        }

    }


    public void testComplexType() {
        Document doc = DOMUtils.newDocument();
        Element element = doc.createElement("part1");
        element.appendChild(doc.createElement("kid"));
        Map<String, Element> m = new HashMap<String, Element>();
        m.put("part1", element);

        UrlReplacementTransformer encoder = new UrlReplacementTransformer();
        assertEquals("Result should be equal to template because the only part is associated to a complex type", "(part1)", encoder.transform("(part1)", m));

    }

}
