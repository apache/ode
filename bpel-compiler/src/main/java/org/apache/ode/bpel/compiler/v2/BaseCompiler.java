package org.apache.ode.bpel.compiler.v2;

import org.apache.ode.bpel.rtrep.v2.OProcess;
import org.apache.ode.bpel.rtrep.v2.OConstants;

import javax.xml.namespace.QName;

/**
 * @author Matthieu Riou <mriou@apache.org>
 */
public abstract class BaseCompiler {

    protected OProcess _oprocess;

    protected OConstants makeConstants() {
        OConstants constants = new OConstants(_oprocess);
        constants.qnConflictingReceive = new QName(getBpwsNamespace(), "conflictingReceive");
        constants.qnConflictingRequest = new QName(getBpwsNamespace(), "conflictingRequest");
        constants.qnCorrelationViolation = new QName(getBpwsNamespace(), "correlationViolation");
        constants.qnForcedTermination = new QName(getBpwsNamespace(), "forcedTermination");
        constants.qnJoinFailure = new QName(getBpwsNamespace(), "joinFailure");
        constants.qnMismatchedAssignmentFailure = new QName(getBpwsNamespace(), "mismatchedAssignment");
        constants.qnMissingReply = new QName(getBpwsNamespace(), "missingReply");
        constants.qnMissingRequest = new QName(getBpwsNamespace(), "missingRequest");
        constants.qnSelectionFailure = new QName(getBpwsNamespace(), "selectionFailure");
        constants.qnUninitializedVariable = new QName(getBpwsNamespace(), "uninitializedVariable");
        constants.qnXsltInvalidSource = new QName(getBpwsNamespace(), "xsltInvalidSource");
        constants.qnSubLanguageExecutionFault = new QName(getBpwsNamespace(), "subLanguageExecutionFault");
        constants.qnUninitializedPartnerRole = new QName(getBpwsNamespace(), "uninitializedPartnerRole");
        constants.qnForEachCounterError = new QName(getBpwsNamespace(), "forEachCounterError");
        constants.qnInvalidBranchCondition = new QName(getBpwsNamespace(), "invalidBranchCondition");
        constants.qnInvalidExpressionValue = new QName(getBpwsNamespace(), "invalidExpressionValue");
        constants.qnScopeRollback = new QName(getOdeNamespace(), "scopeRollback");
        
        constants.qnDuplicateInstance = new QName(getOdeNamespace(), "duplicateInstance");
        constants.qnRetiredProcess = new QName(getOdeNamespace(), "retiredProcess");
        constants.qnUnknownFault = new QName(getOdeNamespace(), "unknownFault");
        return constants;
    }

    protected abstract String getBpwsNamespace();
    
    protected String getOdeNamespace() {
    	return "ode";
    }

}
