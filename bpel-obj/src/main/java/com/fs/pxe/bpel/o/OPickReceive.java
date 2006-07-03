/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.o;

import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Operation;


/**
 * Compiled rerperesentation of the BPEL <code>&lt;pick&gt;</code> and
 * <codE>&lt;receive&gt;</code> activities. Because the latter is essentially
 * a simplified version of the former, at run-time we do not distinguish
 * between the two.
 */
public class OPickReceive extends OActivity{
  static final long serialVersionUID = -1L  ;
	public final List<OnMessage> onMessages  = new ArrayList<OnMessage>();
  public final List<OnAlarm> onAlarms = new ArrayList<OnAlarm>();

  public boolean createInstanceFlag;

  public OPickReceive(OProcess owner) {
    super(owner);
  }

  public static class OnAlarm extends OBase {
    static final long serialVersionUID = -1L  ;
		public OActivity activity;
    public OExpression forExpr;
    public OExpression untilExpr;

    public OnAlarm(OProcess owner) {
      super(owner);
    }
	}
	
	public static class OnMessage extends OBase {
    
    static final long serialVersionUID = -1L  ;
    
    /** Correlations to initialize. */
    public final List <OScope.CorrelationSet> initCorrelations = new ArrayList<OScope.CorrelationSet>();

    /** Correlation set to match on. */
    public OScope.CorrelationSet matchCorrelation;

    public OPartnerLink partnerLink;
    public Operation operation;
    public OScope.Variable variable;
    public OActivity activity;

    /** OASIS addition for disambiguating receives (optional). */
    public String messageExchangeId = "";

    public OnMessage(OProcess owner) {
      super(owner);
    }

    public String getCorrelatorId() {
      return partnerLink.getId() + "." + operation.getName();
    }
  }
}
