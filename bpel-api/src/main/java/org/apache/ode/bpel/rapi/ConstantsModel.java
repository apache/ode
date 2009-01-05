package org.apache.ode.bpel.rapi;

import javax.xml.namespace.QName;

public interface ConstantsModel {
	
    // standard fault names
    public QName getMissingRequest();
    public QName getMissingReply();
    public QName getUninitializedVariable();
    public QName getConflictingReceive();
    public QName getSelectionFailure();
    public QName getMismatchedAssignmentFailure();
    public QName getJoinFailure();
    public QName getForcedTermination();
    public QName getCorrelationViolation();
    public QName getXsltInvalidSource();
    public QName getSubLanguageExecutionFault();
    public QName getUninitializedPartnerRole();
    public QName getForEachCounterError();
    public QName getInvalidBranchCondition();
    public QName getInvalidExpressionValue();

    // non-standard fault names
    public QName getRetiredProcess();
    public QName getDuplicateInstance();
    public QName getUnknownFault();

}
