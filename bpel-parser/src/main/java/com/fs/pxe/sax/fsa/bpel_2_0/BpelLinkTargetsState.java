/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.Expression;
import com.fs.pxe.bom.api.LinkTarget;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;

import java.util.ArrayList;
import java.util.Iterator;

class BpelLinkTargetsState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private ArrayList<LinkTarget> _targets = new ArrayList<LinkTarget>();
  private Expression _expr;
  
  
  BpelLinkTargetsState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
  }
  
  
  public void handleChildCompleted(State pn) throws ParseException {
    if(pn instanceof BpelLinkTargetState){
      _targets.add(((BpelLinkTargetState)pn).getTarget()); 
    }else if(pn instanceof BpelExpressionState){
    	_expr = ((BpelExpressionState)pn).getExpression();
    }else{
      super.handleChildCompleted(pn);
    }
  }
  
  public Expression getJoinCondition(){
  	return _expr;
  }
  
  public Iterator<LinkTarget> getTargets() {
    return _targets.iterator();
  }

  /**
   * @see com.fs.pxe.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return _factory;
  }

  /**
   * @see com.fs.pxe.sax.fsa.State#getType()
   */
  public int getType() {
    return BPEL_TARGETS;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelLinkTargetsState(se,pc);
    }
  }
}
