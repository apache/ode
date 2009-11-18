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
package org.apache.ode.bpel.o;

import javax.xml.namespace.QName;

/**
 * Compiled BPEL constants. Mostly the qualified names of the standard
 * faults.
 */
public class OConstants extends OBase {

    private static final long serialVersionUID = 1L;
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

    // non-standard fault names
    public QName qnRetiredProcess;
    public QName qnTooManyInstances;
    public QName qnTooManyProcesses;
    public QName qnTooHugeProcesses;
    public QName qnUnknownFault;

    public OConstants(OProcess owner) {
        super(owner);
    }

}
