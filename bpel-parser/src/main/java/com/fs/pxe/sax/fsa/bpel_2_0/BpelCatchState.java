/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.Catch;
import com.fs.pxe.sax.fsa.*;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;
import com.fs.sax.evt.attspec.FilterSpec;
import com.fs.sax.evt.attspec.OrSpec;
import com.fs.sax.evt.attspec.XmlAttributeSpec;

class BpelCatchState extends BpelCatchAllState {

  private static final StateFactory _factory = new Factory();
  
  private static final XmlAttributeSpec FAULTNAME = new FilterSpec(
      new String[] {"faultName"}, new String[] {"faultVariable","faultMessageType","faultElement"});
  private static final XmlAttributeSpec FAULTVARIABLE = new FilterSpec(
      new String[] {"faultVariable"}, new String[] {"faultMessageType","faultElement"});
  
  private static final XmlAttributeSpec VALID = new OrSpec(FAULTNAME,FAULTVARIABLE);
  
  BpelCatchState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
    XmlAttributes atts = se.getAttributes();
    if (!VALID.matches(atts)) {
      pc.parseError(ParseError.ERROR,se,"PARSER_ERROR",
          "Invalid attributes for <catch>; expected @faultName and/or @faultVariable with @faultMessageType optional.");
    }
    Catch c = getCatch();
    if (atts.hasAtt("faultName")) 
      c.setFaultName(se.getNamespaceContext().derefQName(atts.getValue("faultName")));
    if (atts.hasAtt("faultVariable")) 
      c.setFaultVariable(atts.getValue("faultVariable"));
    if(atts.hasAtt("faultMessageType"))
      c.setFaultVariableMessageType(se.getNamespaceContext().derefQName(atts.getValue("faultMessageType")));
    if(atts.hasAtt("faultElement"))
      c.setFaultVariableElementType(se.getNamespaceContext().derefQName(atts.getValue("faultElement")));
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
    return BPEL_CATCH;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelCatchState(se,pc);
    }
  }
}
