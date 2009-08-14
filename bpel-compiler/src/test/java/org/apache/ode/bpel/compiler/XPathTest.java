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
package org.apache.ode.bpel.compiler;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import junit.framework.TestCase;

import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.api.CompilerContext;
import org.apache.ode.bpel.compiler.api.ExpressionCompiler;
import org.apache.ode.bpel.compiler.api.SourceLocation;
import org.apache.ode.bpel.compiler.bom.Activity;
import org.apache.ode.bpel.compiler.bom.BpelObject;
import org.apache.ode.bpel.compiler.bom.Expression;
import org.apache.ode.bpel.compiler.bom.ScopeLikeActivity;
import org.apache.ode.bpel.elang.xpath10.compiler.XPath10ExpressionCompilerBPEL11;
import org.apache.ode.bpel.elang.xpath10.compiler.XPath10ExpressionCompilerBPEL20;
import org.apache.ode.bpel.elang.xpath10.compiler.XPath10ExpressionCompilerBPEL20Draft;
import org.apache.ode.bpel.elang.xpath20.compiler.XPath20ExpressionCompilerBPEL20;
import org.apache.ode.bpel.elang.xpath20.compiler.XPath20ExpressionCompilerBPEL20Draft;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OElementVarType;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.bpel.o.OLValueExpression;
import org.apache.ode.bpel.o.OLink;
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OVarType;
import org.apache.ode.bpel.o.OXsdTypeVarType;
import org.apache.ode.bpel.o.OXslSheet;
import org.apache.ode.bpel.o.OMessageVarType.Part;
import org.apache.ode.bpel.o.OProcess.OProperty;
import org.apache.ode.bpel.o.OProcess.OPropertyAlias;
import org.apache.ode.bpel.o.OScope.CorrelationSet;
import org.apache.ode.bpel.o.OScope.Variable;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.NSContext;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XPathTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testEmptyXPath10StringBPEL11() throws Exception {
		XPath10ExpressionCompilerBPEL11 compiler = new XPath10ExpressionCompilerBPEL11();
		assertCompilationExceptionForEmptyXPath(compiler);
	}

	public void testEmptyXPath10StringBPEL20Draft() throws Exception {
		XPath10ExpressionCompilerBPEL20Draft compiler = new XPath10ExpressionCompilerBPEL20Draft();
		assertCompilationExceptionForEmptyXPath(compiler);
	}

	public void testEmptyXPath10StringBPEL20() throws Exception {
		XPath10ExpressionCompilerBPEL20 compiler = new XPath10ExpressionCompilerBPEL20();
		assertCompilationExceptionForEmptyXPath(compiler);
	}

	public void testEmptyXPath20StringBPEL20Draft() throws Exception {
		XPath20ExpressionCompilerBPEL20Draft compiler = new XPath20ExpressionCompilerBPEL20Draft();
		assertCompilationExceptionForEmptyXPath(compiler);
	}

	public void testEmptyXPath20StringBPEL20() throws Exception {
		XPath20ExpressionCompilerBPEL20 compiler = new XPath20ExpressionCompilerBPEL20();
		assertCompilationExceptionForEmptyXPath(compiler);
	}

	private void assertCompilationExceptionForEmptyXPath(
			ExpressionCompiler compiler) throws SAXException, IOException {
		compiler.setCompilerContext(new MockCompilerContext());
		Element element = DOMUtils.stringToDOM("<condition> </condition>");
		Expression xpath = new Expression(element);
		try {
			compiler.compile(xpath);
			throw new Exception("Empty string is invalid XPath");
		} catch (Exception except) {
			// System.out.println("Empty XPath caused: " + except.getClass());
			// except.printStackTrace();
			assertTrue("Expected a CompilationException",
					except instanceof CompilationException);
		}
	}
}

class MockCompilerContext implements CompilerContext {
	private OProcess _oprocess = new OProcess("20");

	private Map<String, Variable> _vars = new HashMap<String, Variable>();

	public OExpression constantExpr(boolean value) {
		return null;
	}

	public OExpression compileJoinCondition(Expression expr)
			throws CompilationException {
		return null;
	}

