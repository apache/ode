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
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.obj.OMessageVarType.Part;
import org.apache.ode.utils.NSContext;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import de.danielbechler.diff.annotation.ObjectDiffProperty;

/**
 * Compiled BPEL process representation.
 */
public class OProcess extends OBase  implements Serializable{
	public static final long serialVersionUID = -1L;
	
	/**
	 * Change log of class version
	 * initial 1
	 * current 2
	 * 
	 * 1->2:
	 * 	added namespaceContext attribute
	 *  */
	public static final int CURRENT_CLASS_VERSION = 2;

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
	
	/** All declared extensions in the process. **/
	private static final String DECLAREDEXTENSIONS = "declaredExtensions";
	/** All must-understand extensions in the process. **/
	private static final String MUSTUNDERSTANDEXTENSIONS = "mustUnderstandExtensions";
	
	/**
	 * This constructor should only be used by Jackson when deserialize.
	 */
	@JsonCreator
	public OProcess() {
		instanceCount ++;
		setChildIdCounter(0);
	}
	public OProcess(String version) {
		super(null);
		setVersion(version);
		instanceCount++;
		setAllPartnerLinks(new HashSet<OPartnerLink>());
		setProperties(new ArrayList<OProperty>());
		setChildren(new ArrayList<OBase>());
		setExpressionLanguages(new HashSet<OExpressionLanguage>());
		setMessageTypes(new HashMap<QName, OMessageVarType>());
		setElementTypes(new HashMap<QName, OElementVarType>());
		setXsdTypes(new HashMap<QName, OXsdTypeVarType>());
		setXslSheets(new HashMap<URI, OXslSheet>());
		
		setDeclaredExtensions(new HashSet<OExtension>());
		setMustUnderstandExtensions(new HashSet<OExtension>());
		
		setChildIdCounter(0);
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
		getDeclaredExtensions().clear();
		getMustUnderstandExtensions().clear();
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
		Set<OPartnerLink> links = (Set<OPartnerLink>) fieldContainer
				.get(ALLPARTNERLINKS);
		return links;
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
		Object o = fieldContainer.get(CHILDIDCOUNTER);
		return o == null ? 0 : (Integer)o;
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<OBase> getChildren() {
		Object o = fieldContainer.get(CHILDREN);
		return o == null ? null : (List<OBase>)o;
	}

	@JsonIgnore
	public Date getCompileDate() {
		Object o = fieldContainer.get(COMPILEDATE);
		return o == null ? null : (Date)o;
	}

	@JsonIgnore
	public OConstants getConstants() {
		Object o = fieldContainer.get(CONSTANTS);
		return o == null ? null : (OConstants)o;
	}

	@SuppressWarnings("rawtypes")
	@ObjectDiffProperty(ignore = true)
	@JsonIgnore
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
	public HashSet<OExpressionLanguage> getExpressionLanguages() {
		//TODO conflicts with legacy impl of this method
		return (HashSet<OExpressionLanguage>) fieldContainer
				.get(EXPRESSIONLANGUAGES);
	}

	@JsonIgnore
	public String getGuid() {
		Object o = fieldContainer.get(GUID);
		return o == null ? null : (String)o;
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
		Object o = fieldContainer.get(NAMESPACECONTEXT);
		return o == null ? null : (NSContext)o;
	}

	@JsonIgnore
	public OPartnerLink getPartnerLink(String name) {
		for (OPartnerLink partnerLink : getAllPartnerLinks()) {
			if (partnerLink.getName().equals(name)) {
				return partnerLink;
			}
		}
		return null;
	}

	@JsonIgnore
	public OScope getProcesScope() {
		Object o = fieldContainer.get(PROCESSCOPE);
		return o == null ? null : (OScope)o;
	}

	@JsonIgnore
	public String getProcessName() {
		Object o = fieldContainer.get(PROCESSNAME);
		return o == null ? null : (String)o;
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<OProperty> getProperties() {
		Object o = fieldContainer.get(PROPERTIES);
		return o == null ? null : (List<OProperty>)o;
	}

	@ObjectDiffProperty(ignore = true)
	@JsonIgnore
	public QName getQName() {
		return new QName(getTargetNamespace(), getProcessName());
	}

	public OScope getScope(String scopeName) {
		throw new UnsupportedOperationException();
	}

	@JsonIgnore
	public String getTargetNamespace() {
		Object o = fieldContainer.get(TARGETNAMESPACE);
		return o == null ? null : (String)o;
	}

	@JsonIgnore
	public String getUuid() {
		Object o = fieldContainer.get(UUID);
		return o == null ? null : (String)o;
	}

	@JsonIgnore
	public String getVersion() {
		Object o = fieldContainer.get(VERSION);
		return o == null ? null : (String)o;
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public HashMap<QName, OXsdTypeVarType> getXsdTypes() {
		Object o = fieldContainer.get(XSDTYPES);
		return o == null ? null : (HashMap<QName, OXsdTypeVarType>)o;
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public HashMap<URI, OXslSheet> getXslSheets() {
		Object o = fieldContainer.get(XSLSHEETS);
		return o == null ? null : (HashMap<URI, OXslSheet>)o;
	}
	
	@SuppressWarnings("unchecked")
	@JsonIgnore
	public Set<OExtension> getDeclaredExtensions() {
		return (Set<OExtension>) fieldContainer
				.get(DECLAREDEXTENSIONS);
	}
	
	@SuppressWarnings("unchecked")
	@JsonIgnore
	public Set<OExtension> getMustUnderstandExtensions() {
		return (Set<OExtension>) fieldContainer
				.get(MUSTUNDERSTANDEXTENSIONS);
	}

	public void setAllPartnerLinks(Set<OPartnerLink> allPartnerLinks) {
		if (getAllPartnerLinks() == null) {
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
		if (getElementTypes() == null) {
			fieldContainer.put(ELEMENTTYPES, elementTypes);
		}
	}

	public void setExpressionLanguages(
			HashSet<OExpressionLanguage> expressionLanguages) {
		if (getExpressionLanguages() == null) {
			fieldContainer.put(EXPRESSIONLANGUAGES, expressionLanguages);
		}
	}

	public void setGuid(String guid) {
		fieldContainer.put(GUID, guid);
	}

	public void setMessageTypes(HashMap<QName, OMessageVarType> messageTypes) {
		if (getMessageTypes() == null) {
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
		if (getProperties() == null) {
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
		if (getXsdTypes() == null) {
			fieldContainer.put(XSDTYPES, xsdTypes);
		}
	}

	public void setXslSheets(HashMap<URI, OXslSheet> xslSheets) {
		if (getXslSheets() == null) {
			fieldContainer.put(XSLSHEETS, xslSheets);
		}
	}
	
	public void setDeclaredExtensions(Set<OExtension> extensions) {
		if (getDeclaredExtensions() == null) {
			fieldContainer.put(DECLAREDEXTENSIONS, extensions);
		}
	}
	
	public void setMustUnderstandExtensions(Set<OExtension> extensions) {
		if (getMustUnderstandExtensions() == null) {
			fieldContainer.put(MUSTUNDERSTANDEXTENSIONS, extensions);
		}
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException{
		ois.defaultReadObject();
		fieldContainer.remove(NAMESPACECONTEXT);
	}

	public static class OProperty extends OBase  implements Serializable{
	public static final long serialVersionUID = -1L;

		private static final String ALIASES = "aliases";
		private static final String NAME = "name";
		
		@JsonCreator
		public OProperty(){}
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
			Object o = fieldContainer.get(ALIASES);
		return o == null ? null : (List<OPropertyAlias>)o;
		}

		@JsonIgnore
		public QName getName() {
			Object o = fieldContainer.get(NAME);
		return o == null ? null : (QName)o;
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

	public static class OPropertyAlias extends OBase  implements Serializable{
		public static final long serialVersionUID = -1L;
		/**
		 * Change log of class version
		 * initial 1
		 * current 2
		 * 
		 * 1->2:
		 * 	added header attribute
		 *  */
		public static final int CURRENT_CLASS_VERSION = 2;

		private static final String VARTYPE = "varType";

		/** For BPEL 1.1 */
		private static final String PART = "part";
		private static final String HEADER = "header";
		private static final String LOCATION = "location";

		@JsonCreator
		public OPropertyAlias(){}
		
		public OPropertyAlias(OProcess owner) {
			super(owner);
		}

		@JsonIgnore
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
			Object o = fieldContainer.get(HEADER);
			return o == null ? null : (String)o;
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
		public OVarType getVarType() {
			Object o = fieldContainer.get(VARTYPE);
		return o == null ? null : (OVarType)o;
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
	
	public static class OExtension extends OBase implements Serializable {
		public static final long serialVersionUID = -1L  ;
        
        private static final String NAMESPACE = "namespaceURI";
		private static final String MUSTUNDERSTAND = "mustUnderstand";
        
        @JsonCreator
		public OExtension(){}
        
        public OExtension(OProcess process) { super(process); }
        
        @JsonIgnore
		public String getNamespace() {
			Object o = fieldContainer.get(NAMESPACE);
			return o == null ? null : (String)o;
		}

		@JsonIgnore
		public boolean isMustUnderstand() {
			Object o = fieldContainer.get(MUSTUNDERSTAND);
			return o == null ? false : (Boolean)o;
		}
		
		public void setNamespace(String namespaceURI) {
			fieldContainer.put(NAMESPACE, namespaceURI);
		}

		public void setMustUnderstand(boolean mustUnderstand) {
			fieldContainer.put(MUSTUNDERSTAND, mustUnderstand);
		}

        public String toString() {
            return "{OExtension " + getNamespace() + (isMustUnderstand() ? " mustUnderstand" : "") + "}";
        }
    }

	/**
	 * custom deserializer of OProcess.
	 * @author fangzhen
	 * @deprecated unnecessary now
	 */
	public static class OProcessDeser extends StdDeserializer<OProcess>
			implements ResolvableDeserializer {
		private static final long serialVersionUID = 7750214662590623362L;
		private JsonDeserializer<?> defaultDeserializer;

		public OProcessDeser(JsonDeserializer<?> defaultDeserializer) {
			super(OProcess.class);
			this.defaultDeserializer = defaultDeserializer;
		}

		@Override
		public OProcess deserialize(JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			OProcess process = (OProcess) defaultDeserializer.deserialize(jp,
					ctxt);
			OProcess.instanceCount++;
			return process;
		}

		// for some reason you have to implement ResolvableDeserializer when modifying BeanDeserializer
		// otherwise deserializing throws JsonMappingException??
		@Override
		public void resolve(DeserializationContext ctxt)
				throws JsonMappingException {
			((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
		}
	}
}
