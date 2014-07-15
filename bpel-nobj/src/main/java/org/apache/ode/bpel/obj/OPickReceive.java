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

import org.apache.ode.bpel.obj.OScope.CorrelationSet;
import org.apache.ode.bpel.obj.OScope.Variable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Compiled representation of the BPEL <code>&lt;pick&gt;</code> and
 * <codE>&lt;receive&gt;</code> activities. Because the latter is essentially
 * a simplified version of the former, at run-time we do not distinguish
 * between the two.
 */
public class OPickReceive extends OActivity {
	private static final String ONMESSAGES = "onMessages";
	private static final String ONALARMS = "onAlarms";
	private static final String CREATEINSTANCEFLAG = "createInstanceFlag";

	@JsonCreator
	public OPickReceive(){
		setCreateInstanceFlag(false);
	}
	public OPickReceive(OProcess owner, OActivity parent) {
		super(owner, parent);
		setOnMessages(new ArrayList<OnMessage>());
		setOnAlarms(new ArrayList<OnAlarm>());
		setCreateInstanceFlag(false);
	}

	@JsonIgnore
	public boolean isCreateInstanceFlag() {
		Object o = fieldContainer.get(CREATEINSTANCEFLAG);
		return o == null ? null : (Boolean)o;
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<OnAlarm> getOnAlarms() {
		Object o = fieldContainer.get(ONALARMS);
		return o == null ? null : (List<OnAlarm>)o;
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<OnMessage> getOnMessages() {
		Object o = fieldContainer.get(ONMESSAGES);
		return o == null ? null : (List<OnMessage>)o;
	}

	public void setCreateInstanceFlag(boolean createInstanceFlag) {
		fieldContainer.put(CREATEINSTANCEFLAG, createInstanceFlag);
	}

	public void setOnAlarms(List<OnAlarm> onAlarms) {
		fieldContainer.put(ONALARMS, onAlarms);
	}

	public void setOnMessages(List<OnMessage> onMessages) {
		fieldContainer.put(ONMESSAGES, onMessages);
	}

	public static class OnAlarm extends OBase {
		private static final String ACTIVITY = "activity";
		private static final String FOREXPR = "forExpr";
		private static final String UNTILEXPR = "untilExpr";

		@JsonCreator
		public OnAlarm(){}
		
		public OnAlarm(OProcess owner) {
			super(owner);
		}

		@JsonIgnore
		public OActivity getActivity() {
			Object o = fieldContainer.get(ACTIVITY);
		return o == null ? null : (OActivity)o;
		}

		@JsonIgnore
		public OExpression getForExpr() {
			Object o = fieldContainer.get(FOREXPR);
		return o == null ? null : (OExpression)o;
		}

		@JsonIgnore
		public OExpression getUntilExpr() {
			Object o = fieldContainer.get(UNTILEXPR);
		return o == null ? null : (OExpression)o;
		}

		public void setActivity(OActivity activity) {
			fieldContainer.put(ACTIVITY, activity);
		}

		public void setForExpr(OExpression forExpr) {
			fieldContainer.put(FOREXPR, forExpr);
		}

		public void setUntilExpr(OExpression untilExpr) {
			fieldContainer.put(UNTILEXPR, untilExpr);
		}
	}

	public static class OnMessage extends OBase {

		/** Correlations to initialize. */
		private static final String INITCORRELATIONS = "initCorrelations";
		/** Correlations to match on. */
		private static final String MATCHCORRELATIONS = "matchCorrelations";

		/** Correlations to join on. */
		private static final String JOINCORRELATIONS = "joinCorrelations";
		private static final String PARTNERLINK = "partnerLink";
		private static final String OPERATION = "operation";
		private static final String VARIABLE = "variable";
		private static final String ACTIVITY = "activity";
		/** OASIS addition for disambiguating receives (optional). */
		private static final String MESSAGEEXCHANGEID = "messageExchangeId";
		private static final String ROUTE = "route";

		@JsonCreator
		public OnMessage(){
			
		}
		public OnMessage(OProcess owner) {
			super(owner);
			setInitCorrelations(new ArrayList<CorrelationSet>());
			setJoinCorrelations(new ArrayList<CorrelationSet>());
			setMatchCorrelations(new ArrayList<CorrelationSet>());
			setRoute("one");
			setMessageExchangeId("");
		}

		@JsonIgnore
		public OActivity getActivity() {
			Object o = fieldContainer.get(ACTIVITY);
		return o == null ? null : (OActivity)o;
		}

		public String getCorrelatorId() {
			return getPartnerLink().getId() + "." + getOperation().getName();
		}

		@SuppressWarnings("unchecked")
		@JsonIgnore
		public List<CorrelationSet> getInitCorrelations() {
			Object o = fieldContainer.get(INITCORRELATIONS);
		return o == null ? null : (List<CorrelationSet>)o;
		}

		@SuppressWarnings("unchecked")
		@JsonIgnore
		public List<CorrelationSet> getJoinCorrelations() {
			Object o = fieldContainer.get(JOINCORRELATIONS);
		return o == null ? null : (List<CorrelationSet>)o;
		}

		@SuppressWarnings("unchecked")
		@JsonIgnore
		public List<CorrelationSet> getMatchCorrelations() {
			Object o = fieldContainer.get(MATCHCORRELATIONS);
		return o == null ? null : (List<CorrelationSet>)o;
		}

		@JsonIgnore
		public String getMessageExchangeId() {
			Object o = fieldContainer.get(MESSAGEEXCHANGEID);
		return o == null ? null : (String)o;
		}

		@JsonIgnore
		public Operation getOperation() {
			Object o = fieldContainer.get(OPERATION);
		return o == null ? null : (Operation)o;
		}

		@JsonIgnore
		public OPartnerLink getPartnerLink() {
			Object o = fieldContainer.get(PARTNERLINK);
		return o == null ? null : (OPartnerLink)o;
		}

		@JsonIgnore
		public String getRoute() {
			Object o = fieldContainer.get(ROUTE);
		return o == null ? null : (String)o;
		}

		@JsonIgnore
		public Variable getVariable() {
			Object o = fieldContainer.get(VARIABLE);
		return o == null ? null : (Variable)o;
		}

		public void setActivity(OActivity activity) {
			fieldContainer.put(ACTIVITY, activity);
		}

		public void setInitCorrelations(List<CorrelationSet> initCorrelations) {
			if (getInitCorrelations() == null){
				fieldContainer.put(INITCORRELATIONS, initCorrelations);
			}
		}

		public void setJoinCorrelations(List<CorrelationSet> joinCorrelations) {
			if (getJoinCorrelations() == null){
				fieldContainer.put(JOINCORRELATIONS, joinCorrelations);
			}
		}

		public void setMatchCorrelations(List<CorrelationSet> matchCorrelations) {
			if (getMatchCorrelations() == null){
				fieldContainer.put(MATCHCORRELATIONS, matchCorrelations);
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

		public void setRoute(String route) {
			fieldContainer.put(ROUTE, route);
		}

		public void setVariable(Variable variable) {
			fieldContainer.put(VARIABLE, variable);
		}
		//TODO: custom readObject -- backward compatibility
	}
}