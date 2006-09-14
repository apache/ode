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

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.BpelObject;
import org.apache.ode.bom.api.Expression;
import org.apache.ode.bom.impl.nodes.SwitchActivityImpl;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;

class BpelSwitchActivityState extends BpelBaseActivityState {

    private static final StateFactory _factory = new Factory();

    BpelSwitchActivityState(StartElement se, ParseContext pc) throws ParseException {
        super(se,pc);
    }

    protected Activity createActivity(StartElement se) {
        return new SwitchActivityImpl();
    }

    public void handleChildCompleted(State pn) throws ParseException {
        if (pn.getType() == BPEL_CASE) {
            CaseState c = (CaseState)pn;
            ((SwitchActivityImpl)getActivity()).addCase(c.getExpression(),c.getActivity());
        } else if (pn.getType() == BPEL_OTHERWISE){
            ((SwitchActivityImpl)getActivity()).addCase(
                    null,((OtherwiseState)pn).getActivity());
        } else {
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
        return BPEL_SWITCH;
    }

    static class Factory implements StateFactory {

        public State newInstance(StartElement se, ParseContext pc) throws ParseException {
            return new BpelSwitchActivityState(se,pc);
        }
    }

    static class CaseState extends OtherwiseState {

        private static final StateFactory _factory = new Factory();

        private Expression _e;

        CaseState(StartElement se, ParseContext pc) throws ParseException {
            super(se,pc);
        }

        public void handleChildCompleted(State pn) throws ParseException {
            switch (pn.getType()) {
                case BPEL_EXPRESSION:
                    _e = ((BpelExpressionState)pn).getExpression();
                    break;
                default:
                    super.handleChildCompleted(pn);
            }
        }

        public Expression getExpression() {
            return _e;
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
            return BPEL_CASE;
        }

        static class Factory implements StateFactory {

            public State newInstance(StartElement se, ParseContext pc) throws ParseException {
                return new CaseState(se,pc);
            }
        }

    }

    static class OtherwiseState extends BaseBpelState {

        private static final StateFactory _factory = new Factory();
        private Activity _a;

        OtherwiseState(StartElement se, ParseContext pc) throws ParseException {
            super(se, pc);
            // Do nothing in this case.
        }

        protected BpelObject createBpelObject(StartElement se) throws ParseException {
            return null;
        }

        public void handleChildCompleted(State pn) throws ParseException {
            if (pn instanceof ActivityStateI) {
                _a = ((ActivityStateI)pn).getActivity();
            } else {
                super.handleChildCompleted(pn);
            }
        }

        public Activity getActivity() {
            return _a;
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
            return BPEL_OTHERWISE;
        }

        static class Factory implements StateFactory {

            public State newInstance(StartElement se, ParseContext pc) throws ParseException {
                return new OtherwiseState(se,pc);
            }
        }
    }
}
