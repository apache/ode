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

import java.util.*;

import org.apache.commons.httpclient.URIException;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class URITemplateTest extends TestCase {
    private static final String EXCEPTION_EXPECTED = "ExceptionExpected";

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testParseExpansion(){
        Object[] expansionPatterns = new Object[]{
                "{my_var}",  new Object[]{null, null, new HashMap(){{put("my_var",null);}}},
                "{my_var=my_default}",  new Object[]{null, null, new HashMap(){{put("my_var","my_default");}}},
                "{-suffix|/|foo}",  new Object[]{"suffix", "/", new HashMap(){{put("foo",null);}}},
                "{-opt|fred@example.org|foo}",  new Object[]{"opt", "fred@example.org", new HashMap(){{put("foo",null);}}}
        };

        for (int i = 0; i < expansionPatterns.length; i=i+2) {
            String patternInfo = (String) expansionPatterns[i];
            Object[] computedResult = URITemplate.parseExpansion(patternInfo);
            Object[] expectedResult = (Object[]) expansionPatterns[i + 1];
            assertEquals("Unexpected operation", expectedResult[0], computedResult[0]);
            assertEquals("Unexpected argument", expectedResult[1], computedResult[1]);
            Map expectedVarMap = (Map) expectedResult[2];
            Map computedVarMap = (Map) computedResult[2];
            assertEquals("Var map do not have the number of elements", expectedVarMap.size(), computedVarMap.size());
            for (Iterator it = expectedVarMap.entrySet().iterator(); it.hasNext();) {
                Map.Entry e = (Map.Entry) it.next();
                assertEquals("Different Value!", e.getValue(), computedVarMap.get(e.getKey()));
            }
        }
    }


    public void testExpand() throws Exception {
        // template, input name/value array, expected result
        Object[] templates = new Object[]{
                "{a}", new String[]{"a", "hello"}, "hello"
                ,"{a}", new String[]{"var_not_in_template", "hello"}, "{a}"
                ,"{a=3}", new String[]{"var_not_in_template", "hello"}, "3" // with a default value
                ,"hello {name}!", new String[]{"name", null}, "hello !"  // null value
                ,"hello {name=darling}!", new String[]{"name", null}, "hello darling!"  // null value and a default
                ,"hello {name=darling}!", new String[]{"name", "brother"}, "hello brother!"
                ,"hello {name=darling}! what's {this}?", new String[]{"name", "brother", "this", "this"}, "hello brother! what's this?"
                ,"hello {name=darling}! what's {this}?", new String[]{"name", "brother", "this", "wrong"}, "hello brother! what's wrong?"
                ,"hello {name=darling}! what's {this}?", new String[]{"name", "brother", "this", null}, "hello brother! what's ?"
                ,"hello {name=darling}! what's {this=up}?", new String[]{"name", "brother", "this", null}, "hello brother! what's up?"
                ,"hello {name=darling}! what's {this=up}?", new String[]{"name", "brother"}, "hello brother! what's up?"
                ,"hello {name}! what's {this}?", new String[]{"var_not_in_template", "foo"}, "hello ! what's ?"
                ,"hello{name}what's{this}", new String[]{"name", " brother! ", "this", " this?"}, "hello%20brother!%20what's%20this%3F" // test encoding
                ,"hello{name= brother! }what's{this}", new String[]{"this", " this?"}, "hello%20brother!%20what's%20this%3F" // test encoding + default value
                ,"hello {name=darling}! what's {this}?", new String[]{"name", "brother", "this", "{wrong}"}, "hello brother! what's %7Bwrong%7D?"
                ,"hello%20brother!%20what's{this}", new String[]{"this", " this?"}, "hello%20brother!%20what's%20this%3F" // test template of template
                ,"{this}", new String[]{"this", ";/?:@&=+,$"}, "%3B%2F%3F%3A%40%26%3D%2B%2C%24" // reserved characters within a query
                ,"{this}", new String[]{"this", "somereserved%;/?:@&=+,$allunreserved-_.!~*'()"}, "somereserved%25%3B%2F%3F%3A%40%26%3D%2B%2C%24allunreserved-_.!~*'()" // reserved characters within a query
                // the followings are included in the javadoc as examples
                , "http://example.com/{foo}/{bar}.{format=xml}", new String[]{"foo", "tag", "bar", "java", "name", null, "date", "2008/05/09"},"http://example.com/tag/java.xml" // undefined var with a default value
                , "http://example.com/tag/java.{format}", new String[]{"foo", "tag", "bar", "java", "name", null, "date", "2008/05/09"},"http://example.com/tag/java." // undefined and no default
                , "http://example.com/{foo}/{name}", new String[]{"foo", "tag", "bar", "java", "name", null, "date", "2008/05/09"},"http://example.com/tag/"
                , "http://example.com/{foo}/{name=james}", new String[]{"foo", "tag", "bar", "java", "name", null, "date", "2008/05/09"},"http://example.com/tag/james"
                , "http://example.org/{date}", new String[]{"foo", "tag", "bar", "java", "name", null, "date", "2008/05/09"}, "http://example.org/2008%2F05%2F09"
                , "http://example.org/{-join|&|foo,bar,xyzzy,baz}/{date}", new String[]{"foo", "tag", "bar", "java", "name", null, "date", "2008/05/09"}, EXCEPTION_EXPECTED
        };
        for (int i = 0; i < templates.length; i = i + 3) {
            String template = (String) templates[i];
            String[] pairs = (String[]) templates[i + 1];
            String expected = (String) templates[i + 2];
            String computed = null;
            try {
                computed = URITemplate.expand(template, pairs);
                if (EXCEPTION_EXPECTED.equals(expected)) {
                    fail("An exception was supposed to be thrown!");
                } else {
                    assertEquals("Test #" + ((i / 3) + 1) + ": Result does not match expectation.", expected, computed);
                }
            } catch (Exception e) {
                if (!EXCEPTION_EXPECTED.equals(expected)) {
                    // this exception was NOT expected!
                    throw e;
                }
            }
        }
    }

    public void testExpandLazily() throws Exception {
        // same but with some undefined vars
        // template, input name/value array, expected result
        Object[] templates = new Object[]{
                "http://example.com/{foo}/{bar}.{format}", new String[]{"foo", "tag", "bar", "java", "name", null, "date", "2008/05/09"},"http://example.com/tag/java.{format}" // undefined var with no default value
                , "http://example.com/{foo}/{name}", new String[]{"bar", "java", "date", "2008/05/09"},"http://example.com/{foo}/{name}"
                , "http://example.com/{foo}/{name=james}", new String[]{"bar", "java", "name", null, "date", "2008/05/09"},"http://example.com/{foo}/james"
        };
        for (int i = 0; i < templates.length; i = i + 3) {
            String template = (String) templates[i];
            String[] pairs = (String[]) templates[i + 1];
            String expected = (String) templates[i + 2];
            String computed = null;
            try {
                computed = URITemplate.expandLazily(template, pairs);
                if (EXCEPTION_EXPECTED.equals(expected)) {
                    fail("An exception was supposed to be thrown!");
                } else {
                    assertEquals("Test #" + ((i / 3) + 1) + ": Result does not match expectation.", expected, computed);
                }
            } catch (Exception e) {
                if (!EXCEPTION_EXPECTED.equals(expected)) {
                    // this exception was NOT expected!
                    throw e;
                }
            }
        }
    }
}
