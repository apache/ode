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

package org.apache.ode.bpel.elang.xquery10.compiler;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import net.sf.saxon.dom.NodeWrapper;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;

import org.apache.ode.bpel.elang.xpath20.compiler.Constants;
import org.apache.ode.bpel.elang.xpath20.compiler.JaxpFunctionResolver;
import org.apache.ode.utils.Namespaces;
import org.w3c.dom.Node;


/**
 * XQuery-Friendly definition of BPEL functions, which 
 * delegates calls to the JAXP-based function resolver
 */
public class XQuery10BpelFunctions {
    public static final String USER_DATA_KEY_FUNCTION_RESOLVER = "function-resolver";
    private static final QName WSBPEL_GET_VARIABLE_PROPERTY = new QName(Namespaces.WSBPEL2_0_FINAL_EXEC,
            Constants.EXT_FUNCTION_GETVARIABLEPROPERTY);
    private static final QName WSBPEL_GET_VARIABLE_DATA = new QName(Namespaces.WSBPEL2_0_FINAL_EXEC,
            Constants.EXT_FUNCTION_GETVARIABLEDATA);
    private static final QName WSBPEL_GET_LINK_STATUS = new QName(Namespaces.WSBPEL2_0_FINAL_EXEC,
            Constants.EXT_FUNCTION_GETLINKSTATUS);
    private static final QName WSBPEL_DO_XSL_TRANSFORM = new QName(Namespaces.WSBPEL2_0_FINAL_EXEC,
            Constants.EXT_FUNCTION_DOXSLTRANSFORM);

    /**
     * WS-BPEL getVariableProperty function 
     *
     * @param context context
     * @param variableName variableName
     * @param propertyName propertyName
     *
     * @return type
     *
     * @throws XPathFunctionException XPathFunctionException
     */
    public static Object getVariableProperty(XPathContext context,
        String variableName, String propertyName) throws XPathFunctionException {
        ArrayList<String> arguments = new ArrayList<String>();
        arguments.add(variableName);
        arguments.add(propertyName);

        return evaluate(resolveFunction(context, WSBPEL_GET_VARIABLE_PROPERTY),
            arguments);
    }

    /**
     * WS-BPEL getVariableData function 
     *
     * @param context context
     * @param variableName variableName
     * @param partName partName
     * @param xpath xpath
     *
     * @return type
     *
     * @throws XPathFunctionException XPathFunctionException
     */
    public static Object getVariableData(XPathContext context,
        String variableName, String partName, String xpath)
        throws XPathFunctionException {
        ArrayList<String> arguments = new ArrayList<String>();
        arguments.add(variableName);
        arguments.add(partName);
        arguments.add(xpath);

        return evaluate(resolveFunction(context, WSBPEL_GET_VARIABLE_DATA),
            arguments);
    }

    /**
     * WS-BPEL doXslTransform function 
     *
     * @param context context
     * @param xslUri xslUri
     * @param sources sources
     *
     * @return type
     *
     * @throws XPathFunctionException XPathFunctionException
     */
    public static Object doXslTransform(XPathContext context, String xslUri,
        Object sources) throws XPathFunctionException {
        ArrayList<Object> arguments = new ArrayList<Object>();
        arguments.add(xslUri);
        arguments.add(sources);

        return evaluate(resolveFunction(context, WSBPEL_DO_XSL_TRANSFORM),
            arguments);
    }

    /**
     * WS-BPEL getLinkStatus function 
     *
     * @param context context
     * @param linkName linkName
     *
     * @return type
     *
     * @throws XPathFunctionException XPathFunctionException
     */
    public static Object getLinkStatus(XPathContext context, String linkName)
        throws XPathFunctionException {
        ArrayList<String> arguments = new ArrayList<String>();
        arguments.add(linkName);

        return evaluate(resolveFunction(context, WSBPEL_GET_LINK_STATUS),
            arguments);
    }

    /**
     * Locates the JAXP equivalent function
     *
     * @param context context
     * @param name name
     *
     * @return type
     */
    private static XPathFunction resolveFunction(XPathContext context,
        QName name) {
        JaxpFunctionResolver funcResolver = null;
        Item item = context.getCurrentIterator().current();

        if (item instanceof NodeWrapper) {
            Node node = (Node) ((NodeWrapper) item).getUnderlyingNode();

            if (node != null) {
                funcResolver = (JaxpFunctionResolver) node.getUserData(USER_DATA_KEY_FUNCTION_RESOLVER);
            }
        }

        return funcResolver.resolveFunction(name, 0);
    }

    /**
     * Evaluates function against arguments passed by XQuery 
     *
     * @param function function
     * @param arguments arguments
     *
     * @return type
     *
     * @throws XPathFunctionException XPathFunctionException
     */
    private static Object evaluate(XPathFunction function, List arguments)
        throws XPathFunctionException {
        if (function == null) {
            throw new XPathFunctionException("Unable to locate function in library");
        }

        return function.evaluate(arguments);
    }
}
