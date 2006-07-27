/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_1_1;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.impl.nodes.ExpressionImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

abstract class Bpel11BaseActivityState extends BaseBpelState implements ActivityStateI {

  private Activity _activity;
  
  protected static final String[] BPEL11_BASE_ACTIVITY_ATTS = new String[] {
    "name","suppressJoinFailure","joinCondition"
  };  
  
  protected Bpel11BaseActivityState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    _activity = createActivity(se);
    XmlAttributes atts = se.getAttributes();

    _activity.setNamespaceContext(se.getNamespaceContext());
    _activity.setLineNo(se.getLocation().getLineNumber());
    
    if (atts.hasAtt("name")) {
      _activity.setName(atts.getValue("name"));
    }
    if(atts.hasAtt("joinCondition")){
      ExpressionImpl expr = new ExpressionImpl();
      expr.setNamespaceContext(se.getNamespaceContext());
      expr.setLineNo(se.getLocation().getLineNumber());
      expr.setXPathString(atts.getValue("joinCondition"));
    	_activity.setJoinCondition(expr);
    }
    _activity.setSuppressJoinFailure(getSuppressJoinFailure(atts));
  }
  
  protected abstract Activity createActivity(StartElement se) throws ParseException;
  
  public Activity getActivity() {
    return _activity;
  }
  
  /**
   * @see org.apache.ode.sax.fsa.State#handleChildCompleted(org.apache.ode.sax.fsa.State)
   */
  public void handleChildCompleted(State pn) throws ParseException {
    switch (pn.getType()) {
      case BPEL11_TARGET:
        _activity.addTarget(((Bpel11LinkTargetState)pn).getTarget());
        break;
      case BPEL11_SOURCE:
        _activity.addSource(((Bpel11LinkSourceState)pn).getSource());
        break;
    default:
      super.handleChildCompleted(pn);
    }
  }

}
