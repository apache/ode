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

import javax.xml.xpath.XPathFunctionException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class CombineUrlTest extends TestCase {

    public void testCombineUrl() throws XPathFunctionException {
        final String baseURL = "http://WebReference.com/html/";

        // key: the relative URL,  value: the expected result
        Map m = new TreeMap();

        // relative-path references
        m.put("about.html", "http://WebReference.com/html/about.html");
        m.put("tutorial1/", "http://WebReference.com/html/tutorial1/");
        m.put("tutorial1/2.html", "http://WebReference.com/html/tutorial1/2.html");
        m.put("../", "http://WebReference.com/");
        m.put("../experts/", "http://WebReference.com/experts/");
        m.put("./", "http://WebReference.com/html/");
        m.put("./about.html", "http://WebReference.com/html/about.html");

        // absolute-path references
        m.put("/", "http://WebReference.com/");
        m.put("/experts/", "http://WebReference.com/experts/");

        // network-path references
        m.put("//www.internet.com/", "http://www.internet.com/");

        // absolute urls
        m.put("http://www.google.com", "http://www.google.com");
        m.put("file://mybox/dev/null", "file://mybox/dev/null");

        /*
        m.put("../../../", "http://WebReference.com/");
        m.put("../../../", "http://WebReference.com/../../");

         This case is considered as an error by rfc2396.
         but several options are left to the implementation.
         So the result is implementation specific.
         see section 5.2, step 6.g

         "Implementations may handle this error
         by retaining these components in the resolved path (i.e.,
         treating them as part of the final URI), by removing them from
         the resolved path (i.e., discarding relative levels above the
         root), or by avoiding traversal of the reference."

         http://www.ietf.org/rfc/rfc2396.txt
         */

        final JaxpFunctionResolver.CombineUrl f = new JaxpFunctionResolver.CombineUrl();

        for (Iterator it = m.entrySet().iterator(); it.hasNext();) {
            Map.Entry e = (Map.Entry) it.next();
            assertEquals(e.getValue(), f.evaluate(Arrays.asList(baseURL, e.getKey())));
        }
    }

}
