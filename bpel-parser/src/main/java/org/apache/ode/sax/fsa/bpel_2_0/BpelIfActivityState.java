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
import org.apache.ode.bom.api.Expression;
import org.apache.ode.bom.impl.nodes.SwitchActivityImpl;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

class BpelIfActivityState extends BpelBaseActivityState {
	private static final StateFactory _factory = new Factory();
  private Expression _ifExpr;
  
	BpelIfActivityState(StartElement se, ParseContext pc) throws ParseException {
		super(se,pc);
	}
	protected Activity createActivity(StartElement se) {
		return new SwitchActivityImpl();
	}
	public void handleChildCompleted(State pn) throws ParseException {
		switch(pn.getType()){
			case BPEL_THEN: 
				if(_ifExpr == null)
					throw new IllegalStateException("Missing 'if' condition");
        ((SwitchActivityImpl) getActivity()).addCase(_ifExpr, 
            ((ThenState)pn).getActivity());
				break;
      case BPEL_EXPRESSION:
      	_ifExpr = ((BpelExpressionState)pn).getExpression();
        break;
      case BPEL_ELSEIF:
      	ElseIfState c = (ElseIfState) pn;
      	((SwitchActivityImpl) getActivity()).addCase(c.getExpression(), c
					.getActivity());
        break;
		  case BPEL_ELSE:
		  	((SwitchActivityImpl) getActivity()).addCase(null,
					((ElseState) pn).getActivity());
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
		return BPEL_IF;
	}
	static class Factory implements StateFactory {
		public State newInstance(StartElement se, ParseContext pc) throws ParseException {
			return new BpelIfActivityState(se,pc);
		}
	}
	static class ElseIfState extends ElseState {
		private static final StateFactory _factory = new Factory();
		private Expression _e;
		ElseIfState(StartElement se,ParseContext pc) throws ParseException {
			super(se,pc);
		}
		public void handleChildCompleted(State pn) throws ParseException {
			switch (pn.getType()) {
				case BPEL_EXPRESSION :
					_e = ((BpelExpressionState) pn).getExpression();
					break;
				default :
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
			return BPEL_ELSEIF;
		}
		static class Factory implements StateFactory {
			public State newInstance(StartElement se, ParseContext pc) throws ParseException {
				return new ElseIfState(se,pc);
			}
		}
	}
	static class ElseState extends BaseBpelState {
		private static final StateFactory _factory = new Factory();
		private Activity _a;
		ElseState(StartElement se, ParseContext pc) throws ParseException {
      super(pc);
		}
		public void handleChildCompleted(State pn) throws ParseException {
			if (pn instanceof ActivityStateI) {
				_a = ((ActivityStateI) pn).getActivity();
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
			return BPEL_ELSE;
		}
		static class Factory implements StateFactory {
			public State newInstance(StartElement se, ParseContext pc) throws ParseException {
				return new ElseState(se,pc);
			}
		}
	}
  static class ThenState extends BaseBpelState {
    private static final StateFactory _factory = new Factory();
    private Activity _a;
    ThenState(StartElement se, ParseContext pc) throws ParseException {
      super(pc);
    }
    public void handleChildCompleted(State pn) throws ParseException {
      if (pn instanceof ActivityStateI) {
        _a = ((ActivityStateI) pn).getActivity();
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
      return BPEL_THEN;
    }
    static class Factory implements StateFactory {
      public State newInstance(StartElement se, ParseContext pc) throws ParseException {
        return new ThenState(se,pc);
      }
    }
  }
}