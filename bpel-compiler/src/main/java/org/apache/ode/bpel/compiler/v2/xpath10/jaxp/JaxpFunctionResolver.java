/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.apache.ode.bpel.compiler.v2.xpath10.jaxp;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.WrappedResolverException;
import org.apache.ode.bpel.compiler.XPathMessages;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.v2.CompilerContext;
import org.apache.ode.bpel.compiler.v2.xpath10.XslCompileUriResolver;
import org.apache.ode.bpel.rtrep.common.Constants;
import org.apache.ode.bpel.rtrep.v2.OExpression;
import org.apache.ode.bpel.rtrep.v2.OLink;
import org.apache.ode.bpel.rtrep.v2.OMessageVarType;
import org.apache.ode.bpel.rtrep.v2.OProcess;
import org.apache.ode.bpel.rtrep.v2.OScope;
import org.apache.ode.bpel.rtrep.v2.OXslSheet;
import org.apache.ode.bpel.rtrep.v2.xpath10.OXPath10Expression;
import org.apache.ode.utils.NSContext;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.xsl.XslTransformHandler;

/**
 * mock JAXP function resolver for checking the functions during compile time.
 */
public class JaxpFunctionResolver implements XPathFunctionResolver {
    private static final Log __log = LogFactory.getLog(JaxpFunctionResolver.class);

    private static final XPathMessages __msgs = MessageBundle.getMessages(XPathMessages.class);

    private CompilerContext _cctx;

    private OXPath10Expression _out;

    private NSContext _nsContext;

    private String _bpelNS;

    public JaxpFunctionResolver(CompilerContext cctx, OXPath10Expression out, NSContext nsContext, String bpelNS) {
        _cctx = cctx;
        _bpelNS = bpelNS;
        _nsContext = nsContext;
        _bpelNS = bpelNS;
        _out = out;
    }

    public XPathFunction resolveFunction(QName functionName, int arity) {
        __log.debug("JAXP compiler: Resolving function " + functionName);
        if (functionName.getNamespaceURI() == null) {
            throw new WrappedResolverException("Undeclared namespace for " + functionName);
        } else if (functionName.getNamespaceURI().equals(_bpelNS)) {
            String localName = functionName.getLocalPart();
            if (Constants.EXT_FUNCTION_GETVARIABLEDATA.equals(localName)) {
                return new GetVariableData();
            }
            if (Constants.EXT_FUNCTION_GETVARIABLEPROPERTY.equals(localName)) {
                return new GetVariableProperty();
            }
            if (Constants.EXT_FUNCTION_GETLINKSTATUS.equals(localName)) {
                return new GetLinkStatus();
            }
            if (Constants.EXT_FUNCTION_DOXSLTRANSFORM.equals(localName)) {
                return new DoXslTransform();
            }
            throw new WrappedResolverException(__msgs.errUnknownBpelFunction(localName));
        }

        return null;
    }

    public class GetVariableData implements XPathFunction {
        public Object evaluate(List params) throws XPathFunctionException {
            if (params.size() < 1 || params.size() > 3) {
                throw new CompilationException(__msgs
                    .errInvalidNumberOfArguments(Constants.EXT_FUNCTION_GETVARIABLEDATA));
            }
            String varname = (String) params.get(0);
            String partname = params.size() > 1 ? (String) params.get(1) : null;
            String locationstr = params.size() > 2 ? (String) params.get(2) : null;

            OScope.Variable var = _cctx.resolveVariable(varname);
            OMessageVarType.Part part = partname != null ? _cctx.resolvePart(var, partname) : null;
            OExpression location = null;
            if (locationstr != null) {
                location = _cctx.compileExpr(locationstr, _nsContext);
            }

            _out.addGetVariableDataSig(varname, partname, locationstr, new OXPath10Expression.OSigGetVariableData(
                _cctx.getOProcess(), var, part, location));
            return "";
        }
    }

    public class GetVariableProperty implements XPathFunction {
        public Object evaluate(List params) throws XPathFunctionException {
            if (params.size() != 2) {
                throw new CompilationException(__msgs
                    .errInvalidNumberOfArguments(Constants.EXT_FUNCTION_GETVARIABLEPROPERTY));
            }
            String varName = (String) params.get(0);
            OScope.Variable v = _cctx.resolveVariable(varName);
            _out.vars.put(varName, v);

            String propName = (String) params.get(1);
            QName qname = _nsContext.derefQName(propName);

            if (qname == null)
                throw new CompilationException(__msgs.errInvalidQName(propName));

            OProcess.OProperty property = _cctx.resolveProperty(qname);
            // Make sure we can...
            _cctx.resolvePropertyAlias(v, qname);

            _out.properties.put(propName, property);
            _out.vars.put(varName, v);
            return "";
        }
    }

    public class GetLinkStatus implements XPathFunction {
        public Object evaluate(List params) throws XPathFunctionException {
            if (params.size() != 1) {
                throw new CompilationException(__msgs
                    .errInvalidNumberOfArguments(Constants.EXT_FUNCTION_GETLINKSTATUS));
            }
            String linkName = (String) params.get(0);
            OLink olink = _cctx.resolveLink(linkName);
            _out.links.put(linkName, olink);

            return "";
        }
    }

    public class DoXslTransform implements XPathFunction {
        public Object evaluate(List params) throws XPathFunctionException {
            if (params.size() < 2 || params.size() % 2 != 0) {
                throw new CompilationException(__msgs
                    .errInvalidNumberOfArguments(Constants.EXT_FUNCTION_DOXSLTRANSFORM));
            }

            String xslUri = (String) params.get(0);
            OXslSheet xslSheet = _cctx.compileXslt(xslUri);
            try {
                XslTransformHandler.getInstance().parseXSLSheet(_cctx.getBaseResourceURI(), xslSheet.uri,
                    xslSheet.sheetBody, new XslCompileUriResolver(_cctx, _out));
            } catch (Exception e) {
                throw new CompilationException(__msgs.errXslCompilation(xslUri, e.toString()));
            }

            _out.xslSheets.put(xslSheet.uri, xslSheet);
            return "";
        }
    }

}
