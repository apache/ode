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

import java.util.List;

import net.sf.saxon.Configuration;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.xpath.JAXPXPathStaticContext;
import net.sf.saxon.xpath.XPathFunctionLibrary;

/**
 * Hooks on Saxon StandaloneContext to be notified when the compilation
 * finds some variables and functions. This allows us to prepare the
 * OXpathExpression with variable references and all the things needed
 * at runtime.
 * @author mriou <mriou at apache dot org>
 */
public class SaxonContext extends JAXPXPathStaticContext {


    private static final long serialVersionUID = 2460900369574468960L;
    private transient JaxpVariableResolver _varResolver;
    private transient JaxpFunctionResolver _funcResolver;

    public SaxonContext(Configuration config, JaxpVariableResolver varResolver,
                        JaxpFunctionResolver funcResolver) {
        super(config);

        // We need to remove the default XPathFunctionLibrary to replace it
        // with our own
        List libList = ((FunctionLibraryList)getFunctionLibrary()).libraryList;
        XPathFunctionLibrary xpathLib = null;
        for (Object lib : libList) {
            FunctionLibrary flib = (FunctionLibrary) lib;
            if (flib instanceof XPathFunctionLibrary) xpathLib = (XPathFunctionLibrary) flib;
        }
        if (xpathLib != null) libList.remove(xpathLib);
        OdeXPathFunctionLibrary oxpfl = new OdeXPathFunctionLibrary(funcResolver);
        oxpfl.setXPathFunctionResolver(funcResolver);

        oxpfl.setXPathFunctionResolver(_funcResolver);
        ((FunctionLibraryList)getFunctionLibrary()).addFunctionLibrary(oxpfl);

        setXPathFunctionResolver(funcResolver);
        setXPathVariableResolver(varResolver);

        _varResolver = varResolver;
        _funcResolver = funcResolver;
    }

    // The following methods don't apply to the JAXPXPathStaticContext interface.

//    public Variable declareVariable(String qname, Object initialValue) throws XPathException {
//        String prefix;
//        String localName;
//        final NameChecker checker = getConfiguration().getNameChecker();
//        try {
//            String[] parts = checker.getQNameParts(qname);
//            prefix = parts[0];
//            localName = parts[1];
//        } catch (QNameException err) {
//            throw new StaticError("Invalid QName for variable: " + qname);
//        }
//        String uri = "";
//        if (!("".equals(prefix))) {
//            uri = getURIForPrefix(prefix);
//        }
//
//        _varResolver.resolveVariable(new QName(uri, localName, prefix));
//
//        return super.declareVariable(qname, initialValue);
//    }

//    public VariableReference bindVariable(int fingerprint) throws StaticError {
//        String localName = getNamePool().getLocalName(fingerprint);
//        String prefix = getNamePool().getPrefix(fingerprint);
//        String ns = getNamePool().getURI(fingerprint);
//        // The prefix is lost by compilation, hardcoding it from the ns.
//        if (Namespaces.ODE_EXTENSION_NS.equals(ns)) prefix = "ode";
//        if (prefix != null && prefix.length() > 0) prefix = prefix + ":";
//        try {
//            declareVariable(prefix + localName, null);
//        } catch (XPathException e) {
//            throw new StaticError(e);
//        }
////        return super.bindVariable(fingerprint);
//        return null;
//    }

//   public VariableReference bindVariable(StructuredQName qName) {
//	   // The prefix is lost by compilation, hardcoding it from the ns.
//	   String prefix = qName.getPrefix();
//	   String ns = qName.getNamespaceURI();
//	   if (prefix == null && Namespaces.ODE_EXTENSION_NS.equals(ns)) {
//		   qName = new StructuredQName("ode", ns, qName.getLocalName());
//	   }
//	   return super.bindVariable(qName);
//   }

}
