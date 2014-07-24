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
package org.apache.ode.bpel.elang.xpath10.obj;

import java.net.URI;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.obj.ExtensibleImpl;
import org.apache.ode.bpel.obj.OBase;
import org.apache.ode.bpel.obj.OExpression;
import org.apache.ode.bpel.obj.OLValueExpression;
import org.apache.ode.bpel.obj.OLink;
import org.apache.ode.bpel.obj.OMessageVarType;
import org.apache.ode.bpel.obj.OProcess;
import org.apache.ode.bpel.obj.OScope;
import org.apache.ode.bpel.obj.OScope.Variable;
import org.apache.ode.bpel.obj.OXslSheet;
import org.apache.ode.bpel.obj.OMessageVarType.Part;
import org.apache.ode.bpel.obj.OProcess.OProperty;
import org.apache.ode.utils.NSContext;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Jaxen-based compiled-xpath representation for XPATH 1.0 expression language.
 */
public class OXPath10Expression extends OLValueExpression {

	/** Map from query name to variable declaration. */
	private static final String VARS = "vars";
	private static final String PROPERTIES = "properties";

	private static final String LINKS = "links";
	/** Map getVariableData invocation signature to compiled objects. */
	private final String GETVARIABLEDATASIGS = "_getVariableDataSigs";
	private static final String XPATH = "xpath";

	private static final String NAMESPACECTX = "namespaceCtx";
	/** QName of the <code>bpws:getVariableData</code> function. */
	private static final String QNAME_GETVARIABLEDATA = "qname_getVariableData";

	/** QName of the <code>bpws:getVariableData</code> function. */
	private static final String QNAME_GETVARIABLEPROPERTY = "qname_getVariableProperty";
	/** QName of the <code>bpws:getVariableData</code> function. */
	private static final String QNAME_GETLINKSTATUS = "qname_getLinkStatus";

	private static final String XSLSHEETS = "xslSheets";

