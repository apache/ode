/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.Copy;
import org.apache.ode.bom.impl.nodes.CopyImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

class BpelCopyState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private CopyImpl _c;

  BpelCopyState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    _c = new CopyImpl(se.getNamespaceContext());
    _c.setLineNo(se.getLocation().getLineNumber());
    if (atts.hasAtt("keepSrcElementName"))
      _c.setKeepSrcElement(checkYesNo(atts.getValue("keepSrcElementName")));
  }

  public Copy getCopy() {
    return _c;
  }

  /**
   * @see org.apache.ode.sax.fsa.State#handleChildCompleted(org.apache.ode.sax.fsa.State)
   */
  public void handleChildCompleted(State pn) throws ParseException {
    switch (pn.getType()) {
    case BPEL_FROM:
      _c.setFrom(((BpelFromState)pn).getFrom());
      break;
    case BPEL_TO:
      _c.setTo(((BpelToState)pn).getTo());
      break;
    default:
      super.handleChildCompleted(pn);
    }
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
    return BPEL_COPY;
  }

  static class Factory implements StateFactory {

    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelCopyState(se,pc);
    }
  }
}
