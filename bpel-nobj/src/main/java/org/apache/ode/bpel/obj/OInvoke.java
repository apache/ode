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

import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Operation;

import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.obj.OScope.CorrelationSet;
import org.apache.ode.bpel.obj.OScope.Variable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Compiled rerpresentation of the BPEL <code>&lt;invoke&gt;</code> activity.
 */
public class OInvoke extends OActivity  implements Serializable{
	public static final long serialVersionUID = -1L;
	private static final String PARTNERLINK = "partnerLink";
	private static final String INPUTVAR = "inputVar";
	private static final String OUTPUTVAR = "outputVar";
	private static final String OPERATION = "operation";

	/** Correlation sets initialized on the input message. */
	private static final String INITCORRELATIONSINPUT = "initCorrelationsInput";

	/** Correlation sets initialized on the output message. */
	private static final String INITCORRELATIONSOUTPUT = "initCorrelationsOutput";

	/** Correlation sets asserted on input. */
	private static final String ASSERTCORRELATIONSINPUT = "assertCorrelationsInput";

	/** Correlation sets asserted on output. */
	private static final String ASSERTCORRELATIONSOUTPUT = "assertCorrelationsOutput";

	/** Correlation sets joined on input. */
	private static final String JOINCORRELATIONSINPUT = "joinCorrelationsInput";

	/** Correlation sets joined on output. */
	private static final String JOINCORRELATIONSOUTPUT = "joinCorrelationsOutput";
	
	@JsonCreator
	public OInvoke(){}
	public OInvoke(OProcess owner, OActivity parent) {
		super(owner, parent);
		setInitCorrelationsInput(new ArrayList<CorrelationSet>());
		setInitCorrelationsOutput(new ArrayList<CorrelationSet>());
		setAssertCorrelationsInput(new ArrayList<CorrelationSet>());
		setAssertCorrelationsOutput(new ArrayList<CorrelationSet>());
		setJoinCorrelationsInput(new ArrayList<CorrelationSet>());
		setJoinCorrelationsOutput(new ArrayList<CorrelationSet>());
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<CorrelationSet> getAssertCorrelationsInput() {
		return (List<CorrelationSet>) fieldContainer
				.get(ASSERTCORRELATIONSINPUT);
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<CorrelationSet> getAssertCorrelationsOutput() {
		return (List<CorrelationSet>) fieldContainer
				.get(ASSERTCORRELATIONSOUTPUT);
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<CorrelationSet> getInitCorrelationsInput() {
		Object o = fieldContainer.get(INITCORRELATIONSINPUT);
		return o == null ? null : (List<CorrelationSet>)o;
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<CorrelationSet> getInitCorrelationsOutput() {
		return (List<CorrelationSet>) fieldContainer
				.get(INITCORRELATIONSOUTPUT);
	}

	@JsonIgnore
	public Variable getInputVar() {
		Object o = fieldContainer.get(INPUTVAR);
		return o == null ? null : (Variable)o;
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<CorrelationSet> getJoinCorrelationsInput() {
		Object o = fieldContainer.get(JOINCORRELATIONSINPUT);
		return o == null ? null : (List<CorrelationSet>)o;
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public List<CorrelationSet> getJoinCorrelationsOutput() {
		return (List<CorrelationSet>) fieldContainer
				.get(JOINCORRELATIONSOUTPUT);
	}

	@JsonIgnore
	public Operation getOperation() {
		Object o = fieldContainer.get(OPERATION);
		return o == null ? null : (Operation)o;
	}

	@JsonIgnore
	public Variable getOutputVar() {
		Object o = fieldContainer.get(OUTPUTVAR);
		return o == null ? null : (Variable)o;
	}

	@JsonIgnore
	public OPartnerLink getPartnerLink() {
		Object o = fieldContainer.get(PARTNERLINK);
		return o == null ? null : (OPartnerLink)o;
	}

	//TODO: custom read Object -- backward compatibility
//	private void readObject(ObjectInputStream in) throws IOException,
//			ClassNotFoundException {
//		in.defaultReadObject();
//
//		// backward compatibility; joinCorrelationInput could be null if read from old definition
//		if (getJoinCorrelationsInput() == null) {
//			try {
//				Field field = getClass().getDeclaredField(
//						"joinCorrelationsInput");
//				field.setAccessible(true);
//				field.set(this, new ArrayList<OScope.CorrelationSet>());
//			} catch (NoSuchFieldException nfe) {
//				throw new IOException(nfe.getMessage());
//			} catch (IllegalAccessException iae) {
//				throw new IOException(iae.getMessage());
//			}
//		}
//		// backward compatibility; joinCorrelationOutput could be null if read from old definition
//		if (getJoinCorrelationsOutput() == null) {
//			try {
//				Field field = getClass().getDeclaredField(
//						"joinCorrelationsOutput");
//				field.setAccessible(true);
//				field.set(this, new ArrayList<CorrelationSet>());
//			} catch (NoSuchFieldException nfe) {
//				throw new IOException(nfe.getMessage());
//			} catch (IllegalAccessException iae) {
//				throw new IOException(iae.getMessage());
//			}
//		}
//	}

	public void setAssertCorrelationsInput(
			List<CorrelationSet> assertCorrelationsInput) {
		if (getAssertCorrelationsInput() == null){
			fieldContainer.put(ASSERTCORRELATIONSINPUT, assertCorrelationsInput);
		}
	}

	public void setAssertCorrelationsOutput(
			List<CorrelationSet> assertCorrelationsOutput) {
		if (getAssertCorrelationsOutput() == null){
			fieldContainer.put(ASSERTCORRELATIONSOUTPUT, assertCorrelationsOutput);
		}
	}

	public void setInitCorrelationsInput(
			List<CorrelationSet> initCorrelationsInput) {
		if (getInitCorrelationsInput() == null){
			fieldContainer.put(INITCORRELATIONSINPUT, initCorrelationsInput);
		}
	}

	public void setInitCorrelationsOutput(
			List<CorrelationSet> initCorrelationsOutput) {
		if (getInitCorrelationsOutput() == null){
			fieldContainer.put(INITCORRELATIONSOUTPUT, initCorrelationsOutput);
		}
	}

	public void setInputVar(Variable inputVar) {
		fieldContainer.put(INPUTVAR, inputVar);
	}

	public void setJoinCorrelationsInput(
			List<CorrelationSet> joinCorrelationsInput) {
		if (getJoinCorrelationsInput() == null){
			fieldContainer.put(JOINCORRELATIONSINPUT, joinCorrelationsInput);
		}
	}

	public void setJoinCorrelationsOutput(
			List<CorrelationSet> joinCorrelationsOutput) {
		if (getJoinCorrelationsOutput() == null){
			fieldContainer.put(JOINCORRELATIONSOUTPUT, joinCorrelationsOutput);
		}
	}

	public void setOperation(Operation operation) {
		fieldContainer.put(OPERATION, operation);
	}

	public void setOutputVar(Variable outputVar) {
		fieldContainer.put(OUTPUTVAR, outputVar);
	}

	public void setPartnerLink(OPartnerLink partnerLink) {
		fieldContainer.put(PARTNERLINK, partnerLink);
	}
}
