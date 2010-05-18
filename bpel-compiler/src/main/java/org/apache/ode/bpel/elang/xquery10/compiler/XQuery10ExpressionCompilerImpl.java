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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQStaticContext;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.Validation;
import net.sf.saxon.xqj.SaxonXQConnection;
import net.sf.saxon.xqj.SaxonXQDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.api.CompilerContext;
import org.apache.ode.bpel.compiler.api.ExpressionCompiler;
import org.apache.ode.bpel.compiler.bom.Expression;
import org.apache.ode.bpel.elang.xpath10.compiler.XPathMessages;
import org.apache.ode.bpel.elang.xpath10.compiler.XslCompilationErrorListener;
import org.apache.ode.bpel.elang.xpath20.compiler.Constants;
import org.apache.ode.bpel.elang.xpath20.compiler.JaxpFunctionResolver;
import org.apache.ode.bpel.elang.xpath20.compiler.JaxpVariableResolver;
import org.apache.ode.bpel.elang.xpath20.compiler.WrappedResolverException;
import org.apache.ode.bpel.elang.xquery10.o.OXQuery10ExpressionBPEL20;
import org.apache.ode.bpel.o.OConstantVarType;
import org.apache.ode.bpel.o.OElementVarType;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.bpel.o.OLValueExpression;
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OVarType;
import org.apache.ode.bpel.o.OXsdTypeVarType;
import org.apache.ode.bpel.o.OMessageVarType.Part;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.NSContext;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.xsl.XslTransformHandler;
import org.apache.xml.utils.XMLChar;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XQuery compiler based on the SAXON implementation.
 */
public class XQuery10ExpressionCompilerImpl implements ExpressionCompiler {

    protected static final Log __log = LogFactory.getLog(XQuery10ExpressionCompilerImpl.class);

    protected String _bpelNS;
    protected QName _qnLinkStatus;
    protected QName _qnVarProp;
    protected QName _qnVarData;
    protected QName _qnXslTransform;

    protected final XPathMessages __msgs = MessageBundle.getMessages(XPathMessages.class);
    protected Map<String, String> _properties = new HashMap<String, String>();
    protected CompilerContext _compilerContext;

    public XQuery10ExpressionCompilerImpl(String bpelNS) {
        _bpelNS = bpelNS;
        _qnLinkStatus = new QName(_bpelNS, Constants.EXT_FUNCTION_GETLINKSTATUS);
        _qnVarProp = new QName(_bpelNS, Constants.EXT_FUNCTION_GETVARIABLEPROPERTY);
        _qnVarData = new QName(_bpelNS, Constants.EXT_FUNCTION_GETVARIABLEDATA);
        _qnXslTransform = new QName(_bpelNS, Constants.EXT_FUNCTION_DOXSLTRANSFORM);

        _properties.put("runtime-class", "org.apache.ode.bpel.elang.xquery10.runtime.XQuery10ExpressionRuntime");
        TransformerFactory trsf = new net.sf.saxon.TransformerFactoryImpl();
        XslTransformHandler.getInstance().setTransformerFactory(trsf);
    }

    public void setCompilerContext(CompilerContext compilerContext) {
        _compilerContext = compilerContext;
        XslCompilationErrorListener xe = new XslCompilationErrorListener(compilerContext);
        XslTransformHandler.getInstance().setErrorListener(xe);
    }

    /**
     * @see org.apache.ode.bpel.compiler.api.ExpressionCompiler#compileJoinCondition(java.lang.Object)
     */
    public OExpression compileJoinCondition(Object source) throws CompilationException {
        return _compile((Expression) source, true);
    }

    /**
     * @see org.apache.ode.bpel.compiler.api.ExpressionCompiler#compile(java.lang.Object)
     */
    public OExpression compile(Object source) throws CompilationException {
        return _compile((Expression) source, false);
    }

    /**
     * @see org.apache.ode.bpel.compiler.api.ExpressionCompiler#compileLValue(java.lang.Object)
     */
    public OLValueExpression compileLValue(Object source) throws CompilationException {
        return (OLValueExpression) _compile((Expression) source, false);
    }

