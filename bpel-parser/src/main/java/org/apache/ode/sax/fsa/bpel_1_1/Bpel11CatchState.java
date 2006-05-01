/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_1_1;

import org.apache.ode.bom.api.Catch;
import org.apache.ode.sax.fsa.*;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;
import org.apache.ode.sax.evt.attspec.FilterSpec;
import org.apache.ode.sax.evt.attspec.OrSpec;
import org.apache.ode.sax.evt.attspec.XmlAttributeSpec;

class Bpel11CatchState extends Bpel11CatchAllState {

  private static final StateFactory _factory = new Factory();
  
  private static final XmlAttributeSpec FAULTNAME = new FilterSpec(
      new String[] {"faultName"}, new String[] {"faultVariable"});
  private static final XmlAttributeSpec FAULTVARIABLE = new FilterSpec(
      new String[] {"faultVariable"}, new String[] {});
  
  private static final XmlAttributeSpec VALID = new OrSpec(FAULTNAME,FAULTVARIABLE);
  
  Bpel11CatchState(StartElement se,ParseContext pc) throws ParseException {
    super(se,pc);
    XmlAttributes atts = se.getAttributes();
    if (!VALID.matches(atts)) {
      pc.parseError(ParseError.ERROR,se,"PARSER_ERROR",
          "Invalid attributes for <catch>; @faultName?,@faultVariable? expected.");
    }
    Catch c = getCatch();
    if (atts.hasAtt("faultName")) 
      c.setFaultName(se.getNamespaceContext().derefQName(atts.getValue("faultName")));
    if (atts.hasAtt("faultVariable")) 
      c.setFaultVariable(atts.getValue("faultVariable"));
//    following is not strictly 1.1, but we'll let it slide
//    if(atts.hasAtt("faultMessageType"))
//      c.setFaultVariableType(se.getNamespaceContext().derefQName(atts.getValue("faultMessageType")));
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
    return BPEL11_CATCH;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11CatchState(se,pc);
    }
  }
}
