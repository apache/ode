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

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;

import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.api.CompilerContext;
import org.apache.ode.bpel.elang.xpath10.compiler.XPathMessages;
import org.apache.ode.bpel.elang.xpath10.compiler.XslCompileUriResolver;
import org.apache.ode.bpel.elang.xpath20.o.OXPath20ExpressionBPEL20;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OXslSheet;
import org.apache.ode.utils.NSContext;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.xsl.XslTransformHandler;

/**
 * @author mriou <mriou at apache dot org>
 */
public class JaxpFunctionResolver implements XPathFunctionResolver {

    private static final XPathMessages __msgs = MessageBundle.getMessages(XPathMessages.class);

    private CompilerContext _cctx;
    private OXPath20ExpressionBPEL20 _out;
    private NSContext _nsContext;
    private String _bpelNS;

    public JaxpFunctionResolver(CompilerContext cctx, OXPath20ExpressionBPEL20 out,
                                NSContext nsContext, String bpelNS) {
        _cctx = cctx;
        _bpelNS = bpelNS;
        _nsContext = nsContext;
        _bpelNS = bpelNS;
        _out = out;
    }

    public XPathFunction resolveFunction(QName functionName, int arity) {
        if (functionName.getNamespaceURI() == null) {
            throw new WrappedResolverException("Undeclared namespace for " + functionName);
        } else if (functionName.getNamespaceURI().equals(_bpelNS)) {
            String localName = functionName.getLocalPart();
            if (Constants.EXT_FUNCTION_GETVARIABLEPROPRTY.equals(localName)) {
                return new GetVariableProperty();
            } else if (Constants.EXT_FUNCTION_DOXSLTRANSFORM.equals(localName)) {
                return new DoXslTransform();
            } else {
                throw new WrappedResolverException(__msgs.errUnknownBpelFunction(localName));
            }
        } else if (functionName.getNamespaceURI().equals(Namespaces.ODE_EXTENSION_NS)) {
            String localName = functionName.getLocalPart();
            if (Constants.NON_STDRD_FUNCTION_SPLITTOELEMENTS.equals(localName)) {
                return new SplitToElements();
            }
        }

        return null;
    }


    public class GetVariableProperty implements XPathFunction {
        public Object evaluate(List params) throws XPathFunctionException {
            if (params.size() != 2) {
                throw new CompilationException(
                        __msgs.errInvalidNumberOfArguments(Constants.EXT_FUNCTION_GETVARIABLEPROPRTY));
            }
            String varName = (String) params.get(0);
            OScope.Variable v = _cctx.resolveVariable(varName);
            _out.vars.put(varName, v);

            String propName = (String) params.get(1);
            QName qname = _nsContext.derefQName(propName);

            if (qname == null)
                throw new CompilationException(
                        __msgs.errInvalidQName(propName));

            OProcess.OProperty property = _cctx.resolveProperty(qname);
            // Make sure we can...
            _cctx.resolvePropertyAlias(v, qname);

            _out.properties.put(propName, property);
            _out.vars.put(varName, v);
            return "";
        }
    }

    public class DoXslTransform implements XPathFunction {
        public Object evaluate(List params) throws XPathFunctionException {
            if (params.size() < 2 || params.size() % 2 != 0) {
                throw new CompilationException(
                        __msgs.errInvalidNumberOfArguments(Constants.EXT_FUNCTION_DOXSLTRANSFORM));
            }

            String xslUri = (String) params.get(0);
            OXslSheet xslSheet = _cctx.compileXslt(xslUri);
            try {
                XslTransformHandler.getInstance().parseXSLSheet(xslSheet.uri, xslSheet.sheetBody,
                        new XslCompileUriResolver(_cctx, _out));
            } catch (Exception e) {
                throw new CompilationException(__msgs.errXslCompilation(xslUri, e.toString()));
            }

            _out.xslSheets.put(xslSheet.uri, xslSheet);
            return "";
        }
    }

    /**
     * Compile time checking for the non standard ode:splitToElements function.
     */
    public class SplitToElements implements XPathFunction {
        public Object evaluate(List params) throws XPathFunctionException {
            if (params.size() < 3 || params.size() > 4) {
                throw new CompilationException(
                        __msgs.errInvalidNumberOfArguments(Constants.NON_STDRD_FUNCTION_SPLITTOELEMENTS));
            }
            return "";
        }
    }

}
