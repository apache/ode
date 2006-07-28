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
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.To;
import org.apache.ode.bom.impl.nodes.*;
import org.apache.ode.sax.fsa.*;
import org.apache.ode.sax.evt.SaxEvent;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;
import org.apache.ode.sax.evt.attspec.FilterSpec;
import org.apache.ode.sax.evt.attspec.XmlAttributeSpec;

class BpelToState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private To _t;
  
  private XmlAttributeSpec PLINK = new FilterSpec(
      new String[] {"partnerLink"},new String[] {});

  private XmlAttributeSpec VAR = new FilterSpec(
        new String[] {"variable"},
        new String[] {"part"});

  private XmlAttributeSpec VAR_PROP = new FilterSpec(
      new String[] {"variable","property"},new String[] {});

  private StartElement _se;
  private String _queryLanguage;
  private DOMGenerator _domBuilder;
  private ExpressionImpl _expr;

  BpelToState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    _se = se;
    XmlAttributes atts = se.getAttributes();
    
    _queryLanguage = atts.getValue("queryLanguage");
    
    if (VAR_PROP.matches(atts)) {
      PropertyValImpl pvi = new PropertyValImpl(se.getNamespaceContext());
      pvi.setLineNo(se.getLocation().getLineNumber());
      pvi.setVariable(atts.getValue("variable"));
      pvi.setProperty(se.getNamespaceContext().derefQName(atts.getValue("property")));
      _t = pvi;
    } if (VAR.matches(atts)) {
      VariableValImpl vvi = new VariableValImpl();
      vvi.setLineNo(se.getLocation().getLineNumber());
      vvi.setVariable(atts.getValue("variable"));
      vvi.setPart(atts.getValue("part"));
      _t = vvi;
    } else if (PLINK.matches(atts)) {
    PartnerLinkValImpl pvi = new PartnerLinkValImpl();
    pvi.setLineNo(se.getLocation().getLineNumber());
    pvi.setPartnerLink(atts.getValue("partnerLink"));
    _t = pvi;
    } else {
      if (_queryLanguage != null)
        _expr = new ExpressionImpl(_queryLanguage);
      else
        _expr = new ExpressionImpl();
      
			_expr.setNamespaceContext(_se.getNamespaceContext());
			_expr.setLineNo(_se.getLocation().getLineNumber());
    	_domBuilder = new DOMGenerator();
    }
  }
  
  /**
   * @see org.apache.ode.sax.fsa.State#handleSaxEvent(org.apache.ode.sax.evt.SaxEvent)
   */
  public void handleSaxEvent(SaxEvent se) throws ParseException {
    if (_domBuilder == null) {
      getParseContext().parseError(ParseError.ERROR,se,"","Unexpected content in the <to> element.");
      assert false;
    }
    _domBuilder.handleSaxEvent(se);
  }
  
  /**
   * @see org.apache.ode.sax.fsa.State#done()
   */
  public void done(){
  	if(_domBuilder != null && _t == null){
			_expr.setNode(_domBuilder.getRoot());
			ExpressionValImpl evi = new ExpressionValImpl(_expr.getNamespaceContext());
      evi.setExpression(_expr);
      _t = evi;
    }
  }
  
  
  public To getTo() {
    return _t;
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
    return BPEL_TO;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelToState(se,pc);
    }
  }
}
