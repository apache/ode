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
package org.apache.ode.bpel.elang.xpath20;

import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import org.apache.ode.utils.NSContext;
import org.apache.ode.utils.trax.LogErrorListener;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.SourceLocator;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ExpressionTool;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.VariableDeclaration;
import net.sf.saxon.functions.*;
import net.sf.saxon.instruct.LocationMap;
import net.sf.saxon.instruct.SlotManager;
import net.sf.saxon.om.*;
import net.sf.saxon.sort.CollationFactory;
import net.sf.saxon.trans.StaticError;
import net.sf.saxon.trans.Variable;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SaxonCompileContext implements StaticContext, NamespaceResolver {
  private static final Log __log = LogFactory.getLog(SaxonCompileContext.class);

  private NamePool _namePool;
	private Map<String, String> _namespaces = new HashMap<String, String>(10);
	private Map<String, Comparator> _collations = new HashMap<String, Comparator>(10);
	private Map<Integer, Variable> _variables = new HashMap<Integer, Variable>(20);
	private SlotManager _stackFrameMap;
	private String _defaultCollationName = null;
	private String _baseURI = null;
	private Configuration _config;
	private LocationMap _locationMap = new LocationMap();
	private FunctionLibrary _functionLibrary;
  private NSContext _nsCtx;
	private String _defaultFunctionNamespace = NamespaceConstant.FN;
	private short _defaultElementNamespace = NamespaceConstant.NULL_CODE;
	private boolean _backwardsCompatible = false;
	
  private static Configuration getNewConfiguration() {
    Configuration c = new Configuration();
    c.setErrorListener(new LogErrorListener(__log));
    return c;
  }
  
  public static Expression compileExpression(OXPath10Expression oexp) throws XPathException{
  	return compileExpression(oexp, getNewConfiguration());
  }
    
  public static Expression compileExpression(OXPath10Expression exp, Configuration config) throws XPathException{
    SaxonCompileContext staticContext = new SaxonCompileContext(config, exp.namespaceCtx);
    
    Expression expr = ExpressionTool.make(exp.xpath, staticContext,0,-1,1);
    expr = expr.analyze(staticContext, Type.ITEM_TYPE);
    SlotManager map = staticContext.getConfiguration().makeSlotManager();
    ExpressionTool.allocateSlots(expr, 0, map);
    return expr;
  }
  /**
	 * Create a StandaloneContext using the default Configuration and NamePool
	 */
	public SaxonCompileContext(NSContext nsContext) {
		this(getNewConfiguration(), nsContext);
    // avoid the Saxon version warning.
	}
	/**
	 * Create a StandaloneContext using a specific NamePool
	 */
	public SaxonCompileContext(Configuration config, NSContext nsctx) {
		this._config = config;
    this._nsCtx = nsctx;
		_namePool = config.getNamePool();
		_stackFrameMap = config.makeSlotManager();
		clearNamespaces();
		// Set up a default function library. This can be overridden using
		// setFunctionLibrary()
		FunctionLibraryList lib = new FunctionLibraryList();
		lib.addFunctionLibrary(new SystemFunctionLibrary(
				SystemFunctionLibrary.XPATH_ONLY));
		lib.addFunctionLibrary(getConfiguration().getVendorFunctionLibrary());
		lib.addFunctionLibrary(new ConstructorFunctionLibrary(getConfiguration()));
		if (config.isAllowExternalFunctions()) {
			lib.addFunctionLibrary(new JavaExtensionLibrary(getConfiguration()));
		}
    lib.addFunctionLibrary(new BpelFunctionLibrary(Constants.BPEL20_NS));
		_functionLibrary = lib;
	}
	/**
	 * Get the system configuration
	 */
	public Configuration getConfiguration() {
		return _config;
	}
	public LocationMap getLocationMap() {
		return _locationMap;
	}
	public void setLocationMap(LocationMap locationMap) {
		this._locationMap = locationMap;
	}
	/**
	 * Declare a namespace whose prefix can be used in expressions. Namespaces may
	 * either be pre-declared (the traditional Saxon interface), or they may be
	 * resolved on demand using a supplied NamespaceContext. When a prefix has to
	 * be resolved, the parser looks first in the pre-declared namespaces, then in
	 * the supplied NamespaceContext object.
	 * 
	 * @param prefix
	 *          The namespace prefix. Must not be null. Must not be the empty
	 *          string ("") - unqualified names in an XPath expression always
	 *          refer to the null namespace.
	 * @param uri
	 *          The namespace URI. Must not be null.
	 */
	public void declareNamespace(String prefix, String uri) {
		if (prefix == null) {
			throw new NullPointerException("Null prefix supplied to declareNamespace()");
		}
		if (uri == null) {
			throw new NullPointerException("Null namespace URI supplied to declareNamespace()");
		}
		_namespaces.put(prefix, uri);
		_namePool.allocateNamespaceCode(prefix, uri);
	}
	/**
	 * Clear all the declared namespaces, except for the standard ones (xml, xslt,
	 * saxon, xdt). This doesn't clear the namespace context set using
	 * {@link #setNamespaceContext}
	 */
	public void clearNamespaces() {
		_namespaces.clear();
		declareNamespace("xml", NamespaceConstant.XML);
		declareNamespace("xsl", NamespaceConstant.XSLT);
		declareNamespace("saxon", NamespaceConstant.SAXON);
		declareNamespace("xs", NamespaceConstant.SCHEMA);
		declareNamespace("xdt", NamespaceConstant.XDT);
		declareNamespace("", "");
	}
	/**
	 * Clear all the declared namespaces, including the standard ones (xml, xslt,
	 * saxon). Leave only the XML namespace and the default namespace (xmlns="")
	 */
	public void clearAllNamespaces() {
		_namespaces.clear();
		declareNamespace("xml", NamespaceConstant.XML);
		declareNamespace("", "");
	}
	/**
	 * Set all the declared namespaces to be the namespaces that are in-scope for
	 * a given node. In addition, the standard namespaces (xml, xslt, saxon) are
	 * declared.
	 * 
	 * @param node
	 *          The node whose in-scope namespaces are to be used as the context
	 *          namespaces. Note that this will have no effect unless this node is
	 *          an element.
	 */
	public void setNamespaces(NodeInfo node) {
		_namespaces.clear();
		AxisIterator iter = node.iterateAxis(Axis.NAMESPACE);
		while (true) {
			NodeInfo ns = (NodeInfo) iter.next();
			if (ns == null) {
				return;
			}
			declareNamespace(ns.getLocalPart(), ns.getStringValue());
		}
	}
	/**
	 * Set the base URI in the static context
	 */
	public void setBaseURI(String baseURI) {
		this._baseURI = baseURI;
	}
	/**
	 * Declare a named collation
	 * 
	 * @param name
	 *          The name of the collation (technically, a URI)
	 * @param comparator
	 *          The Java Comparator used to implement the collating sequence
	 * @param isDefault
	 *          True if this is to be used as the default collation
	 */
	public void declareCollation(String name, Comparator comparator, boolean isDefault) {
		_collations.put(name, comparator);
		if (isDefault) {
			_defaultCollationName = name;
		}
	}
	/**
	 * Get the stack frame map containing the slot number allocations for the
	 * variables declared in this static context
	 */
	public SlotManager getStackFrameMap() {
		return _stackFrameMap;
	}
	/**
	 * Declare a variable. A variable may be declared before an expression
	 * referring to it is compiled. Alternatively, a JAXP XPathVariableResolver
	 * may be supplied to perform the resolution. A variable that has been
	 * explicitly declared is used in preference.
	 * 
	 * @param qname
	 *          Lexical QName identifying the variable. The namespace prefix, if
	 *          any, must have been declared before this method is called, or must
	 *          be resolvable using the namespace context.
	 * @param initialValue
	 *          The initial value of the variable. A Java object that can be
	 *          converted to an XPath value.
	 */
	public Variable declareVariable(String qname, Object initialValue)
			throws XPathException {
		String prefix;
		String localName;
		try {
			String[] parts = Name.getQNameParts(qname);
			prefix = parts[0];
			localName = parts[1];
		} catch (QNameException err) {
			throw new StaticError("Invalid QName for variable: " + qname);
		}
		String uri = "";
		if (!("".equals(prefix))) {
			uri = getURIForPrefix(prefix);
		}
		Variable var = Variable.make(qname, getConfiguration());
		var.setValue(initialValue);
		int fingerprint = _namePool.allocate(prefix, uri, localName) & 0xfffff;
		_variables.put(Integer.valueOf(fingerprint), var);
		_stackFrameMap.allocateSlotNumber(fingerprint);
		return var;
	}
	/**
	 * Get the NamePool used for compiling expressions
	 */
	public NamePool getNamePool() {
		return _namePool;
	}
	/**
	 * Issue a compile-time warning. This method is used during XPath expression
	 * compilation to output warning conditions. The default implementation writes
	 * the message to System.err. To change the destination of messages, create a
	 * subclass of StandaloneContext that overrides this method.
	 */
	public void issueWarning(String s, SourceLocator locator) {
		System.err.println(s);
	}
	/**
	 * Get the system ID of the container of the expression. Used to construct
	 * error messages.
	 * 
	 * @return "" always
	 */
	public String getSystemId() {
		return "";
	}
	/**
	 * Get the Base URI of the stylesheet element, for resolving any relative
	 * URI's used in the expression. Used by the document() function,
	 * resolve-uri(), etc.
	 * 
	 * @return "" if no base URI has been set
	 */
	public String getBaseURI() {
		return _baseURI == null ? "" : _baseURI;
	}
	/**
	 * Get the line number of the expression within that container. Used to
	 * construct error messages.
	 * 
	 * @return -1 always
	 */
	public int getLineNumber() {
		return -1;
	}
	/**
	 * Get the URI for a prefix, using the declared namespaces as the context for
	 * namespace resolution. The default namespace is NOT used when the prefix is
	 * empty. This method is provided for use by the XPath parser.
	 * 
	 * @param prefix
	 *          The prefix
	 * @throws net.sf.saxon.trans.XPathException
	 *           if the prefix is not declared
	 */
	public String getURIForPrefix(String prefix) throws XPathException {
		String uri = getURIForPrefix(prefix, false);
		if (uri == null) {
			throw new StaticError("Prefix " + prefix + " has not been declared");
		}
		return uri;
	}
	/**
	 * Get the namespace URI corresponding to a given prefix. Return null if the
	 * prefix is not in scope. This method first searches any namespaces declared
	 * using {@link #declareNamespace(String, String)}, and then searches any
	 * namespace context supplied using
	 * {@link #setNamespaceContext(javax.xml.namespace.NamespaceContext)}.
	 * 
	 * @param prefix
	 *          the namespace prefix
	 * @param useDefault
	 *          true if the default namespace is to be used when the prefix is ""
	 * @return the uri for the namespace, or null if the prefix is not in scope.
	 *         Return "" if the prefix maps to the null namespace.
	 */
	public String getURIForPrefix(String prefix, boolean useDefault) {
		if (prefix.equals("") && !useDefault) {
			return "";
		} else {
			String uri = _namespaces.get(prefix);
      return (uri == null)
        ? _nsCtx.getNamespaceURI(prefix)
        : uri;
		}
	}
	/**
	 * Get an iterator over all the prefixes declared in this namespace context.
	 * This will include the default namespace (prefix="") and the XML namespace
	 * where appropriate. The iterator only covers namespaces explicitly declared
	 * using {@link #declareNamespace(String, String)}; it does not include
	 * namespaces declared using
	 * {@link #setNamespaceContext(javax.xml.namespace.NamespaceContext)},
	 * because the JAXP {@link NamespaceContext}class provides no way to discover
	 * all the namespaces available.
	 */
	public Iterator iteratePrefixes() {
		return _namespaces.keySet().iterator();
	}
	/**
	 * Bind a variable used in an XPath Expression to the XSLVariable element in
	 * which it is declared. This method is provided for use by the XPath parser,
	 * and it should not be called by the user of the API, or overridden, unless
	 * variables are to be declared using a mechanism other than the
	 * declareVariable method of this class.
	 * <p>
	 * If the variable has been explicitly declared using
	 * {@link #declareVariable(String, Object)}, that value is used; otherwise if
	 * a variable resolved has been supplied using
	 * {@link #setXPathVariableResolver(javax.xml.xpath.XPathVariableResolver)}
	 * then that is used.
	 * 
	 * @throws StaticError
	 *           If no variable with the given name is found, or if the value
	 *           supplied for the variable cannot be converted to an XPath value.
	 */
	public VariableDeclaration bindVariable(int fingerprint) throws StaticError {
		throw new UnsupportedOperationException();
	}
	/**
	 * Get the function library containing all the in-scope functions available in
	 * this static context
	 */
	public FunctionLibrary getFunctionLibrary() {
		return _functionLibrary;
	}
	/**
	 * Set the function library to be used
	 */
	public void setFunctionLibrary(FunctionLibrary lib) {
		_functionLibrary = lib;
	}
	/**
	 * Get a named collation.
	 * 
	 * @return the collation identified by the given name, as set previously using
	 *         declareCollation. Return null if no collation with this name is
	 *         found.
	 */
	public Comparator getCollation(String name) {
		try {
			return CollationFactory.makeCollationFromURI(name,_config);
		} catch (XPathException e) {
			return null;
		}
	}
	/**
	 * Get the name of the default collation.
	 * 
	 * @return the name of the default collation; or the name of the codepoint
	 *         collation if no default collation has been defined
	 */
	public String getDefaultCollationName() {
		if (_defaultCollationName != null) {
			return _defaultCollationName;
		} else {
			return NamespaceConstant.CodepointCollationURI;
		}
	}
	/**
	 * Set the default namespace for element and type names
	 */
	public void setDefaultElementNamespace(String uri) {
		_defaultElementNamespace = _namePool.allocateCodeForURI(uri);
	}
	/**
	 * Get the default XPath namespace, as a namespace code that can be looked up
	 * in the NamePool
	 */
	public short getDefaultElementNamespace() {
		return _defaultElementNamespace;
	}
	/**
	 * Set the default function namespace
	 */
	public void setDefaultFunctionNamespace(String uri) {
		_defaultFunctionNamespace = uri;
	}
	/**
	 * Get the default function namespace
	 */
	public String getDefaultFunctionNamespace() {
		return _defaultFunctionNamespace;
	}
	/**
	 * Set XPath 1.0 backwards compatibility mode
	 * 
	 * @param backwardsCompatible
	 *          if true, expressions will be evaluated with XPath 1.0
	 *          compatibility mode set to true.
	 */
	public void setBackwardsCompatibilityMode(boolean backwardsCompatible) {
		this._backwardsCompatible = true;
	}
  
  
  
	/**
	 * Determine whether Backwards Compatible Mode is used
	 * 
	 * @return false; XPath 1.0 compatibility mode is not supported in the
	 *         standalone XPath API
	 */
	public boolean isInBackwardsCompatibleMode() {
		return _backwardsCompatible;
	}
  
	public boolean isAllowedBuiltInType(AtomicType arg0) {
    return true;
  }

  /**
	 * Determine whether a Schema for a given target namespace has been imported.
	 * Note that the in-scope element declarations, attribute declarations and
	 * schema types are the types registered with the (schema-aware)
	 * configuration, provided that their namespace URI is registered in the
	 * static context as being an imported schema namespace. (A consequence of
	 * this is that within a Configuration, there can only be one schema for any
	 * given namespace, including the null namespace).
	 * 
	 * @return This implementation always returns false: the standalone XPath API
	 *         does not support schema-aware processing.
	 */
	public boolean isImportedSchema(String namespace) {
		return false;
	}
	/**
	 * @see net.sf.saxon.expr.StaticContext#getNamespaceResolver()
	 */
	public NamespaceResolver getNamespaceResolver() {
		return this;
	}
}