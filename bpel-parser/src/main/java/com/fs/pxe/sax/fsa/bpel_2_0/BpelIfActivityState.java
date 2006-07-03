/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.Expression;
import com.fs.pxe.bom.impl.nodes.SwitchActivityImpl;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;

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
	 * @see com.fs.pxe.sax.fsa.State#getFactory()
	 */
	public StateFactory getFactory() {
		return _factory;
	}
	/**
	 * @see com.fs.pxe.sax.fsa.State#getType()
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
		 * @see com.fs.pxe.sax.fsa.State#getFactory()
		 */
		public StateFactory getFactory() {
			return _factory;
		}
		/**
		 * @see com.fs.pxe.sax.fsa.State#getType()
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
		 * @see com.fs.pxe.sax.fsa.State#getFactory()
		 */
		public StateFactory getFactory() {
			return _factory;
		}
		/**
		 * @see com.fs.pxe.sax.fsa.State#getType()
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
     * @see com.fs.pxe.sax.fsa.State#getFactory()
     */
    public StateFactory getFactory() {
      return _factory;
    }
    /**
     * @see com.fs.pxe.sax.fsa.State#getType()
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