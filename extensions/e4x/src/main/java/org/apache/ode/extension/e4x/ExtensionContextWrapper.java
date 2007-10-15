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
package org.apache.ode.extension.e4x;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.runtime.extension.ExtensionContext;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.xml.XMLObject;
import org.mozilla.javascript.xmlimpl.XMLLibImpl;
import org.w3c.dom.Node;

/**
 * Scriptable wrapper for the <code>ExtensionContext</code>. This
 * is needed for a transparent transformation between org.w3c.dom.Node 
 * and Rhino's XML implementation.
 * 
 * Currently only 
 * <pre>Node readVariable(String)</pre> and <pre>void writeVariable(String, Node)</pre>
 * are implemented. 
 * 
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class ExtensionContextWrapper extends ScriptableObject {
	private static final long serialVersionUID = 1L;

	private ExtensionContext _ectx;
	private Context _sctx;
	
	public ExtensionContextWrapper() {
		super();
	}

	public ExtensionContextWrapper(ExtensionContext ectx, Context sctx) {
		super();
		_ectx = ectx;
		_sctx = sctx;
	}
	
	public String getClassName() {
		return "ExtensionContext";
	}

	public static Scriptable jsConstructor(Context cx, Object[] args,
			Function ctorObj,
			boolean inNewExpr)
	{
		if (args.length == 2 && (args[0] instanceof ExtensionContext) && (args[1] instanceof Context)) {
			return new ExtensionContextWrapper((ExtensionContext)args[0], (Context)args[1]);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	public void jsFunction_writeVariable(String varName, XMLObject node) throws FaultException {
		Node n = js2node(node);
		_ectx.writeVariable(varName, n);
	}

	public XMLObject jsFunction_readVariable(String varName) throws FaultException, TransformerException {
		Node n = _ectx.readVariable(varName);
		return node2js(n);
	}

	private Node js2node(final XMLObject xml) {
		return XMLLibImpl.toDomNode(xml);
	}
	
	public XMLObject node2js(Node n) throws TransformerException {
		Scriptable parentScope = getParentScope();
		return (XMLObject) _sctx.newObject(parentScope, "XML", new Object[] {Context.javaToJS(domToString(n), parentScope)});
	}
	
	public static String domToString(Node n) throws TransformerException {
		TransformerFactory xformFactory	= TransformerFactory.newInstance();
		Transformer idTransform = xformFactory.newTransformer();
		idTransform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		Source input = new DOMSource(n);
		StringWriter sw = new StringWriter();
		Result output = new StreamResult(sw);
		idTransform.transform(input, output);
		return sw.toString();
	}
}
