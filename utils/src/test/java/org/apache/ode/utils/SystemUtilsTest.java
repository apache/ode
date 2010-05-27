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

/**
 * Test the {@link SystemUtils} class.
 */
public class SystemUtilsTest extends TestCase {

    static final String PROP = "org.apache.ode.test.property";

    static final String REPLACE = "${" + PROP + "}";

    static final String VALUE = "foo\bar$bar";

    public void setUp() {
        System.getProperties().setProperty(PROP, VALUE);
    }

    public void teardown() {
        System.getProperties().setProperty(PROP, null);
    }

    public void testReplaceSystemProperties() throws Exception {
        // no replacement
        assertReplace("xxx", "xxx");

        // no substitutions for special characters
        assertReplace("\\aaa\\\\bbb$$$ccc$", "\\aaa\\\\bbb$$$ccc$");

        // empty replacement
        assertReplace("${}", "${}");

        // as-is
        assertReplace(REPLACE, VALUE);

        // before
        assertReplace(REPLACE + "foo", VALUE + "foo");

        // after
        assertReplace("foo" + REPLACE, "foo" + VALUE);

        // before+after
        assertReplace(REPLACE + "foo" + REPLACE, VALUE + "foo" + VALUE);

        // in-between
        assertReplace("foo" + REPLACE + "bar", "foo" + VALUE + "bar");

        // multiple occurence
        assertReplace(" " + REPLACE + " " + REPLACE + " " + REPLACE, " "
                + VALUE + " " + VALUE + " " + VALUE);

        // undefined
        assertReplace(" ${undefined.property} ", " ${undefined.property} ");

        // stacked
        assertReplace(REPLACE + REPLACE, VALUE + VALUE);
    }

    static void assertReplace(String str, String expected) throws Exception {
        String actual = SystemUtils.replaceSystemProperties(str);
        assertEquals(expected, actual);
    }
}
