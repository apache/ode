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

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Operation;

/**
 * Compiled represenation of a BPEL event handler.
 */
public class OEventHandler extends OAgent {
	private static final String ONMESSAGES = "onMessages";
	private static final String ONALARMS = "onAlarms";

	public OEventHandler(OProcess owner) {
		super(owner);
		setOnMessages(new ArrayList<OEvent>());
		setOnAlarms(new ArrayList<OAlarm>());
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<OAlarm> getOnAlarms() {
		return (List<OAlarm>) fieldContainer.get(ONALARMS);
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<OEvent> getOnMessages() {
		return (List<OEvent>) fieldContainer.get(ONMESSAGES);
	}

	public void setOnAlarms(List<OAlarm> onAlarms) {
		fieldContainer.put(ONALARMS, onAlarms);
	}

	public void setOnMessages(List<OEvent> onMessages) {
		fieldContainer.put(ONMESSAGES, onMessages);
	}

	public static class OAlarm extends OAgent {
		private static final String FOREXPR = "forExpr";
		private static final String UNTILEXPR = "untilExpr";
		private static final String REPEATEXPR = "repeatExpr";
		private static final String ACTIVITY = "activity";

		public OAlarm(OProcess owner) {
			super(owner);
		}

		@JsonIgnore
		public OActivity getActivity() {
			return (OActivity) fieldContainer.get(ACTIVITY);
		}

		@JsonIgnore
		public OExpression getForExpr() {
			return (OExpression) fieldContainer.get(FOREXPR);
		}

		@JsonIgnore
		public OExpression getRepeatExpr() {
			return (OExpression) fieldContainer.get(REPEATEXPR);
		}

		@JsonIgnore
		public OExpression getUntilExpr() {
			return (OExpression) fieldContainer.get(UNTILEXPR);
		}

		public void setActivity(OActivity activity) {
			fieldContainer.put(ACTIVITY, activity);
		}

		public void setForExpr(OExpression forExpr) {
			fieldContainer.put(FOREXPR, forExpr);
		}

		public void setRepeatExpr(OExpression repeatExpr) {
			fieldContainer.put(REPEATEXPR, repeatExpr);
		}

		public void setUntilExpr(OExpression untilExpr) {
			fieldContainer.put(UNTILEXPR, untilExpr);
		}
	}

	public static class OEvent extends OScope {

		/** Correlations to initialize. */
		private static final String INITCORRELATIONS = "initCorrelations";

		/** Correlation set to match on. */
		private static final String MATCHCORRELATIONS = "matchCorrelations";

		/** Correlation set to join on. */
		private static final String JOINCORRELATIONS = "joinCorrelations";

		private static final String PARTNERLINK = "partnerLink";

		private static final String OPERATION = "operation";

		private static final String VARIABLE = "variable";

		/** OASIS addition for disambiguating receives (optional). */

		private static final String MESSAGEEXCHANGEID = "messageExchangeId";

		private static final String ROUTE = "route";

		public OEvent(OProcess owner, OActivity parent) {
			super(owner, parent);
			setInitCorrelations(new ArrayList<CorrelationSet>());
			setMatchCorrelations(new ArrayList<CorrelationSet>());
			setJoinCorrelations(new ArrayList<CorrelationSet>());
			setMessageExchangeId("");
			setRoute("one");
		}

		public String getCorrelatorId() {
			return getPartnerLink().getId() + "." + getOperation().getName();
		}

		@SuppressWarnings("unchecked")
		@JsonIgnore
		public List<CorrelationSet> getInitCorrelations() {
			return (List<CorrelationSet>) fieldContainer.get(INITCORRELATIONS);
		}

		@SuppressWarnings("unchecked")
		@JsonIgnore
		public List<CorrelationSet> getJoinCorrelations() {
			return (List<CorrelationSet>) fieldContainer.get(JOINCORRELATIONS);
		}

		@SuppressWarnings("unchecked")
		@JsonIgnore
		public List<CorrelationSet> getMatchCorrelations() {
			return (List<CorrelationSet>) fieldContainer.get(MATCHCORRELATIONS);
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
		public String getRoute() {
			return (String) fieldContainer.get(ROUTE);
		}

		@JsonIgnore
		public Variable getVariable() {
			return (Variable) fieldContainer.get(VARIABLE);
		}

		public void setInitCorrelations(List<CorrelationSet> initCorrelations) {
			fieldContainer.put(INITCORRELATIONS, initCorrelations);
		}

		public void setJoinCorrelations(List<CorrelationSet> joinCorrelations) {
			fieldContainer.put(JOINCORRELATIONS, joinCorrelations);
		}

		public void setMatchCorrelations(List<CorrelationSet> matchCorrelations) {
			fieldContainer.put(MATCHCORRELATIONS, matchCorrelations);
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
		
		//TODO: custom read object; private fields. backward compatibility;
	}
}