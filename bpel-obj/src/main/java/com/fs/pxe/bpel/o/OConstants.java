/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.o;

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


  public OConstants(OProcess owner) {
    super(owner);
  }

}
