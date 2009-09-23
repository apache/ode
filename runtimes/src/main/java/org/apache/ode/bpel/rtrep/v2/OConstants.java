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
package org.apache.ode.bpel.rtrep.v2;

import org.apache.ode.bpel.rapi.ConstantsModel;

import javax.xml.namespace.QName;

/**
 * Compiled BPEL constants. Mostly the qualified names of the standard
 * faults.
 */
public class OConstants extends OBase implements ConstantsModel {

    private static final long serialVersionUID = 1L;
    
    // standard fault names
    public QName qnMissingRequest;
    public QName qnMissingReply;
    public QName qnUninitializedVariable;
    public QName qnConflictingReceive;
    public QName qnConflictingRequest;
    public QName qnSelectionFailure;
    public QName qnMismatchedAssignmentFailure;
    public QName qnJoinFailure;
    public QName qnForcedTermination;
    public QName qnCorrelationViolation;
    public QName qnXsltInvalidSource;
    public QName qnSubLanguageExecutionFault;
    public QName qnUninitializedPartnerRole;
    public QName qnForEachCounterError;
    public QName qnInvalidBranchCondition;
    public QName qnInvalidExpressionValue;
    public QName qnScopeRollback;

    // non-standard fault names
    public QName qnRetiredProcess;
    public QName qnDuplicateInstance;
    public QName qnUnknownFault;

    public OConstants(OProcess owner) {
        super(owner);
    }

    public QName getConflictingReceive() {
        return qnConflictingReceive;
    }
    
    public QName getConflictingRequest() {
        return qnConflictingRequest;
    }

    public QName getCorrelationViolation() {
        return qnCorrelationViolation;
    }

    public QName getDuplicateInstance() {
        return qnDuplicateInstance;
    }

    public QName getForEachCounterError() {
        return qnForEachCounterError;
    }

    public QName getForcedTermination() {
        return qnForcedTermination;
    }

    public QName getInvalidBranchCondition() {
        return qnInvalidBranchCondition;
    }

    public QName getInvalidExpressionValue() {
        return qnInvalidExpressionValue;
    }

    public QName getJoinFailure() {
        return qnJoinFailure;
    }

    public QName getMismatchedAssignmentFailure() {
        return qnMismatchedAssignmentFailure;
    }

    public QName getMissingReply() {
        return qnMissingReply;
    }

    public QName getMissingRequest() {
        return qnMissingRequest;
    }

    public QName getRetiredProcess() {
        return qnRetiredProcess;
    }

    public QName getSelectionFailure() {
        return qnSelectionFailure;
    }

    public QName getSubLanguageExecutionFault() {
        return qnSubLanguageExecutionFault;
    }

    public QName getUninitializedPartnerRole() {
        return qnUninitializedPartnerRole;
    }

    public QName getUninitializedVariable() {
        return qnUninitializedVariable;
    }

    public QName getUnknownFault() {
        return qnUnknownFault;
    }

    public QName getXsltInvalidSource() {
        return qnXsltInvalidSource;
    }
    
}