	public OExpression compileExpr(Expression expr) throws CompilationException {
		return null;
	}

	public OLValueExpression compileLValueExpr(Expression expr)
			throws CompilationException {
		return null;
	}

	public OXslSheet compileXslt(String docStrUri) throws CompilationException {
		return null;
	}

	public OXsdTypeVarType resolveXsdType(QName typeName)
			throws CompilationException {
		return null;
	}

	public OProperty resolveProperty(QName name) throws CompilationException {
		return null;
	}

	public Variable resolveVariable(String name) throws CompilationException {
		return _vars.get(name);
	}

	public List<Variable> getAccessibleVariables() {
		return new ArrayList<Variable>(_vars.values());
	}

	public Variable resolveMessageVariable(String inputVar)
			throws CompilationException {
		return _vars.get(inputVar);
	}

	public Variable resolveMessageVariable(String inputVar, QName messageType)
			throws CompilationException {
		return _vars.get(inputVar);
	}

	public Part resolvePart(Variable variable, String partname)
			throws CompilationException {
		return ((OMessageVarType) variable.type).parts.get(partname);
	}

	public OActivity compile(Activity child) throws CompilationException {
		// TODO Auto-generated method stub
		return null;
	}

	public OActivity compileSLC(Activity source) throws CompilationException {
		// TODO Auto-generated method stub
		return null;
	}

	public OPartnerLink resolvePartnerLink(String name)
			throws CompilationException {
		// TODO Auto-generated method stub
		return null;
	}

	public Operation resolvePartnerRoleOperation(OPartnerLink partnerLink,
			String operationName) throws CompilationException {
		// TODO Auto-generated method stub
		return null;
	}

	public Operation resolveMyRoleOperation(OPartnerLink partnerLink,
			String operationName) throws CompilationException {
		// TODO Auto-generated method stub
		return null;
	}

	public OPropertyAlias resolvePropertyAlias(Variable variable, QName property)
			throws CompilationException {
		// TODO Auto-generated method stub
		return null;
	}

	public void recoveredFromError(Object where, CompilationException bce)
			throws CompilationException {
	}

	public OLink resolveLink(String linkName) throws CompilationException {
		return null;
	}

	public OScope resolveCompensatableScope(String scopeToCompensate)
			throws CompilationException {
		return null;
	}

	public OProcess getOProcess() throws CompilationException {
		return _oprocess;
	}

	public CorrelationSet resolveCorrelationSet(String csetName)
			throws CompilationException {
		return null;
	}

	public String getSourceLocation() {
		return null;
	}

	public void compile(OActivity context, BpelObject activity, Runnable run) {
	}

	public boolean isPartnerLinkAssigned(String plink) {
		return false;
	}

	public List<OActivity> getActivityStack() {
		return null;
	}

	public void registerElementVar(String name, QName type) {
		OElementVarType varType = new OElementVarType(getOProcess(), type);
		OScope.Variable var = new OScope.Variable(getOProcess(), varType);
		var.name = name;
		_vars.put(name, var);
	}

	public OExpression compileExpr(String locationstr, NSContext nsContext) {
		return null;
	}

	public OActivity getCurrent() {
		return null;
	}

	public OScope compileSLC(ScopeLikeActivity child, Variable[] variables) {
		return null;
	}

	public void recoveredFromError(SourceLocation location,
			CompilationException error) {
	}

    public Part resolveHeaderPart(Variable variable, String partname) throws CompilationException {
        return null;
    }
    
    public Map<URI, Source> getSchemaSources() {
    	return null;
    }

	public URI getBaseResourceURI() {
		// TODO Auto-generated method stub
		return null;
	}

	public OExpression compileExpr(Expression expr, OVarType rootNodeType,
			Object requestedResultType, Object[] resultType)
			throws CompilationException {
		// TODO Auto-generated method stub
		return null;
	}

	public OLValueExpression compileLValueExpr(Expression expr,
			OVarType rootNodeType, Object requestedResultType,
			Object[] resultType) throws CompilationException {
		// TODO Auto-generated method stub
		return null;
	}
}
