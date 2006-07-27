/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_1_1;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.impl.nodes.ExpressionImpl;
import org.apache.ode.bom.impl.nodes.WaitActivityImpl;
import org.apache.ode.sax.fsa.*;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;
import org.apache.ode.sax.evt.attspec.FilterSpec;
import org.apache.ode.sax.evt.attspec.OrSpec;
import org.apache.ode.sax.evt.attspec.XmlAttributeSpec;

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
   * @see org.apache.ode.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return _factory;
  }

  /**
   * @see org.apache.ode.sax.fsa.State#getType()
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
