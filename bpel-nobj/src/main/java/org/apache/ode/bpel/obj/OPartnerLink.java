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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.obj.OScope.CorrelationSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Compiled representation of a BPEL partnerLink.
 */
public class OPartnerLink extends OBase {
	/** partnerLink name. */
	private static final String NAME = "name";

	/** Scope in which this partnerLink is declared. */
	private static final String DECLARINGSCOPE = "declaringScope";

	/** The type of this partnerLink. */
	private static final String PARTNERLINKTYPE = "partnerLinkType";

	private static final String PARTNERROLENAME = "partnerRoleName";
	private static final String MYROLENAME = "myRoleName";
	private static final String MYROLEPORTTYPE = "myRolePortType";
	private static final String PARTNERROLEPORTTYPE = "partnerRolePortType";
	private static final String INITIALIZEPARTNERROLE = "initializePartnerRole";

	/** The set of CorrelationSets that may be used as a match criteria, organized by {@link Operation} */
	private static final String NONINITIATINGCORRELATIONSETS = "nonIntitiatingCorrelationSets";
	/** The set of joining CorrelationSets that may be used as a match criteria, organized by {@link Operation} */
	private static final String JOININGCORRELATIONSETS = "joiningCorrelationSets";
	/** The set of {@link Operation}s that can be used to create a process instance. */
	private static final String CREATEINSTANCEOPERATIONS = "createInstanceOperations";

	@JsonCreator
	public OPartnerLink(){
		setInitializePartnerRole(false);
	}
	public OPartnerLink(OProcess owner) {
		super(owner);
		setNonIntitiatingCorrelationSets(new HashMap<String, Set<CorrelationSet>>());
		setJoiningCorrelationSets(new HashMap<String, Set<CorrelationSet>>());
		setCreateInstanceOperations(new HashSet<String>());

		setInitializePartnerRole(false);
	}

	/**
	 * Add a {@link org.apache.ode.bpel.obj.OScope.CorrelationSet} to an {@link Operation}'s list
	 * of "non-initiating" correlation sets. The non-initiating correlation sets are those
	 * sets that are used (along with the operation) to "match" incoming messages.
	 * We need to know which correlation sets are used with which operation in order to
	 * pre-compute correlation keys at the time of message receipt.
	 * @param operation WSDL {@link Operation}
	 * @param cset non-initiating correlation used in this operation
	 */
	public void addCorrelationSetForOperation(Operation operation,
			OScope.CorrelationSet cset, boolean isJoin) {
		if (!isJoin) {
			Set<OScope.CorrelationSet> ret = getNonIntitiatingCorrelationSets()
					.get(operation.getName());
			if (ret == null) {
				ret = new HashSet<OScope.CorrelationSet>();
				getNonIntitiatingCorrelationSets()
						.put(operation.getName(), ret);
			}
			ret.add(cset);
		} else {
			// serialization backward compatibility; joiningCorrelationSets could be null if read from old definition
			if (getJoiningCorrelationSets() == null) {
				setJoiningCorrelationSets(new java.util.HashMap<java.lang.String, java.util.Set<org.apache.ode.bpel.obj.OScope.CorrelationSet>>());
			}
			Set<OScope.CorrelationSet> ret = getJoiningCorrelationSets().get(
					operation.getName());
			if (ret == null) {
				ret = new HashSet<OScope.CorrelationSet>();
				getJoiningCorrelationSets().put(operation.getName(), ret);
			}
			ret.add(cset);
		}
	}

