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

import org.apache.ode.bom.api.BpelObject;
import org.apache.ode.bom.api.Correlation;
import org.apache.ode.bom.impl.nodes.CorrelationImpl;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;

class BpelCorrelationState extends BaseBpelState {

    private static final StateFactory _factory = new Factory();
    private CorrelationImpl _c;

    BpelCorrelationState(StartElement se, ParseContext pc) throws ParseException {
        super(se, pc);
    }

    protected BpelObject createBpelObject(StartElement se) throws ParseException {
        XmlAttributes atts = se.getAttributes();
        _c = new CorrelationImpl();
        _c.setNamespaceContext(se.getNamespaceContext());
        _c.setLineNo(se.getLocation().getLineNumber());
        _c.setCorrelationSet(atts.getValue("set"));
        _c.setInitiate(getInitiateYesNo(atts));
        if (atts.hasAtt("pattern")) {
            String pat = atts.getValue("pattern");
            if (pat.equals("out") || pat.equals("request")) {
                _c.setPattern(Correlation.CORRPATTERN_OUT);
            } else if (pat.equals("in") || pat.equals("response")) {
                _c.setPattern(Correlation.CORRPATTERN_IN);
            } else if (pat.equals("out-in") || pat.equals("request-response")) {
                _c.setPattern(Correlation.CORRPATTERN_INOUT);
            } else {
                throw new IllegalStateException("Bad correlation pattern: " + pat);
            }
        }
        return _c;
    }

    public Correlation getCorrelation() {
        return _c;
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
        return BPEL_CORRELATION;
    }

    static class Factory implements StateFactory {

        public State newInstance(StartElement se, ParseContext pc) throws ParseException {
            return new BpelCorrelationState(se,pc);
        }
    }
}
