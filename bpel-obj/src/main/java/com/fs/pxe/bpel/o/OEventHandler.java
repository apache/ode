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
 * Compiled represenation of a BPEL event handler.
 */
public class OEventHandler extends OAgent {
  static final long serialVersionUID = -1L  ;
	public List<OEvent> onMessages = new ArrayList<OEvent>();
  public List<OAlarm> onAlarms = new ArrayList<OAlarm>();

  public OEventHandler(OProcess owner) {
    super(owner);
  }

  public static class OAlarm extends OAgent {
    static final long serialVersionUID = -1L  ;

    public OExpression forExpr;
    public OExpression untilExpr;
    public OExpression repeatExpr;
    public OActivity activity;

		public OAlarm(OProcess owner){
			super(owner);
		}
	}
	
	public static class OEvent extends OScope {
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


    public String getCorrelatorId() {
      return partnerLink.getId() + "." + operation.getName();
    }

		public OEvent(OProcess owner) {
      super(owner);
		}
	}
}
