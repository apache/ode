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

package org.apache.ode.utils;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class HierarchiedPropertiesTest extends TestCase {
    protected HierarchiedProperties hp;

    protected void setUp() throws Exception {
        File file = new File(getClass().getResource("/hierarchied.properties").toURI());
        hp = new HierarchiedProperties(file);
    }

    public void testGetProperty() {
        String msg = "Returned value does not match expected value for this property!";
        assertEquals(msg, hp.getProperty("max-redirects"), "30");
        assertEquals(msg, hp.getProperty("brel-service", "max-redirects"), "40");
        assertEquals(msg, hp.getProperty("brel-service", "port-of-amsterdam", "max-redirects"), "60");
        assertEquals(msg, hp.getProperty("film-service", "timeout"), "40000");
        assertEquals(msg, hp.getProperty("film-service", "port-of-cannes", "timeout"), "50000");
        assertEquals(msg, hp.getProperty("brel-service", "port-of-amsterdam", "timeout"), "40000");
        assertEquals(msg, hp.getProperty("ode.a.property.beginning.with.the.prefix.but.no.service"), "so green or red?");
    }

    public void testGetProperties(){
        final List keys = Arrays.asList("timeout", "max-redirects", "ode.a.property.beginning.with.the.prefix.but.no.service");
        Map map = hp.getProperties("film-service");
        assertEquals("Number of properties is wrong",keys.size(), map.size());
        assertEquals("40000", map.get("timeout"));
        assertEquals("30", map.get("max-redirects"));
        assertEquals("so green or red?", map.get("ode.a.property.beginning.with.the.prefix.but.no.service"));
    }


    public void testCachedGetProperties(){
        assertSame("Snapshot maps should be cached!", hp.getProperties("film-service"), hp.getProperties("film-service"));
        assertSame("Snapshot maps should be cached!", hp.getProperties("film-service", "port-of-cannes"), hp.getProperties("film-service", "port-of-cannes"));
        assertSame("Snapshot maps should be cached!", hp.getProperties("unknown-service"), hp.getProperties("unknown-service"));
    }

    public void testWithNoFile() throws IOException {
        File file = new File("/a-file-that-does-not-exist");
        Map m = new HierarchiedProperties(file).getProperties("a-service", "a-port");
        assertEquals("Should be empty", 0, m.size());
    }

}