	@JsonCreator
	public OXPath10Expression(){}
	public OXPath10Expression(OProcess owner, QName qname_getVariableData,
			QName qname_getVariableProperty, QName qname_getLinkStatus) {
		super(owner);
		setQname_getLinkStatus(qname_getLinkStatus);
		setQname_getVariableData(qname_getVariableData);
		setQname_getVariableProperty(qname_getVariableProperty);
		setVars(new HashMap<String,Variable>());
		setProperties(new HashMap<String, OProperty>());
		setLinks(new HashMap<String, OLink>());
		setXslSheets(new HashMap<URI, OXslSheet>());
		setGetVariableDataSigs(new HashMap<SigGetVariableData,OSigGetVariableData>());
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	private HashMap<SigGetVariableData,OSigGetVariableData> getGetVariableDataSigs(){
		Object o = fieldContainer.get(GETVARIABLEDATASIGS);
		return o == null ? null : (HashMap<SigGetVariableData,OSigGetVariableData>)o;
	}
	private void setGetVariableDataSigs(HashMap<SigGetVariableData,OSigGetVariableData> dataSigs){
		fieldContainer.put(GETVARIABLEDATASIGS, dataSigs);
	}
	/**
	   * Add a compiled representation for a certain <code>bpws:getVariableData(...)</code> call.
	   * @param varname variable name
	   * @param partname part name
	   * @param location location query
	   * @param compiled compiled representation
	   */
	public void addGetVariableDataSig(String varname, String partname,
			String location, OSigGetVariableData compiled) {
		getGetVariableDataSigs().put(new SigGetVariableData(varname, partname,
				location), compiled);
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public HashMap<String, OLink> getLinks() {
		Object o = fieldContainer.get(LINKS);
		return o == null ? null : (HashMap<String, OLink>)o;
	}

	@JsonIgnore
	public NSContext getNamespaceCtx() {
		Object o = fieldContainer.get(NAMESPACECTX);
		return o == null ? null : (NSContext)o;
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public HashMap<String, OProperty> getProperties() {
		Object o = fieldContainer.get(PROPERTIES);
		return o == null ? null : (HashMap<String, OProperty>)o;
	}

	@JsonIgnore
	public QName getQname_getLinkStatus() {
		Object o = fieldContainer.get(QNAME_GETLINKSTATUS);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQname_getVariableData() {
		Object o = fieldContainer.get(QNAME_GETVARIABLEDATA);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQname_getVariableProperty() {
		Object o = fieldContainer.get(QNAME_GETVARIABLEPROPERTY);
		return o == null ? null : (QName)o;
	}

	/**
	 * @see org.apache.ode.bpel.o.OLValueExpression#getVariable()
	 */
	@JsonIgnore
	public Variable getVariable() {
		if (getVars().size() == 0)
			throw new IllegalStateException(
					"LValue must have one variable reference.");
		// We're interested in the first variable referenced by the LValue
		for (String varName : getVars().keySet()) {
			if (getXpath().substring(1, getXpath().length()).startsWith(varName))
				return getVars().get(varName);
		}
		throw new IllegalStateException(
				"Either the expression doesn't start with a variable reference or "
						+ "the reference is unknow.");
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public HashMap<String, Variable> getVars() {
		Object o = fieldContainer.get(VARS);
		return o == null ? null : (HashMap<String, Variable>)o;
	}

	@JsonIgnore
	public String getXpath() {
		Object o = fieldContainer.get(XPATH);
		return o == null ? null : (String)o;
	}

	public OXslSheet getXslSheet(URI projectRelativeXslUri) {
		return getXslSheets().get(projectRelativeXslUri);
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	protected HashMap<URI, OXslSheet> getXslSheets() {
		Object o = fieldContainer.get(XSLSHEETS);
		return o == null ? null : (HashMap<URI, OXslSheet>)o;
	}

	/**
	   * Get the compiled representation of a certain <code>bpws:getVariableData(...)</code> call.
	   * @param varname variable name
	   * @param partname part name
	   * @param location location query
	   * @return compiled representation, or <code>null</code> if none found
	   */
	public OSigGetVariableData resolveGetVariableDataSig(String varname,
			String partname, String location) {
		SigGetVariableData key = new SigGetVariableData(varname, partname,
				location);
		return getGetVariableDataSigs().get(key);
	}

	public void setLinks(HashMap<String, OLink> links) {
		fieldContainer.put(LINKS, links);
	}

	public void setNamespaceCtx(NSContext namespaceCtx) {
		fieldContainer.put(NAMESPACECTX, namespaceCtx);
	}

	public void setProperties(HashMap<String, OProperty> properties) {
		fieldContainer.put(PROPERTIES, properties);
	}

	public void setQname_getLinkStatus(QName qname_getLinkStatus) {
		fieldContainer.put(QNAME_GETLINKSTATUS, qname_getLinkStatus);
	}

	public void setQname_getVariableData(QName qname_getVariableData) {
		fieldContainer.put(QNAME_GETVARIABLEDATA, qname_getVariableData);
	}

	public void setQname_getVariableProperty(QName qname_getVariableProperty) {
		fieldContainer
				.put(QNAME_GETVARIABLEPROPERTY, qname_getVariableProperty);
	}

	public void setVars(HashMap<String, Variable> vars) {
		fieldContainer.put(VARS, vars);
	}

	public void setXpath(String xpath) {
		fieldContainer.put(XPATH, xpath);
	}

	public void setXslSheet(URI projectRelativeXslUri, OXslSheet xslSheet) {
		getXslSheets().put(projectRelativeXslUri, xslSheet);
	}

	protected void setXslSheets(HashMap<URI, OXslSheet> xslSheets) {
		fieldContainer.put(XSLSHEETS, xslSheets);
	}

	public String toString() {
		Object o = "}";
		return o == null ? null : "{OXPath10Expression " + getXpath() +o;
	}

	/**
	 * Data structure representing the compiled signature of a <code>bpws:getVariableData</code>
	 * invocation.
	 */
	public static class OSigGetVariableData extends OBase {
		private static final String VARIABLE = "variable";
		private static final String PART = "part";

		private static final String LOCATION = "location";

		@JsonCreator
		public OSigGetVariableData(){}
		public OSigGetVariableData(OProcess owner, OScope.Variable variable,
				OMessageVarType.Part part, OExpression location) {
			super(owner);
			setVariable(variable);
			setPart(part);
			setLocation(location);
		}

		@JsonIgnore
		public OExpression getLocation() {
			Object o = fieldContainer.get(LOCATION);
		return o == null ? null : (OExpression)o;
		}

		@JsonIgnore
		public Part getPart() {
			Object o = fieldContainer.get(PART);
		return o == null ? null : (Part)o;
		}

		@JsonIgnore
		public Variable getVariable() {
			Object o = fieldContainer.get(VARIABLE);
		return o == null ? null : (Variable)o;
		}

		public void setLocation(OExpression location) {
			fieldContainer.put(LOCATION, location);
		}

		public void setPart(Part part) {
			fieldContainer.put(PART, part);
		}

		public void setVariable(Variable variable) {
			fieldContainer.put(VARIABLE, variable);
		}
	}

	/**
	   * Data structure representing the signature of a <code>bpws:getVariableData</code>
	   * invocation.
	   */
	public static class SigGetVariableData extends ExtensibleImpl{

		private final String VARNAME = "varname";
		private final String PARTNAME = "partname";
		private final String LOCATION = "location";

		@JsonCreator
		SigGetVariableData(){}
		private SigGetVariableData(String varname, String partname,
				String location) {
			setVarname(varname);
			setPartname(partname);
			setLocation(location);
		}

		public boolean equals(Object obj) {
			SigGetVariableData other = (SigGetVariableData) obj;
			if (getVarname() != null
					&& (other.getVarname() == null || !getVarname().equals(other.getVarname())))
				return false;
			if (getPartname() != null
					&& (other.getPartname() == null || !getPartname()
							.equals(other.getPartname())))
				return false;
			if (getLocation() != null
					&& (other.getLocation() == null || !getLocation()
							.equals(other.getLocation())))
				return false;

			return true;
		}

		public int hashCode() {
			int hashCode = 0;
			if (getVarname() != null)
				hashCode ^= getVarname().hashCode();
			if (getPartname() != null)
				hashCode ^= getPartname().hashCode();
			if (getLocation() != null)
				hashCode ^= getLocation().hashCode();
			return hashCode;
		}

		@JsonIgnore
		private String getVarname() {
			return (String)fieldContainer.get(VARNAME);
		}

		@JsonIgnore
		private String getPartname() {
			return (String)fieldContainer.get(PARTNAME);
		}

		@JsonIgnore
		private String getLocation() {
			return (String)fieldContainer.get(LOCATION);
		}
		
		private void setVarname(String varname) {
			fieldContainer.put(VARNAME, varname);
			
		}
		private void setPartname(String partname){
			fieldContainer.put(PARTNAME, partname);
		}
		private void setLocation(String location){
			fieldContainer.put(LOCATION, location);
		}
	}

}