    /**
     * @see org.apache.ode.bpel.compiler.api.ExpressionCompiler#compile(java.lang.Object)
     */
    private OExpression _compile(org.apache.ode.bpel.compiler.bom.Expression xquery, boolean isJoinCondition)
            throws CompilationException {
    	OXQuery10ExpressionBPEL20 oexp = new OXQuery10ExpressionBPEL20(_compilerContext.getOProcess(), _qnVarData,
                _qnVarProp, _qnLinkStatus, _qnXslTransform, isJoinCondition);
        oexp.namespaceCtx = xquery.getNamespaceContext();
        doJaxpCompile(oexp, xquery);
        return oexp;
    }

    private void doJaxpCompile(OXQuery10ExpressionBPEL20 out, Expression source) throws CompilationException {
        String xqueryStr;
        Node node = source.getExpression();
        if (node == null) {
            throw new CompilationException(__msgs.errEmptyExpression(source.getURI(), new QName(source.getElement().getNamespaceURI(), source.getElement().getNodeName())));
        }
        if (node.getNodeType() != Node.TEXT_NODE && 
        		node.getNodeType() != Node.ELEMENT_NODE && 
        		node.getNodeType() != Node.CDATA_SECTION_NODE) {
            throw new CompilationException(__msgs.errUnexpectedNodeTypeForXPath(DOMUtils.domToString(node)));
        }
        xqueryStr = DOMUtils.domToString(node);
        xqueryStr = xqueryStr.trim();
        if (xqueryStr.length() == 0) {
            throw new CompilationException(__msgs.warnXPath20Syntax(DOMUtils.domToString(node), "empty string"));
        }

        try {
        	XQDataSource xqds = new SaxonXQDataSource(new Configuration());
            XQConnection xqconn = xqds.getConnection();
            
            __log.debug("Compiling expression " + xqueryStr);
            Configuration configuration = ((SaxonXQConnection) xqconn).getConfiguration();
            configuration.setAllNodesUntyped(true);
            configuration.setHostLanguage(Configuration.XQUERY);

            XQStaticContext staticContext = xqconn.getStaticContext();
            JaxpFunctionResolver funcResolver = new JaxpFunctionResolver(
                    _compilerContext, out, source.getNamespaceContext(), _bpelNS);
            JaxpVariableResolver variableResolver = new JaxpVariableResolver(
            		_compilerContext, out); 

            XQueryDeclarations declarations = new XQueryDeclarations();            
            NSContext nsContext = source.getNamespaceContext();
        	Set<String> prefixes = nsContext.getPrefixes();
        	if (!nsContext.getUriSet().contains(Namespaces.ODE_EXTENSION_NS)) {
        		nsContext.register("ode", Namespaces.ODE_EXTENSION_NS);
        	}
        	for (String prefix : prefixes) {
        		String uri = nsContext.getNamespaceURI(prefix);
        		staticContext.declareNamespace(prefix, uri);
        		if ("".equals(prefix)) {
        			declarations.declareDefaultElementNamespace(uri);
        		} else if ("bpws".equals(prefix)) {
                    declarations.declareNamespace("bpws", "java:" + Constants.XQUERY_FUNCTION_HANDLER_COMPILER);
        		} else {
	        		declarations.declareNamespace(prefix, uri);
        		}
        	}
            declarations.declareVariable(
            		getQName(nsContext, Namespaces.ODE_EXTENSION_NS, "pid"), 
            		getQName(nsContext, Namespaces.XML_SCHEMA, "integer"));
//            Map<URI, Source> schemaDocuments = _compilerContext.getSchemaSources();
//            for (URI schemaUri : schemaDocuments.keySet()) {
//            	Source schemaSource = schemaDocuments.get(schemaUri);
//            	// Don't add schema sources, since our Saxon library is not schema-aware. 
//            	// configuration.addSchemaSource(schemaSource);
//            }
            configuration.setSchemaValidationMode(Validation.SKIP);
            List<OScope.Variable> variables = _compilerContext.getAccessibleVariables();
            Map<QName, QName> variableTypes = new HashMap<QName, QName>();
            for (String variableName : getVariableNames(xqueryStr)) {
            	OScope.Variable variable = getVariable(variables, variableName);
            	if (variable == null) {
            		continue;
            	}
                OVarType type = variable.type;
                QName nameQName = getNameQName(variableName);
                QName typeQName = getTypeQName(variableName, type);
                variableTypes.put(nameQName, typeQName);
                String prefix = typeQName.getPrefix();
                if (prefix == null || "".equals(prefix.trim())) {
                	prefix = getPrefixForUri(nsContext, typeQName.getNamespaceURI());
                }
                // don't declare typed variables, as our engine is not schema-aware
                // declarations.declareVariable(variable.name, typeQName);
                declarations.declareVariable(variableName);
            }
            
            // Add implicit declarations as prolog to the user-defined XQuery
            out.xquery = declarations.toString() + xqueryStr;

            // Check the XQuery for compilation errors 
            xqconn.setStaticContext(staticContext);            
            XQPreparedExpression exp = xqconn.prepareExpression(out.xquery);
            
            // Pre-evaluate variables and functions by executing query  
            node.setUserData(XQuery10BpelFunctions.USER_DATA_KEY_FUNCTION_RESOLVER, 
            		funcResolver, null);
            exp.bindItem(XQConstants.CONTEXT_ITEM,
                    xqconn.createItemFromNode(node, xqconn.createNodeType()));
            // Bind external variables to dummy runtime values
            for (QName variable : exp.getAllUnboundExternalVariables()) {
            	QName typeQName = variableTypes.get(variable);
                Object value = variableResolver.resolveVariable(variable);
            	if (typeQName != null) {
            		if (value.getClass().getName().startsWith("java.lang")) {
    	                exp.bindAtomicValue(variable, value.toString(), 
    	                		xqconn.createAtomicType(XQItemType.XQBASETYPE_ANYATOMICTYPE));
            		} else if (value instanceof Node) {
    	                exp.bindNode(variable, (Node) value, xqconn.createNodeType());
            		} else if (value instanceof NodeList) {
            			NodeList nodeList = (NodeList) value;
            			ArrayList nodeArray = new ArrayList();
            			for (int i = 0; i < nodeList.getLength(); i++) {
            				nodeArray.add(nodeList.item(i));
            			}
            			XQSequence sequence = xqconn.createSequence(nodeArray.iterator());
            			exp.bindSequence(variable, sequence);
            		}
            	}
            }
            // evaluate the expression so as to initialize the variables
            try { 
                exp.executeQuery();
            } catch (XQException xpee) { 
            	// swallow errors caused by uninitialized variables 
            } finally {
            	// reset the expression's user data, in order to avoid 
            	// serializing the function resolver in the compiled bpel file.
            	if (node != null) {
            		node.setUserData(XQuery10BpelFunctions.USER_DATA_KEY_FUNCTION_RESOLVER, null, null);
            	}
            }
        } catch (XQException xqe) {
            __log.debug(xqe);
            __log.info("Couldn't validate properly expression " + xqueryStr);
            throw new CompilationException(__msgs.errXQuery10Syntax(xqueryStr, "Couldn't validate XQuery expression"));
        } catch (WrappedResolverException wre) {
            if (wre._compilationMsg != null)
                throw new CompilationException(wre._compilationMsg, wre);
            if (wre.getCause() instanceof CompilationException)
                throw (CompilationException) wre.getCause();
            throw wre;
        }
    }
    
