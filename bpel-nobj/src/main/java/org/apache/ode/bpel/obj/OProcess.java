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
package org.apache.ode.bpel.obj;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.o.OProcess.OPropertyAlias;
import org.apache.ode.bpel.obj.OMessageVarType.Part;
import org.apache.ode.utils.NSContext;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Compiled BPEL process representation.
 */
public class OProcess extends OBase {
	public static int instanceCount = 0;
	private static final String GUID = "guid";
	/** BPEL version. */
	private static final String VERSION = "version";
	/** Various constants that are needed at runtime. */
	private static final String CONSTANTS = "constants";
	/** Universally Unique Identifier */
	private static final String UUID = "uuid";
	/** Namespace of the process. */
	private static final String TARGETNAMESPACE = "targetNamespace";
	/** Name of the process. */
	private static final String PROCESSNAME = "processName";
	/** ProcessImpl-level scope. */
	private static final String PROCESSCOPE = "procesScope";
	/** All partner links in the process. */
	private static final String ALLPARTNERLINKS = "allPartnerLinks";
	private static final String PROPERTIES = "properties";
	/** Date process was compiled. */
	private static final String COMPILEDATE = "compileDate";
	private static final String CHILDIDCOUNTER = "_childIdCounter";
	private static final String CHILDREN = "_children";
	private static final String EXPRESSIONLANGUAGES = "expressionLanguages";
	private static final String MESSAGETYPES = "messageTypes";
	private static final String ELEMENTTYPES = "elementTypes";
	private static final String XSDTYPES = "xsdTypes";
	private static final String XSLSHEETS = "xslSheets";
	private static final String NAMESPACECONTEXT = "namespaceContext";

	public OProcess(String bpelVersion) {
		super(null);
		setVersion(bpelVersion);
		instanceCount++;
		setAllPartnerLinks(new HashSet<OPartnerLink>());
		setProperties(new ArrayList<OProperty>());
		setChildren(new ArrayList<OBase>());
		setExpressionLanguages(new HashSet<OExpressionLanguage>());
		setMessageTypes(new HashMap<QName, OMessageVarType>());
		setElementTypes(new HashMap<QName, OElementVarType>());
		setXsdTypes(new HashMap<QName, OXsdTypeVarType>());
		setXslSheets(new HashMap<URI, OXslSheet>());
	}

	@Override
	public void dehydrate() {
		super.dehydrate();
		getProcesScope().dehydrate();
		getAllPartnerLinks().clear();
		for (OBase obase : getChildren()) {
			obase.dehydrate();
		}
		getChildren().clear();
		getMessageTypes().clear();
		getElementTypes().clear();
		getXsdTypes().clear();
		getXslSheets().clear();
	}

	@Override
	public String digest() {
		return getProcessName() + ";" + getProcesScope().digest();
	}

