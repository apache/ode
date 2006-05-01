/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.LinkSource;
import org.apache.ode.bom.api.LinkTarget;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

import java.util.Iterator;

abstract class BpelBaseActivityState extends BaseBpelState implements ActivityStateI {

  private Activity _activity;
  
  protected static final String[] BPEL11_BASE_ACTIVITY_ATTS = new String[] {
    "name","suppressJoinFailure","joinCondition"
  };  
  
  protected BpelBaseActivityState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    _activity = createActivity(se);
    XmlAttributes atts = se.getAttributes();

    _activity.setNamespaceContext(se.getNamespaceContext());
    _activity.setLineNo(se.getLocation().getLineNumber());
    
    if (atts.hasAtt("name")) {
      _activity.setName(atts.getValue("name"));
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
      case BPEL_TARGETS:
        _activity.setJoinCondition(((BpelLinkTargetsState)pn).getJoinCondition());
        for(Iterator<LinkTarget> iter = ((BpelLinkTargetsState)pn).getTargets(); iter.hasNext(); )
        	_activity.addTarget(iter.next());
        break;
      case BPEL_SOURCES:
        for(Iterator<LinkSource> iter = ((BpelLinkSourcesState)pn).getSources(); iter.hasNext(); )
        	_activity.addSource(iter.next());
        break;
    default:
      super.handleChildCompleted(pn);
    }
  }

}
