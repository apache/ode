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

import javax.xml.namespace.QName;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Compiled BPEL constants. Mostly the qualified names of the standard
 * faults.
 */
public class OConstants extends OBase {

	private static final String QNMISSINGREQUEST = "qnMissingRequest";
	private static final String QNMISSINGREPLY = "qnMissingReply";
	private static final String QNUNINITIALIZEDVARIABLE = "qnUninitializedVariable";
	private static final String QNCONFLICTINGRECEIVE = "qnConflictingReceive";
	private static final String QNCONFLICTINGREQUEST = "qnConflictingRequest";
	private static final String QNSELECTIONFAILURE = "qnSelectionFailure";
	private static final String QNMISMATCHEDASSIGNMENTFAILURE = "qnMismatchedAssignmentFailure";
	private static final String QNJOINFAILURE = "qnJoinFailure";
	private static final String QNFORCEDTERMINATION = "qnForcedTermination";
	private static final String QNCORRELATIONVIOLATION = "qnCorrelationViolation";
	private static final String QNXSLTINVALIDSOURCE = "qnXsltInvalidSource";
	private static final String QNSUBLANGUAGEEXECUTIONFAULT = "qnSubLanguageExecutionFault";
	private static final String QNUNINITIALIZEDPARTNERROLE = "qnUninitializedPartnerRole";
	private static final String QNFOREACHCOUNTERERROR = "qnForEachCounterError";
	private static final String QNINVALIDBRANCHCONDITION = "qnInvalidBranchCondition";
	private static final String QNINVALIDEXPRESSIONVALUE = "qnInvalidExpressionValue";
	
	// non-standard fault names
	private static final String QNRETIREDPROCESS = "qnRetiredProcess";
	private static final String QNTOOMANYINSTANCES = "qnTooManyInstances";
	private static final String QNTOOMANYPROCESSES = "qnTooManyProcesses";
	private static final String QNTOOHUGEPROCESSES = "qnTooHugeProcesses";
	private static final String QNUNKNOWNFAULT = "qnUnknownFault";

	@JsonCreator
	public OConstants(){}
	public OConstants(OProcess owner) {
		super(owner);
	}

	private String getNS() {
		return getQnMissingRequest().getNamespaceURI();
	}
	public QName qnAmbiguousReceive() {
		return new QName(getNS(), "ambiguousReceive");
	}