    public Map<String, String> getProperties() {
        return _properties;
    }
    
    private String getQName(NSContext nsContext, String uri, String localPart) {
    	String prefix = getPrefixForUri(nsContext, uri);
    	return (prefix == null ? localPart : (prefix + ":" + localPart));
    }
    
    private String getPrefixForUri(NSContext nsContext, String uri) {
    	Set<String> prefixes = nsContext.getPrefixes();
    	for (String prefix : prefixes) {
    		String anUri = (nsContext.getNamespaceURI(prefix));
    		if (anUri != null && anUri.equals(uri)) {
    			return prefix;
    		}
    	}
    	return null;
    }
    
    protected static Collection<String> getVariableNames(String xquery) {
    	Collection<String> variableNames = new LinkedHashSet<String>();
    	for (int index = xquery.indexOf("$"); index != -1; index = xquery.indexOf("$")) {
    		StringBuilder variableName = new StringBuilder();
    		index++;
    		while(index < xquery.length() && XMLChar.isNCName(xquery.charAt(index))) {
    		    variableName.append(xquery.charAt(index++));
    		}
        	variableNames.add(variableName.toString());
        	xquery = xquery.substring(index);
    	}
    	return variableNames;
    }
    
    private OScope.Variable getVariable(List<OScope.Variable> variables, String variableName) {
    	String declaredVariable = getVariableDeclaredName(variableName);
    	for (OScope.Variable variable : variables) {
    		if (variable.name.equals(declaredVariable)) {
    			return variable;
    		}
    	}
    	return null;
    }
    
