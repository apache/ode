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
import java.util.*;
import java.net.URISyntaxException;

import org.apache.ode.utils.fs.FileUtils;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class HierarchicalPropertiesTest extends TestCase {
    protected HierarchicalProperties hp;

    protected void setUp() throws Exception {
        // one config step happens in setUp()
        System.setProperty("TestSystemProperty", "42");
        File[] files = new File[]{new File(getClass().getResource("/hierarchical-2.properties").toURI()),
                new File(getClass().getResource("/hierarchical-1.properties").toURI())};
        hp = new HierarchicalProperties(files);
    }

    private void checkEnv() {
        // Java does not let us set ENV variables. The test avriable must be set by the process running this test.
        if (System.getenv("TEST_DUMMY_ENV_VAR") == null) {
            fail("Test configuration issue: for testing purposes, the environment variable TEST_DUMMY_ENV_VAR must be set to 42");
        }
    }


    public void testGetProperty() {
        String msg = "Returned value does not match expected value for this property!";
        assertEquals(msg, "30", hp.getProperty("max-redirects"));
        assertEquals(msg, "40", hp.getProperty("http://bar.com", "brel-service", "max-redirects"));
        assertEquals(msg, "60", hp.getProperty("http://bar.com", "brel-service", "port-of-amsterdam", "max-redirects"));
        assertEquals(msg, "40000", hp.getProperty("http://bar.com", "brel-service", "port-of-amsterdam", "timeout"));
        assertEquals(msg, "40000", hp.getProperty("http://foo.com", "film-service", "timeout"));
        assertEquals(msg, "hi!", hp.getProperty("http://hello.com", "a_service", "worldproperty"));
        assertEquals(msg, "4", hp.getProperty("a_namespace_with_no_alias", "a_service", "poolsize"));
        assertEquals("If the same property is set by two different files, the last loaded file must take precedence", "50000", hp.getProperty("http://foo.com", "film-service", "port-of-cannes", "timeout"));
        assertEquals("The prefix could be use without interfering", "so green or red?", hp.getProperty("ode.a.property.beginning.with.the.prefix.but.no.service"));
    }

    public void testGetProperties() {
        Map map = hp.getProperties("http://foo.com", "film-service");
        assertEquals("40000", map.get("timeout"));
        assertEquals("30", map.get("max-redirects"));
        assertEquals("so green or red?", map.get("ode.a.property.beginning.with.the.prefix.but.no.service"));
    }


    public void testCachedGetProperties() {
        assertSame("Snapshot maps should be cached!", hp.getProperties("foo", "film-service"), hp.getProperties("foo", "film-service"));
        assertSame("Snapshot maps should be cached!", hp.getProperties("foo", "film-service", "port-of-cannes"), hp.getProperties("foo", "film-service", "port-of-cannes"));
        assertSame("Snapshot maps should be cached!", hp.getProperties("bla", "unknown-service"), hp.getProperties("bla", "unknown-service"));
    }

    public void testPathHandling() {
        assertTrue("If the property name ends with '.file' or '.path' its value might be resolved against the file path", FileUtils.isAbsolute(hp.getProperty("http://foo.com", "film-service", "port-of-cannes", "p1.file")));
        assertTrue("If the property name ends with '.file' or '.path' its value might be resolved against the file path", FileUtils.isAbsolute(hp.getProperty("http://foo.com", "film-service", "port-of-cannes", "p1.path")));
        assertEquals("An absolute path should not be altered", "/home/ode/hello.txt", hp.getProperty("http://foo.com", "film-service", "port-of-cannes", "p2.path"));

    }

    public void testReservedNames() {
        String s = "Property files cannot define properties starting with ";
        try {
            HierarchicalProperties f = new HierarchicalProperties(new File(getClass().getResource("/hierarchical-bad.properties").toURI()));
            fail(s);
        } catch (Exception e) {
            assertTrue(s, e.getMessage().contains(s));
            assertTrue(s, e.getMessage().contains("system.foo"));
            assertTrue(s, e.getMessage().contains("env.BAR"));
        }
    }

    public void testReplaceSystemProperty() {
        // one config step happens in setUp()
        assertEquals("${system.*} must be replaced by the corresponding system property.", System.getProperty("TestSystemProperty"), hp.getProperty("http://bar.com", "brel-service", "sys.property"));
    }

    public void testReplaceEnvVariable() {
        checkEnv();
        assertEquals("${env.*} must be replaced by the corresponding environment variable.", System.getenv("TEST_DUMMY_ENV_VAR"), hp.getProperty("http://bar.com", "brel-service", "environment.property"));
    }

    public void test_placeholder() {
        assertEquals("A placeholder must be replaced by its value", "placeholder1-value", hp.getProperty("test.placeholder1"));
    }

    public void test_placeholder_that_uses_sys_prop_and_env_var() {
        checkEnv();
        assertEquals("A placeholder can use system properties and/or environment variables", System.getProperty("TestSystemProperty") + "#" + System.getenv("TEST_DUMMY_ENV_VAR"), hp.getProperty("test.placeholder2"));
    }

    public void test_placeholder_defined_in_2_files() {
        assertEquals("Last loaded placeholder value must overridden any previous values", "placeholder3-value", hp.getProperty("http://foo.com", "film-service", "port-of-cannes","test.placeholder3"));
    }

    public void testWithNoFile() throws IOException {
        File file = new File("/a-file-that-does-not-exist");
        Map m = new HierarchicalProperties(file).getProperties("an-uri", "a-service", "a-port");
        assertEquals("Should be empty", 0, m.size());
    }

}
