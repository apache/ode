/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.PartnerLink;
import com.fs.pxe.bom.impl.nodes.PartnerLinkImpl;
import com.fs.pxe.sax.fsa.*;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;
import com.fs.sax.evt.attspec.FilterSpec;
import com.fs.sax.evt.attspec.OrSpec;
import com.fs.sax.evt.attspec.XmlAttributeSpec;

class BpelPartnerLinkState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private PartnerLinkImpl _plink;
  
  private static final XmlAttributeSpec MYROLE_SET = new FilterSpec(
      new String[] {"name","partnerLinkType","myRole"},
      new String[] {"partnerRole","initializePartnerRole"});
  private static final XmlAttributeSpec PARTNERROLE_SET = new FilterSpec(
      new String[] {"name","partnerLinkType","partnerRole"},
      new String[] {"myRole", "initializePartnerRole"});
  private static final XmlAttributeSpec VALID = new OrSpec(MYROLE_SET,PARTNERROLE_SET);
  
  BpelPartnerLinkState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    if (!VALID.matches(atts)){
      pc.parseError(ParseError.ERROR,se,"",
          "Invalid attribute combination on <partnerLink>.");
    }
    _plink = new PartnerLinkImpl();
    _plink.setNamespaceContext(se.getNamespaceContext());
    _plink.setLineNo(se.getLocation().getLineNumber());
    _plink.setName(atts.getValue("name"));
    _plink.setPartnerLinkType(
        se.getNamespaceContext().derefQName(atts.getValue("partnerLinkType")));
    if (atts.hasAtt("myRole")) {
      _plink.setMyRole(atts.getValue("myRole"));
    }
    if (atts.hasAtt("partnerRole")) {
      _plink.setPartnerRole(atts.getValue("partnerRole"));
    }
    if (atts.hasAtt("initializePartnerRole")) {
      _plink.setInitializePartnerRole(checkYesNo(atts.getValue("initializePartnerRole")));
    }
  }
  
  public PartnerLink getPartnerLink() {
    return _plink;
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
    return BPEL_PARTNERLINK;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelPartnerLinkState(se,pc);
    }
  }
}
