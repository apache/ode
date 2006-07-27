/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.o;

import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Operation;

/**
 * Compiled rerpresentation of the BPEL <code>&lt;invoke&gt;</code> activity.
 */
public class OInvoke extends OActivity {
  
  static final long serialVersionUID = -1L  ;
  public OPartnerLink partnerLink;
  public OScope.Variable inputVar;
  public OScope.Variable outputVar;
  public Operation operation;

  /** Correlation sets initialized on the input message. */
  public final List<OScope.CorrelationSet> initCorrelationsInput = new ArrayList<OScope.CorrelationSet>();

  /** Correlation sets initialized on the input message. */
  public final List <OScope.CorrelationSet> initCorrelationsOutput = new ArrayList<OScope.CorrelationSet>();

  /** Correlation sets asserted on input. */
  public final List <OScope.CorrelationSet> assertCorrelationsInput = new ArrayList<OScope.CorrelationSet>();

  /** Correlation sets asserted on output. */
  public final List<OScope.CorrelationSet> assertCorrelationsOutput = new ArrayList<OScope.CorrelationSet>();

  public OInvoke(OProcess owner) {
    super(owner);
  }
}
