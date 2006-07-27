/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.common;

import javax.xml.namespace.QName;


/**
 * <p>Encapsulates an exception that should result in a known named fault being thrown
 * within a BPEL process.</p>
 * <p>As per BPEL specification, appendix A.</p>
 */
public class FaultException extends Exception {
  private static final long serialVersionUID = 389190682205802035L;
  private QName _qname;

  /**
   * Create a new instance.
   * @param qname the <code>QName</code> of the fault
   * @param message a descriptive message for the exception
   */
  public FaultException(QName qname, String message) {
    super(message);
    _qname = qname;
  }

  public FaultException(QName qname) {
    super();
    _qname = qname;
  }

  /**
   * Get the (official) <code>QName</code> of this fault.
   * @return the <code>QName</code> of the fault
   */
  public QName getQName() {
    return _qname;
  }
}
