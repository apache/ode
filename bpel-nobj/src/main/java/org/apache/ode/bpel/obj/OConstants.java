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
		return (QName) fieldContainer.get(QNCONFLICTINGRECEIVE);
	}

	@JsonIgnore
	public QName getQnConflictingRequest() {
		return (QName) fieldContainer.get(QNCONFLICTINGREQUEST);
	}

	@JsonIgnore
	public QName getQnCorrelationViolation() {
		return (QName) fieldContainer.get(QNCORRELATIONVIOLATION);
	}

	@JsonIgnore
	public QName getQnForcedTermination() {
		return (QName) fieldContainer.get(QNFORCEDTERMINATION);
	}

	@JsonIgnore
	public QName getQnForEachCounterError() {
		return (QName) fieldContainer.get(QNFOREACHCOUNTERERROR);
	}

	@JsonIgnore
	public QName getQnInvalidBranchCondition() {
		return (QName) fieldContainer.get(QNINVALIDBRANCHCONDITION);
	}

	@JsonIgnore
	public QName getQnInvalidExpressionValue() {
		return (QName) fieldContainer.get(QNINVALIDEXPRESSIONVALUE);
	}

	@JsonIgnore
	public QName getQnJoinFailure() {
		return (QName) fieldContainer.get(QNJOINFAILURE);
	}

	@JsonIgnore
	public QName getQnMismatchedAssignmentFailure() {
		return (QName) fieldContainer.get(QNMISMATCHEDASSIGNMENTFAILURE);
	}

	@JsonIgnore
	public QName getQnMissingReply() {
		return (QName) fieldContainer.get(QNMISSINGREPLY);
	}

	@JsonIgnore
	public QName getQnMissingRequest() {
		return (QName) fieldContainer.get(QNMISSINGREQUEST);
	}

	@JsonIgnore
	public QName getQnRetiredProcess() {
		return (QName) fieldContainer.get(QNRETIREDPROCESS);
	}

	@JsonIgnore
	public QName getQnSelectionFailure() {
		return (QName) fieldContainer.get(QNSELECTIONFAILURE);
	}

	@JsonIgnore
	public QName getQnSubLanguageExecutionFault() {
		return (QName) fieldContainer.get(QNSUBLANGUAGEEXECUTIONFAULT);
	}

	@JsonIgnore
	public QName getQnTooHugeProcesses() {
		return (QName) fieldContainer.get(QNTOOHUGEPROCESSES);
	}

	@JsonIgnore
	public QName getQnTooManyInstances() {
		return (QName) fieldContainer.get(QNTOOMANYINSTANCES);
	}

	@JsonIgnore
	public QName getQnTooManyProcesses() {
		return (QName) fieldContainer.get(QNTOOMANYPROCESSES);
	}

	@JsonIgnore
	public QName getQnUninitializedPartnerRole() {
		return (QName) fieldContainer.get(QNUNINITIALIZEDPARTNERROLE);
	}

	@JsonIgnore
	public QName getQnUninitializedVariable() {
		return (QName) fieldContainer.get(QNUNINITIALIZEDVARIABLE);
	}

	@JsonIgnore
	public QName getQnUnknownFault() {
		return (QName) fieldContainer.get(QNUNKNOWNFAULT);
	}

	@JsonIgnore
	public QName getQnXsltInvalidSource() {
		return (QName) fieldContainer.get(QNXSLTINVALIDSOURCE);
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
