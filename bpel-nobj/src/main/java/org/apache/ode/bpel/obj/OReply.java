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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.obj.OScope.CorrelationSet;
import org.apache.ode.bpel.obj.OScope.Variable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Compiled representation of the BPEL <code>&lt;reply&gt;</code> activity.
 */
public class OReply extends OActivity {

	/** Is this a Fault reply? */
	private static final String ISFAULTREPLY = "isFaultReply";

	/** The type of the fault (if isFaultReply). */
	private static final String FAULT = "fault";

	private static final String PARTNERLINK = "partnerLink";
	private static final String OPERATION = "operation";
	private static final String VARIABLE = "variable";
	/** Correlation sets initialized. */
	private static final String INITCORRELATIONS = "initCorrelations";

	/** Correlation sets asserted. */
	private static final String ASSERTCORRELATIONS = "assertCorrelations";

	/** Correlation sets joined. */
	private static final String JOINCORRELATIONS = "joinCorrelations";

	/** OASIS modification - Message Exchange Id. */
	private static final String MESSAGEEXCHANGEID = "messageExchangeId";

	public OReply(OProcess owner, OActivity parent) {
		super(owner, parent);
		setInitCorrelations(new ArrayList<CorrelationSet>());
		setJoinCorrelations(new ArrayList<CorrelationSet>());
		setAssertCorrelations(new ArrayList<CorrelationSet>());
		setMessageExchangeId("");
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<CorrelationSet> getAssertCorrelations() {
		return (List<CorrelationSet>) fieldContainer.get(ASSERTCORRELATIONS);
	}

	@JsonIgnore
	public QName getFault() {
		return (QName) fieldContainer.get(FAULT);
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<CorrelationSet> getInitCorrelations() {
		return (List<CorrelationSet>) fieldContainer.get(INITCORRELATIONS);
	}

	@JsonIgnore
	public boolean isIsFaultReply() {
		return (Boolean) fieldContainer.get(ISFAULTREPLY);
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<CorrelationSet> getJoinCorrelations() {
		return (List<CorrelationSet>) fieldContainer.get(JOINCORRELATIONS);
	}

	@JsonIgnore
	public String getMessageExchangeId() {
		return (String) fieldContainer.get(MESSAGEEXCHANGEID);
	}

	@JsonIgnore
	public Operation getOperation() {
		return (Operation) fieldContainer.get(OPERATION);
	}

	@JsonIgnore
	public OPartnerLink getPartnerLink() {
		return (OPartnerLink) fieldContainer.get(PARTNERLINK);
	}

	@JsonIgnore
	public Variable getVariable() {
		return (Variable) fieldContainer.get(VARIABLE);
	}

	public void setAssertCorrelations(List<CorrelationSet> assertCorrelations) {
		if (getAssertCorrelations() == null){
			fieldContainer.put(ASSERTCORRELATIONS, assertCorrelations);
		}
	}

	public void setFault(QName fault) {
		fieldContainer.put(FAULT, fault);
	}

	public void setInitCorrelations(List<CorrelationSet> initCorrelations) {
		if (getInitCorrelations() == null){
			fieldContainer.put(INITCORRELATIONS, initCorrelations);
		}
	}

	public void setIsFaultReply(boolean isFaultReply) {
		fieldContainer.put(ISFAULTREPLY, isFaultReply);
	}

	public void setJoinCorrelations(List<CorrelationSet> joinCorrelations) {
		if (getJoinCorrelations() == null){
			fieldContainer.put(JOINCORRELATIONS, joinCorrelations);
		}
	}

	public void setMessageExchangeId(String messageExchangeId) {
		fieldContainer.put(MESSAGEEXCHANGEID, messageExchangeId);
	}

	public void setOperation(Operation operation) {
		fieldContainer.put(OPERATION, operation);
	}

	public void setPartnerLink(OPartnerLink partnerLink) {
		fieldContainer.put(PARTNERLINK, partnerLink);
	}

	public void setVariable(Variable variable) {
		fieldContainer.put(VARIABLE, variable);
	}
	//TODO: custom readObject -- backward compatibility
}