	public void addCreateInstanceOperation(Operation operation) {
		getCreateInstanceOperations().add(operation.getName());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof OPartnerLink))
			return false;
		OPartnerLink other = (OPartnerLink) obj;
		return (getName() == null && other.getName() == null && super.equals(obj))
				|| getName().equals(other.getName());
	}

	@JsonIgnore
	public OScope getDeclaringScope() {
		Object o = fieldContainer.get(DECLARINGSCOPE);
		return o == null ? null : (OScope)o;
	}

	@JsonIgnore
	public boolean isInitializePartnerRole() {
		Object o = fieldContainer.get(INITIALIZEPARTNERROLE);
		return o == null ? false : (Boolean)o;
	}

	/**
	 * Get all joining correlation sets that are ever used to qualify a receive for a the given
	 * operation.
	 * @param operation the operation
	 * @return all non-initiating correlation sets used in the given operation
	 */
	@SuppressWarnings("unchecked")
	public Set<OScope.CorrelationSet> getJoinningCorrelationSetsForOperation(
			Operation operation) {
		if (getJoiningCorrelationSets() == null)
			return new HashSet<CorrelationSet>();

		Set<OScope.CorrelationSet> ret = getJoiningCorrelationSets().get(
				operation.getName());
		if (ret == null) {
			return Collections.EMPTY_SET;
		}
		return Collections.unmodifiableSet(ret);
	}

	@JsonIgnore
	public String getMyRoleName() {
		Object o = fieldContainer.get(MYROLENAME);
		return o == null ? null : (String)o;
	}

	@JsonIgnore
	@SuppressWarnings("unchecked")
	public Operation getMyRoleOperation(String name) {
		for (Operation op : (List<Operation>) getMyRolePortType()
				.getOperations())
			if (op.getName().equals(name))
				return op;
		return null;
	}

	@JsonIgnore
	public PortType getMyRolePortType() {
		Object o = fieldContainer.get(MYROLEPORTTYPE);
		return o == null ? null : (PortType)o;
	}

	@JsonIgnore
	public String getName() {
		Object o = fieldContainer.get(NAME);
		return o == null ? null : (String)o;
	}

	/**
	 * Get all non-initiating correlation sets that are ever used to qualify a receive for a the given
	 * operation.
	 * @param operation the operation
	 * @return all non-initiating correlation sets used in the given operation
	 */
	@SuppressWarnings("unchecked")
	public Set<OScope.CorrelationSet> getNonInitiatingCorrelationSetsForOperation(
			Operation operation) {
		Set<OScope.CorrelationSet> ret = getNonIntitiatingCorrelationSets()
				.get(operation.getName());
		if (ret == null) {
			return Collections.EMPTY_SET;
		}
		return Collections.unmodifiableSet(ret);
	}

	@JsonIgnore
	public QName getPartnerLinkType() {
		Object o = fieldContainer.get(PARTNERLINKTYPE);
		return o == null ? null : (QName)o;
	}
	
	@JsonIgnore
	public String getPartnerRoleName() {
		Object o = fieldContainer.get(PARTNERROLENAME);
		return o == null ? null : (String)o;
	}

	@SuppressWarnings("unchecked")
	public Operation getPartnerRoleOperation(String name) {
		for (Operation op : (List<Operation>) getPartnerRolePortType()
				.getOperations())
			if (op.getName().equals(name))
				return op;
		return null;
	}

	@JsonIgnore
	public PortType getPartnerRolePortType() {
		Object o = fieldContainer.get(PARTNERROLEPORTTYPE);
		return o == null ? null : (PortType)o;
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public HashSet<String> getCreateInstanceOperations() {
		return (HashSet<String>)fieldContainer.get(CREATEINSTANCEOPERATIONS);
	}
	
	@SuppressWarnings("unchecked")
	@JsonIgnore
	public HashMap<String,Set<CorrelationSet>> getJoiningCorrelationSets(){
		Object o = fieldContainer.get(JOININGCORRELATIONSETS);
		return o == null ? null : (HashMap<String,Set<CorrelationSet>>)o;
	}
	
	@SuppressWarnings("unchecked")
	@JsonIgnore
	public HashMap<String,Set<CorrelationSet>> getNonIntitiatingCorrelationSets(){
		Object o = fieldContainer.get(NONINITIATINGCORRELATIONSETS);
		return o == null ? null : (HashMap<String,Set<CorrelationSet>>)o;
	}
	
	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	public boolean hasMyRole() {
		return getMyRolePortType() != null;
	}

	public boolean hasPartnerRole() {
		return getPartnerRolePortType() != null;
	}

	public boolean isCreateInstanceOperation(Operation op) {
		return getCreateInstanceOperations().contains(op.getName());
	}

	public void setDeclaringScope(OScope declaringScope) {
		fieldContainer.put(DECLARINGSCOPE, declaringScope);
	}

	public void setInitializePartnerRole(boolean initializePartnerRole) {
		fieldContainer.put(INITIALIZEPARTNERROLE, initializePartnerRole);
	}

	public void setMyRoleName(String myRoleName) {
		fieldContainer.put(MYROLENAME, myRoleName);
	}

	public void setMyRolePortType(PortType myRolePortType) {
		fieldContainer.put(MYROLEPORTTYPE, myRolePortType);
	}

	public void setName(String name) {
		fieldContainer.put(NAME, name);
	}

	public void setPartnerLinkType(QName partnerLinkType) {
		fieldContainer.put(PARTNERLINKTYPE, partnerLinkType);
	}

	public void setPartnerRoleName(String partnerRoleName) {
		fieldContainer.put(PARTNERROLENAME, partnerRoleName);
	}

	public void setPartnerRolePortType(PortType partnerRolePortType) {
		fieldContainer.put(PARTNERROLEPORTTYPE, partnerRolePortType);
	}
	
	private void setJoiningCorrelationSets(
			HashMap<String, Set<OScope.CorrelationSet>> joiningCorrelationSets) {
		fieldContainer.put(JOININGCORRELATIONSETS, joiningCorrelationSets);
	}
	private void setNonIntitiatingCorrelationSets(HashMap<String, Set<CorrelationSet>> nics){
		fieldContainer.put(NONINITIATINGCORRELATIONSETS, nics);
	}
	private void setCreateInstanceOperations(HashSet<String> cio){
		fieldContainer.put(CREATEINSTANCEOPERATIONS, cio);
	}
}
