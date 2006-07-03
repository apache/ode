/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.LinkSource;
import com.fs.pxe.bom.api.LinkTarget;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;

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
   * @see com.fs.pxe.sax.fsa.State#handleChildCompleted(com.fs.pxe.sax.fsa.State)
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
