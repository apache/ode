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
import org.apache.ode.bom.api.CompletionCondition;
import org.apache.ode.sax.evt.SaxEvent;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.fsa.DOMGenerator;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;

class BpelCompletionConditionState extends BaseBpelState {

    private static final StateFactory _factory = new BpelCompletionConditionState.Factory();
    private CompletionCondition _branches;
    private DOMGenerator _domGenerator;

    private BpelCompletionConditionState(StartElement se, ParseContext pc) throws ParseException {
        super(se, pc);
    }

    protected BpelObject createBpelObject(StartElement se) throws ParseException {
        _domGenerator = new DOMGenerator();
        return null;
    }

    public CompletionCondition getCompletionCondition() {
        return _branches;
    }

    /**
     * @see org.apache.ode.sax.fsa.State#handleChildCompleted(org.apache.ode.sax.fsa.State)
     */
    public void handleChildCompleted(State pn) throws ParseException {
        if (pn.getType() == BPEL_BRANCHES) {
            _branches = ((BpelBranchesState)pn).getCompletionCondition();
        } else {
            super.handleChildCompleted(pn);
        }
    }

    public void handleSaxEvent(SaxEvent se) throws ParseException {
        _domGenerator.handleSaxEvent(se);
    }

    /**
     * @see org.apache.ode.sax.fsa.State#done()
     */
    public void done(){
    }

    /**
     * @see org.apache.ode.sax.fsa.State#getFactory()
     */
    public StateFactory getFactory() {
        return BpelCompletionConditionState._factory;
    }

    /**
     * @see org.apache.ode.sax.fsa.State#getType()
     */
    public int getType() {
        return BPEL_COMPLETION_CONDITION;
    }

    static class Factory implements StateFactory {

        public State newInstance(StartElement se, ParseContext pc) throws ParseException {
            return new BpelCompletionConditionState(se,pc);
        }
    }
}
