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

package org.apache.ode.bpel.elang.xpath20.compiler;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.*;

public class XPath20ExpressionCompilerImplTest {

    private static final String TEST_NAMESPACE = "http://www.example.com/";
    private static final String EXTRACT_FUNCTION_EXPRS = "extractFunctionExprs";

    @Test
    public void testresolvedFunctionsExpr() throws Exception {
        XPath20ExpressionCompilerImpl xp20Exp = new XPath20ExpressionCompilerImpl(
                TEST_NAMESPACE);
        final Method[] methods = xp20Exp.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; ++i) {
            if (methods[i].getName().equals(EXTRACT_FUNCTION_EXPRS)) {
                final Object params[] = { "count(count(1))" };
                methods[i].setAccessible(true);
                Object ret = methods[i].invoke(xp20Exp, params);
                List<?> values = (List<?>) ret;
                Assert.assertEquals(1, values.size());
            }
        }
    }

    @Test
    public void testTimeStampInFunction() throws Exception {
        XPath20ExpressionCompilerImpl xp20Exp = new XPath20ExpressionCompilerImpl(
                TEST_NAMESPACE);
        final Method[] methods = xp20Exp.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; ++i) {
            if (methods[i].getName().equals(EXTRACT_FUNCTION_EXPRS)) {
                final Object params[] = { "concat(xs:concat(\"P\", \"08:30:00.000+08:00\"))" };
                methods[i].setAccessible(true);
                Object ret = methods[i].invoke(xp20Exp, params);
                List<?> values = (List<?>) ret;
                Assert.assertEquals(1, values.size());
            }
        }

    }

    @Test
    public void testresolvedFunctionsTimeStamp() throws Exception {
        XPath20ExpressionCompilerImpl xp20Exp = new XPath20ExpressionCompilerImpl(
                TEST_NAMESPACE);
        final Method[] methods = xp20Exp.getClass().getDeclaredMethods();
        for (int i = 0; i < methods.length; ++i) {
            if (methods[i].getName().equals(EXTRACT_FUNCTION_EXPRS)) {
                String multipleFunctions = "concat(current-date() + xs:dayTimeDuration(concat(\"P\", $DAYS_TO_NEXT_REMINDER, \"D\")), \"T\", \"08:30:00.000+08:00\")";
                final Object params[] = { multipleFunctions };
                methods[i].setAccessible(true);
                Object ret = methods[i].invoke(xp20Exp, params);
                List<?> values = (List<?>) ret;
                Assert.assertEquals(1, values.size());
                Assert.assertEquals("Unexpected Function value",
                        multipleFunctions, (String) values.get(0));
            }
        }
    }

    @Test
    public void testExtractFunctionsExprs() throws Exception {
        XPath20ExpressionCompilerImpl xp20Exp = new XPath20ExpressionCompilerImpl(
                TEST_NAMESPACE);
        final Method[] methods = xp20Exp.getClass().getDeclaredMethods();
        String ODE_840 = "bpel:doXslTransform(\"1.0.1/some.xsl\", $Variable.body, \"someParameter\", $OtherVariable.body, \"someParameter2\", $SwsHeaderRQ, \"someParameter3\", true(), \"someXpathParameter\", $XPath)";

        for (int i = 0; i < methods.length; ++i) {
            if (methods[i].getName().equals(EXTRACT_FUNCTION_EXPRS)) {
                final Object params[] = { ODE_840 };
                methods[i].setAccessible(true);
                Object ret = methods[i].invoke(xp20Exp, params);
                List<?> values = (List<?>) ret;
                Assert.assertEquals(1, values.size());
                Assert.assertEquals("Unexpected Function value", ODE_840,
                        (String) values.get(0));
            }
        }

    }

}