    private String getVariableDeclaredName(String variableReference) {
    	int dotIndex = variableReference.indexOf(".");
    	return dotIndex >= 0 ? variableReference.substring(0, dotIndex) : variableReference;
    }
    
    private String getVariablePartName(String variableReference) {
    	int dotIndex = variableReference.indexOf(".");
    	return dotIndex >= 0 ? variableReference.substring(dotIndex + 1) : "";    	
    }
    
    private QName getNameQName(String variableName) {
        String prefix = null, localName = null;;
    	int colonIndex = variableName.indexOf(":");
        if (colonIndex >= 0) {
        	prefix = variableName.substring(0, colonIndex);
        	localName = variableName.substring(colonIndex + 1);
        } else {
        	prefix = "";
        	localName = variableName;
        }
        return new QName(prefix, localName);
    }
    
    private QName getTypeQName(String variableName, OVarType type) {
    	QName typeQName = null;
        if (type instanceof OConstantVarType) {
        	typeQName = new QName(Namespaces.XML_SCHEMA, "string", "xs");
        } else if (type instanceof OElementVarType) {
        	typeQName = ((OElementVarType) type).elementType;
        } else if (type instanceof OMessageVarType) {
        	Part part = ((OMessageVarType) type).parts.get(getVariablePartName(variableName));
        	if (part != null) {
            	typeQName = getTypeQName(variableName, part.type);
        	}
        } else if (type instanceof OXsdTypeVarType) {
        	typeQName = ((OXsdTypeVarType) type).xsdType;
        }
    	return typeQName;
    }

    private static class XQueryDeclarations {
    	StringBuffer declarations = new StringBuffer();
    	
    	public XQueryDeclarations() {}
    	
		public void declareVariable(String name, QName type) {
    		declareVariable(name, type.getPrefix() + ":" + type.getLocalPart());    
    	}
    	
    	public void declareVariable(String name, String type) {
    		declarations.append("declare variable ")
    			.append("$")
	    		.append(name)
	    		.append(" as ")
	    		.append(type)
	    		.append(" external ")
	    		.append(";\n");
    	}
    	
    	public void declareVariable(String name) {
    		declarations.append("declare variable ")
    			.append("$")
	    		.append(name)
	    		.append(" external ")
	    		.append(";\n");
    	}
    	
    	public void declareNamespace(String prefix, String uri) {
            declarations.append("declare namespace ")
	            .append(prefix)
	            .append("=")
	            .append("\"" + uri + "\"")
	            .append(";\n");
    	}
    	    	
    	public void declareDefaultElementNamespace(String uri) {
            declarations.append("declare default element namespace ")
	            .append("\"" + uri + "\"")
	            .append(";\n");
		}

    	public String toString() {
    		return declarations.toString();
    	}    	
    }
}
