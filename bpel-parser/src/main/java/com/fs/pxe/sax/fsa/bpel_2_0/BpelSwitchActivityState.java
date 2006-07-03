/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.Expression;
import com.fs.pxe.bom.impl.nodes.SwitchActivityImpl;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;

class BpelSwitchActivityState extends BpelBaseActivityState {

  private static final StateFactory _factory = new Factory();

  BpelSwitchActivityState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
  }
  
  protected Activity createActivity(StartElement se) {
    return new SwitchActivityImpl();
  }
  
  public void handleChildCompleted(State pn) throws ParseException {
    if (pn.getType() == BPEL_CASE) {
      CaseState c = (CaseState)pn;
      ((SwitchActivityImpl)getActivity()).addCase(c.getExpression(),c.getActivity());
    } else if (pn.getType() == BPEL_OTHERWISE){
      ((SwitchActivityImpl)getActivity()).addCase(
          null,((OtherwiseState)pn).getActivity());      
    } else {
      super.handleChildCompleted(pn);
    }
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
    return BPEL_SWITCH;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelSwitchActivityState(se,pc);
    }
  }
  
  static class CaseState extends OtherwiseState {

    private static final StateFactory _factory = new Factory();
      
    private Expression _e;
    
    CaseState(StartElement se, ParseContext pc) throws ParseException {
      super(se,pc);
    }
    
    public void handleChildCompleted(State pn) throws ParseException {
      switch (pn.getType()) {
      case BPEL_EXPRESSION:
        _e = ((BpelExpressionState)pn).getExpression();
        break;
      default:
        super.handleChildCompleted(pn);
      }
    }
    
    public Expression getExpression() {
      return _e;
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
      return BPEL_CASE;
    }
    
    static class Factory implements StateFactory {
      
      public State newInstance(StartElement se, ParseContext pc) throws ParseException {
        return new CaseState(se,pc);
      }
    }

  }
  
  static class OtherwiseState extends BaseBpelState {

    private static final StateFactory _factory = new Factory();
    private Activity _a;
    
    OtherwiseState(StartElement se, ParseContext pc) throws ParseException {
      super(pc);
      // Do nothing in this case.
    }
    
    public void handleChildCompleted(State pn) throws ParseException {
      if (pn instanceof ActivityStateI) {
        _a = ((ActivityStateI)pn).getActivity();
      } else {
        super.handleChildCompleted(pn);
      }
    }
    
    public Activity getActivity() {
      return _a;
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
      return BPEL_OTHERWISE;
    }
    
    static class Factory implements StateFactory {
      
      public State newInstance(StartElement se, ParseContext pc) throws ParseException {
        return new OtherwiseState(se,pc);
      }
    }
  }
}
