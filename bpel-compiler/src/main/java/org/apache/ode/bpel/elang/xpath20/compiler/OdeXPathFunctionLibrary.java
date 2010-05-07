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

import net.sf.saxon.expr.Expression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.StaticError;
import net.sf.saxon.xpath.XPathFunctionCall;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import java.util.ArrayList;

/**
 * Overloading the XPathFunctionLibrary to force it to initialize our functions giving
 * the provided parameters. Otherwise the Saxon implemetation just never gives you
 * any parameter before runtime.
 * @author mriou <mriou at apache dot org>
 */
public class OdeXPathFunctionLibrary extends net.sf.saxon.xpath.XPathFunctionLibrary {
    private static final long serialVersionUID = -8885396864277163797L;
    
    private transient JaxpFunctionResolver _funcResolver;

    public OdeXPathFunctionLibrary(JaxpFunctionResolver funcResolver) {
        _funcResolver = funcResolver;
    }

    public Expression bind(int nameCode, String uri, String local, Expression[] staticArgs) throws XPathException {
        QName name = new QName(uri, local);
        XPathFunction function = _funcResolver.resolveFunction(name, staticArgs.length);
        if (function == null) {
            return null;
        }

        // Converting the expression array to the simple string
        ArrayList args = new ArrayList(staticArgs.length);
        for (Expression expression : staticArgs) {
            String exprStr = expression.toString();
            if (exprStr.startsWith("\"")) exprStr = exprStr.substring(1);
            if (exprStr.endsWith("\"")) exprStr = exprStr.substring(0, exprStr.length() - 1);
            args.add(exprStr);
        }

        try {
            function.evaluate(args);
        } catch (XPathFunctionException e) {
            throw new StaticError(e);
        }
        XPathFunctionCall fc = new XPathFunctionCall(function);
        fc.setArguments(staticArgs);
        return fc;
    }

}
