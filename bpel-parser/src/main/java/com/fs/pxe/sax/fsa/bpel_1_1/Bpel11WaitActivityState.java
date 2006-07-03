/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_1_1;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.impl.nodes.ExpressionImpl;
import com.fs.pxe.bom.impl.nodes.WaitActivityImpl;
import com.fs.pxe.sax.fsa.*;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;
import com.fs.sax.evt.attspec.FilterSpec;
import com.fs.sax.evt.attspec.OrSpec;
import com.fs.sax.evt.attspec.XmlAttributeSpec;

class Bpel11WaitActivityState extends Bpel11BaseActivityState {

  private static final XmlAttributeSpec FOR = new FilterSpec(
      new String[] {"for"},BPEL11_BASE_ACTIVITY_ATTS); 
  private static final XmlAttributeSpec UNTIL = new FilterSpec(
      new String[] {"until"},BPEL11_BASE_ACTIVITY_ATTS);
  // TODO still required?
  private static final XmlAttributeSpec ALARMSPEC = new OrSpec(FOR,UNTIL);
  
  private static final StateFactory _factory = new Factory();
  
  Bpel11WaitActivityState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
  }
  
  protected Activity createActivity(StartElement se) throws ParseException {
    WaitActivityImpl wai = new WaitActivityImpl();
    XmlAttributes atts = se.getAttributes();
    ExpressionImpl expr = new ExpressionImpl();
    expr.setNamespaceContext(se.getNamespaceContext());
    expr.setLineNo(se.getLocation().getLineNumber());
    
    if (FOR.matches(atts)) {
      expr.setXPathString(atts.getValue("for"));
      wai.setFor(expr);
    } else if (UNTIL.matches(atts)) {
      expr.setXPathString(atts.getValue("until"));
      wai.setUntil(expr);
    }  else {
      getParseContext().parseError(ParseError.ERROR,se,"PARSER_ERROR",
          "Invalid attribute combination for wait; expected @for=\"<duration>\" or @until=\"<datetime>\""); // TODO: Error key.
    }
    return wai;
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
    return BPEL11_WAIT;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11WaitActivityState(se,pc);
    }
  }
}
