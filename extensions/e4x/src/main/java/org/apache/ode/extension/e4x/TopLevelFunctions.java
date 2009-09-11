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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.rtrep.common.extension.ExtensionContext;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.xml.XMLObject;
import org.mozilla.javascript.xmlimpl.XMLLibImpl;
import org.w3c.dom.Node;

/**
 * Provides (global) top-level functions like "print" and
 * "load" to our JS/E4X environment.
 * 
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class TopLevelFunctions extends ImporterTopLevel {
	private static final long serialVersionUID = 1L;
	private static final String[] METHODS = { "load", "print", "pid", "activityName", "js2dom", "dom2js", "throwFault" };
	private URI _duDir;
	private ExtensionContext _ectx;
	
	public TopLevelFunctions(ExtensionContext ectx, Context cx, URI duDir) throws Exception {
		super(cx);
		this._duDir = duDir;
		this._ectx = ectx;
		
		// define toplevel functions
		defineFunctionProperties(METHODS, TopLevelFunctions.class,
				ScriptableObject.DONTENUM);
		
		// register _context object
		ScriptableObject.putProperty(this, "_context", _ectx);
	}

	/**
	 * Allows printing debug output to the console. Output will be redirected
	 * to the logger associated with <code>org.apache.ode.extension.e4x.JavaScriptConsole</code>.
	 * The target log level is INFO.
	 */
	public static void print(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			if (i > 0) {
				sb.append(' ');
			}
			sb.append(Context.toString(args[i]));
		}
		
		TopLevelFunctions thiz = (TopLevelFunctions) getTopLevelScope(thisObj);
		thiz._ectx.printToConsole(sb.toString());
	}

	/**
	 * This methods is exposed to the JS environment and supports loading
	 * JavaScript libraries from the deployment unit directory. 
	 */
	public static void load(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) {
		TopLevelFunctions thiz = (TopLevelFunctions) getTopLevelScope(thisObj);
        for (int i = 0; i < args.length; i++) {
            // Assumes resource's path is given relative to the service archive
        	URI uri = thiz._duDir.resolve(Context.toString(args[i]));
        	try {
				InputStream is = uri.toURL().openStream();
				cx.evaluateReader(thiz, new InputStreamReader(is), "<importJS>", 1, null);
			} catch (MalformedURLException e) {
				throw Context.throwAsScriptRuntimeEx(e);
			} catch (IOException e) {
				throw Context.throwAsScriptRuntimeEx(e);
			}
        }
	}
	
	/**
	 * This method is exposed to the JS environment and returns the process
	 * instance ID of the running PI.
	 */
	public static long pid(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) {
		TopLevelFunctions thiz = (TopLevelFunctions) getTopLevelScope(thisObj);
		return thiz._ectx.getProcessId();
	}

	/**
	 * This method is exposed to the JS environment and returns the activity 
	 * name of the running PI.
	 */
	public static String activityName(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) {
		TopLevelFunctions thiz = (TopLevelFunctions) getTopLevelScope(thisObj);
		return thiz._ectx.getActivityName();
	}

	/**
	 * This method is exposed to the JS environment and allows converting from
	 * JavaScript E4X objects to W3C DOM nodes.
	 */
	public static Node js2dom(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) {
		if (args.length != 1) {
			Context.reportError("js2dom expects one E4X XML parameter");
		}
		return XMLLibImpl.toDomNode(args[0]); 
	}
	
	/**
	 * This method is exposed to the JS environment and allows converting from
	 * W3C DOM nodes to JavaScript E4X objects.
	 */
	public static XMLObject dom2js(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) {
		if (args.length != 1) {
			Context.reportError("dom2js expects one org.w3c.dom.Node parameter");
		}
		TopLevelFunctions thiz = (TopLevelFunctions) getTopLevelScope(thisObj);
		try {
			Object n = args[0];
			if (n instanceof NativeJavaObject && ((NativeJavaObject)n).unwrap() instanceof Node) {
				n = ((NativeJavaObject)args[0]).unwrap();
			}
			String xml = domToString((Node)n);
			return (XMLObject)cx.newObject(thiz, "XML", new Object[] {Context.javaToJS(xml, thiz)});
		} catch (TransformerException e) {
			throw Context.throwAsScriptRuntimeEx(e);
		}
	}
	
	/**
	 * This method is exposed to the JS environment and allows users to
	 * throw BPEL faults.
	 * 
	 * @throws FaultException 
	 */
	public static void throwFault(Context cx, Scriptable thisObj, Object[] args,
			Function funObj) throws FaultException {
		if (args.length != 3) {
			Context.reportError("throwFault expects the following parameters: throwFault(namespace, localname, faultMessage)");
		}
		String ns = Context.toString(args[0]);
		String localname = Context.toString(args[1]);
		String msg = Context.toString(args[2]);
		throw new FaultException(new QName(ns, localname), msg);
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