	@JsonIgnore
	public QName getQnConflictingReceive() {
		Object o = fieldContainer.get(QNCONFLICTINGRECEIVE);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnConflictingRequest() {
		Object o = fieldContainer.get(QNCONFLICTINGREQUEST);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnCorrelationViolation() {
		Object o = fieldContainer.get(QNCORRELATIONVIOLATION);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnForcedTermination() {
		Object o = fieldContainer.get(QNFORCEDTERMINATION);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnForEachCounterError() {
		Object o = fieldContainer.get(QNFOREACHCOUNTERERROR);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnInvalidBranchCondition() {
		Object o = fieldContainer.get(QNINVALIDBRANCHCONDITION);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnInvalidExpressionValue() {
		Object o = fieldContainer.get(QNINVALIDEXPRESSIONVALUE);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnJoinFailure() {
		Object o = fieldContainer.get(QNJOINFAILURE);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnMismatchedAssignmentFailure() {
		Object o = fieldContainer.get(QNMISMATCHEDASSIGNMENTFAILURE);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnMissingReply() {
		Object o = fieldContainer.get(QNMISSINGREPLY);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnMissingRequest() {
		Object o = fieldContainer.get(QNMISSINGREQUEST);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnRetiredProcess() {
		Object o = fieldContainer.get(QNRETIREDPROCESS);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnSelectionFailure() {
		Object o = fieldContainer.get(QNSELECTIONFAILURE);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnSubLanguageExecutionFault() {
		Object o = fieldContainer.get(QNSUBLANGUAGEEXECUTIONFAULT);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnTooHugeProcesses() {
		Object o = fieldContainer.get(QNTOOHUGEPROCESSES);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnTooManyInstances() {
		Object o = fieldContainer.get(QNTOOMANYINSTANCES);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnTooManyProcesses() {
		Object o = fieldContainer.get(QNTOOMANYPROCESSES);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnUninitializedPartnerRole() {
		Object o = fieldContainer.get(QNUNINITIALIZEDPARTNERROLE);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnUninitializedVariable() {
		Object o = fieldContainer.get(QNUNINITIALIZEDVARIABLE);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnUnknownFault() {
		Object o = fieldContainer.get(QNUNKNOWNFAULT);
		return o == null ? null : (QName)o;
	}

	@JsonIgnore
	public QName getQnXsltInvalidSource() {
		Object o = fieldContainer.get(QNXSLTINVALIDSOURCE);
		return o == null ? null : (QName)o;
	}

	public void setQnConflictingReceive(QName qnConflictingReceive) {
		fieldContainer.put(QNCONFLICTINGRECEIVE, qnConflictingReceive);
	}

	public void setQnConflictingRequest(QName qnConflictingRequest) {
		fieldContainer.put(QNCONFLICTINGREQUEST, qnConflictingRequest);
	}

	public void setQnCorrelationViolation(QName qnCorrelationViolation) {
		fieldContainer.put(QNCORRELATIONVIOLATION, qnCorrelationViolation);
	}

	public void setQnForcedTermination(QName qnForcedTermination) {
		fieldContainer.put(QNFORCEDTERMINATION, qnForcedTermination);
	}

	public void setQnForEachCounterError(QName qnForEachCounterError) {
		fieldContainer.put(QNFOREACHCOUNTERERROR, qnForEachCounterError);
	}

	public void setQnInvalidBranchCondition(QName qnInvalidBranchCondition) {
		fieldContainer.put(QNINVALIDBRANCHCONDITION, qnInvalidBranchCondition);
	}

	public void setQnInvalidExpressionValue(QName qnInvalidExpressionValue) {
		fieldContainer.put(QNINVALIDEXPRESSIONVALUE, qnInvalidExpressionValue);
	}

	public void setQnJoinFailure(QName qnJoinFailure) {
		fieldContainer.put(QNJOINFAILURE, qnJoinFailure);
	}

	public void setQnMismatchedAssignmentFailure(
			QName qnMismatchedAssignmentFailure) {
		fieldContainer.put(QNMISMATCHEDASSIGNMENTFAILURE,
				qnMismatchedAssignmentFailure);
	}

	public void setQnMissingReply(QName qnMissingReply) {
		fieldContainer.put(QNMISSINGREPLY, qnMissingReply);
	}

	public void setQnMissingRequest(QName qnMissingRequest) {
		fieldContainer.put(QNMISSINGREQUEST, qnMissingRequest);
	}

	public void setQnRetiredProcess(QName qnRetiredProcess) {
		fieldContainer.put(QNRETIREDPROCESS, qnRetiredProcess);
	}

	public void setQnSelectionFailure(QName qnSelectionFailure) {
		fieldContainer.put(QNSELECTIONFAILURE, qnSelectionFailure);
	}

	public void setQnSubLanguageExecutionFault(QName qnSubLanguageExecutionFault) {
		fieldContainer.put(QNSUBLANGUAGEEXECUTIONFAULT,
				qnSubLanguageExecutionFault);
	}

	public void setQnTooHugeProcesses(QName qnTooHugeProcesses) {
		fieldContainer.put(QNTOOHUGEPROCESSES, qnTooHugeProcesses);
	}

	public void setQnTooManyInstances(QName qnTooManyInstances) {
		fieldContainer.put(QNTOOMANYINSTANCES, qnTooManyInstances);
	}

	public void setQnTooManyProcesses(QName qnTooManyProcesses) {
		fieldContainer.put(QNTOOMANYPROCESSES, qnTooManyProcesses);
	}

	public void setQnUninitializedPartnerRole(QName qnUninitializedPartnerRole) {
		fieldContainer.put(QNUNINITIALIZEDPARTNERROLE,
				qnUninitializedPartnerRole);
	}

	public void setQnUninitializedVariable(QName qnUninitializedVariable) {
		fieldContainer.put(QNUNINITIALIZEDVARIABLE, qnUninitializedVariable);
	}

	public void setQnUnknownFault(QName qnUnknownFault) {
		fieldContainer.put(QNUNKNOWNFAULT, qnUnknownFault);
	}

	public void setQnXsltInvalidSource(QName qnXsltInvalidSource) {
		fieldContainer.put(QNXSLTINVALIDSOURCE, qnXsltInvalidSource);
	}
}
