/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.sax.fsa.bpel_1_1;

import org.apache.ode.bom.api.PartnerLink;
import org.apache.ode.bom.impl.nodes.PartnerLinkImpl;
import org.apache.ode.sax.fsa.*;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;
import org.apache.ode.sax.evt.attspec.FilterSpec;
import org.apache.ode.sax.evt.attspec.OrSpec;
import org.apache.ode.sax.evt.attspec.XmlAttributeSpec;

class Bpel11PartnerLinkState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private PartnerLinkImpl _plink;
  
  private static final XmlAttributeSpec MYROLE_SET = new FilterSpec(
      new String[] {"name","partnerLinkType","myRole"},
      new String[] {"partnerRole"});
  private static final XmlAttributeSpec PARTNERROLE_SET = new FilterSpec(
      new String[] {"name","partnerLinkType","partnerRole"},
      new String[] {"myRole"});
  private static final XmlAttributeSpec VALID = new OrSpec(MYROLE_SET,PARTNERROLE_SET);
  
  Bpel11PartnerLinkState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    if (!VALID.matches(atts)){
      pc.parseError(ParseError.ERROR,se,"PARSER_ERROR",
          "At least one of @myRole and @partnerRole must be set.");
      // TODO: Internationalize
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
  }
  
  public PartnerLink getPartnerLink() {
    return _plink;
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
    return BPEL11_PARTNERLINK;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11PartnerLinkState(se,pc);
    }
  }
}