	protected void finalize() throws Throwable {
		instanceCount--;
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public Set<OPartnerLink> getAllPartnerLinks() {
		return Collections.unmodifiableSet((Set<OPartnerLink>) fieldContainer.get(ALLPARTNERLINKS));
	}

	public OBase getChild(final int id) {
		for (int i = getChildren().size() - 1; i >= 0; i--) {
			OBase child = getChildren().get(i);
			if (child.getId() == id)
				return child;
		}
		return null;
	}

	@JsonIgnore
	int getChildIdCounter() {
		return (int) fieldContainer.get(CHILDIDCOUNTER);
	}

	@JsonIgnore
	public List<OBase> getChildren() {
		return (List<OBase>) fieldContainer.get(CHILDREN);
	}

	@JsonIgnore
	public Date getCompileDate() {
		return (Date) fieldContainer.get(COMPILEDATE);
	}

	@JsonIgnore
	public OConstants getConstants() {
		return (OConstants) fieldContainer.get(CONSTANTS);
	}

	@JsonIgnore
	@SuppressWarnings("unchecked")
	public List<String> getCorrelators() {
		// MOVED from ProcessSchemaGenerator
		List<String> correlators = new ArrayList<String>();

		for (OPartnerLink plink : getAllPartnerLinks()) {
			if (plink.hasMyRole()) {
				for (Iterator opI = plink.getMyRolePortType().getOperations()
						.iterator(); opI.hasNext();) {
					Operation op = (Operation) opI.next();
					correlators.add(plink.getName() + "." + op.getName());
				}
			}
		}

		return correlators;
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public HashMap<QName, OElementVarType> getElementTypes() {
		return (HashMap<QName, OElementVarType>) fieldContainer
				.get(ELEMENTTYPES);
	}

	@JsonIgnore
	@SuppressWarnings("unchecked")
	public Collection getExpressionLanguages() {
		throw new UnsupportedOperationException(); // TODO: implement me!
	}

	@JsonIgnore
	public String getGuid() {
		return (String) fieldContainer.get(GUID);
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public HashMap<QName, OMessageVarType> getMessageTypes() {
		return (HashMap<QName, OMessageVarType>) fieldContainer
				.get(MESSAGETYPES);
	}
	@JsonIgnore
	public String getName() {
		return getProcessName();
	}

	@JsonIgnore
	public NSContext getNamespaceContext() {
		return (NSContext) fieldContainer.get(NAMESPACECONTEXT);
	}
	@JsonIgnore
	public OPartnerLink getPartnerLink(String name) {
		for (OPartnerLink partnerLink : getAllPartnerLinks()) {
			if (partnerLink.getName().equals(name)){
				return partnerLink;
			}
		}
		return null;
	}

	@JsonIgnore
	public OScope getProcesScope() {
		return (OScope) fieldContainer.get(PROCESSCOPE);
	}

	@JsonIgnore
	public String getProcessName() {
		return (String) fieldContainer.get(PROCESSNAME);
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<OProperty> getProperties() {
		return (List<OProperty>) fieldContainer.get(PROPERTIES);
	}

	@JsonIgnore
	public QName getQName() {
		return new QName(getTargetNamespace(), getProcessName());
	}

	public OScope getScope(String scopeName) {
		throw new UnsupportedOperationException();
	}

	@JsonIgnore
	public String getTargetNamespace() {
		return (String) fieldContainer.get(TARGETNAMESPACE);
	}

	@JsonIgnore
	public String getUuid() {
		return (String) fieldContainer.get(UUID);
	}

	@JsonIgnore
	public String getVersion() {
		return (String) fieldContainer.get(VERSION);
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public HashMap<QName, OXsdTypeVarType> getXsdTypes() {
		return (HashMap<QName, OXsdTypeVarType>) fieldContainer.get(XSDTYPES);
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public HashMap<URI, OXslSheet> getXslSheets() {
		return (HashMap<URI, OXslSheet>) fieldContainer.get(XSLSHEETS);
	}

	//	TODO: custom readObject
	public void setAllPartnerLinks(Set<OPartnerLink> allPartnerLinks) {
		if (getAllPartnerLinks() == null){
			fieldContainer.put(ALLPARTNERLINKS, allPartnerLinks);
		}
	}

	void setChildIdCounter(int childIdCounter) {
		fieldContainer.put(CHILDIDCOUNTER, childIdCounter);
	}

	void setChildren(List<OBase> children) {
		fieldContainer.put(CHILDREN, children);
	}

	public void setCompileDate(Date compileDate) {
		fieldContainer.put(COMPILEDATE, compileDate);
	}

	public void setConstants(OConstants constants) {
		fieldContainer.put(CONSTANTS, constants);
	}

	public void setElementTypes(HashMap<QName, OElementVarType> elementTypes) {
		if (getElementTypes() == null){
			fieldContainer.put(ELEMENTTYPES, elementTypes);
		}
	}

	public void setExpressionLanguages(
			HashSet<OExpressionLanguage> expressionLanguages) {
		if (getExpressionLanguages() == null){
			fieldContainer.put(EXPRESSIONLANGUAGES, expressionLanguages);
		}
	}

	public void setGuid(String guid) {
		fieldContainer.put(GUID, guid);
	}

	public void setMessageTypes(HashMap<QName, OMessageVarType> messageTypes) {
		if (getMessageTypes() == null){
			fieldContainer.put(MESSAGETYPES, messageTypes);
		}
	}

	public void setNamespaceContext(NSContext namespaceContext) {
		fieldContainer.put(NAMESPACECONTEXT, namespaceContext);
	}

	public void setProcesScope(OScope procesScope) {
		fieldContainer.put(PROCESSCOPE, procesScope);
	}

	public void setProcessName(String processName) {
		fieldContainer.put(PROCESSNAME, processName);
	}

	public void setProperties(List<OProperty> properties) {
		if (getProperties() == null){
			fieldContainer.put(PROPERTIES, properties);
		}
	}

	public void setTargetNamespace(String targetNamespace) {
		fieldContainer.put(TARGETNAMESPACE, targetNamespace);
	}

	public void setUuid(String uuid) {
		fieldContainer.put(UUID, uuid);
	}

	public void setVersion(String version) {
		fieldContainer.put(VERSION, version);
	}

	public void setXsdTypes(HashMap<QName, OXsdTypeVarType> xsdTypes) {
		if (getXsdTypes() == null){
			fieldContainer.put(XSDTYPES, xsdTypes);
		}
	}

	public void setXslSheets(HashMap<URI, OXslSheet> xslSheets) {
		if (getXslSheets() == null){
			fieldContainer.put(XSLSHEETS, xslSheets);
		}
	}

	public static class OProperty extends OBase {

		private static final String ALIASES = "aliases";
		private static final String NAME = "name";

		public OProperty(OProcess process) {
			super(process);
			setAliases(new ArrayList<OPropertyAlias>());
		}

		public OPropertyAlias getAlias(OVarType messageType) {
			for (OPropertyAlias aliase : getAliases())
				if (aliase.getVarType().equals(messageType))
					return aliase;
			return null;
		}

		@SuppressWarnings("unchecked")
		@JsonIgnore
		public List<OPropertyAlias> getAliases() {
			return (List<OPropertyAlias>) fieldContainer.get(ALIASES);
		}

		@JsonIgnore
		public QName getName() {
			return (QName) fieldContainer.get(NAME);
		}

		public void setAliases(List<OPropertyAlias> aliases) {
			fieldContainer.put(ALIASES, aliases);
		}

		public void setName(QName name) {
			fieldContainer.put(NAME, name);
		}

		public String toString() {
			return "{OProperty " + getName() + "}";
		}
	}

	public static class OPropertyAlias extends OBase {

		private static final String VARTYPE = "varType";

		/** For BPEL 1.1 */
		private static final String PART = "part";
		private static final String HEADER = "header";
		private static final String LOCATION = "location";

		public OPropertyAlias(OProcess owner) {
			super(owner);
		}

		public String getDescription() {
			StringBuffer buf = new StringBuffer(getVarType().toString());
			buf.append('[');
			buf.append(getPart() != null ? getPart().getName() : "");
			buf.append(getHeader() != null ? "header: " + getHeader() : "");
			if (getLocation() != null) {
				buf.append("][");
				buf.append(getLocation().toString());
			}
			buf.append(']');
			return buf.toString();
		}

		@JsonIgnore
		public String getHeader() {
			return (String) fieldContainer.get(HEADER);
		}

		@JsonIgnore
		public OExpression getLocation() {
			return (OExpression) fieldContainer.get(LOCATION);
		}

		@JsonIgnore
		public Part getPart() {
			return (Part) fieldContainer.get(PART);
		}

		@JsonIgnore
		public OVarType getVarType() {
			return (OVarType) fieldContainer.get(VARTYPE);
		}

		public void setHeader(String header) {
			fieldContainer.put(HEADER, header);
		}

		public void setLocation(OExpression location) {
			fieldContainer.put(LOCATION, location);
		}

		public void setPart(Part part) {
			fieldContainer.put(PART, part);
		}

		public void setVarType(OVarType varType) {
			fieldContainer.put(VARTYPE, varType);
		}

		public String toString() {
			return "{OPropertyAlias " + getDescription() + "}";
		}

	}
}
