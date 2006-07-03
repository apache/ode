/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_1_1;

import com.fs.pxe.bom.api.Variable;
import com.fs.pxe.bom.impl.nodes.VariableImpl;
import com.fs.pxe.sax.fsa.*;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;
import com.fs.sax.evt.attspec.FilterSpec;
import com.fs.sax.evt.attspec.OrSpec;
import com.fs.sax.evt.attspec.XmlAttributeSpec;

class Bpel11VariableState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private VariableImpl _v;
  
  private static final XmlAttributeSpec MESSAGETYPE = new FilterSpec(
      new String[] {"name","messageType"},
      new String[] {});
  private static final XmlAttributeSpec ELEMENTTYPE = new FilterSpec(
      new String[] {"name","element"},
      new String[] {});
  private static final XmlAttributeSpec TYPE = new FilterSpec(
      new String[] {"name","type"},
      new String[] {});

  private static final XmlAttributeSpec VALID = new OrSpec(MESSAGETYPE,
      new OrSpec(TYPE,ELEMENTTYPE));
  
  Bpel11VariableState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    if (!VALID.matches(atts)){
      pc.parseError(ParseError.ERROR,se,"PARSER_ERROR",
          "Invalid attributes " + atts.toString() + " for <variable>");
    }
    _v = new VariableImpl();
    _v.setNamespaceContext(se.getNamespaceContext());
    _v.setLineNo(se.getLocation().getLineNumber());
    _v.setName(atts.getValue("name"));
    if (MESSAGETYPE.matches(atts)) {
      _v.setMessageType(se.getNamespaceContext().derefQName(atts.getValue("messageType")));
    } else if (TYPE.matches(atts)) {
      _v.setSchemaType(se.getNamespaceContext().derefQName(atts.getValue("type")));
    } else if (ELEMENTTYPE.matches(atts)) {
      _v.setElementType(se.getNamespaceContext().derefQName(atts.getValue("element")));
    }
  }
  
  public Variable getVariable() {
    return _v;
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
    return BPEL11_VARIABLE;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11VariableState(se,pc);
    }
  }
}
