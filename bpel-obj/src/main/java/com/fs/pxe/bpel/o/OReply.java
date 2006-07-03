/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.o;

import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Fault;
import javax.wsdl.Operation;

/**
 * Compiled representation of the BPEL <code>&lt;reply&gt;</code> activity.
 */
public class OReply extends OActivity {
  
  static final long serialVersionUID = -1L  ;

  /** Is this a Fault reply? */
  public boolean isFaultReply;

  /** The type of the fault (if isFaultReply). */
  public Fault fault;

  public OPartnerLink partnerLink;
  public Operation operation;
  public OScope.Variable variable;

  /** Correlation sets initialized. */
  public final List<OScope.CorrelationSet> initCorrelations = new ArrayList<OScope.CorrelationSet>();

  /** Correlation sets asserted. */
  public final List<OScope.CorrelationSet> assertCorrelations = new ArrayList<OScope.CorrelationSet>();

  /** OASIS modification - Message Exchange Id. */
  public String messageExchangeId = "";

  public OReply(OProcess owner) {
    super(owner);
  }
}
