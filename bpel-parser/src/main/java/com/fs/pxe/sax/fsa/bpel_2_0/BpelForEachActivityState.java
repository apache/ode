package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.ScopeActivity;
import com.fs.pxe.bom.impl.nodes.ForEachActivityImpl;
import com.fs.pxe.sax.fsa.ParseContext;
import com.fs.pxe.sax.fsa.ParseException;
import com.fs.pxe.sax.fsa.State;
import com.fs.pxe.sax.fsa.StateFactory;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;

public class BpelForEachActivityState extends BpelBaseActivityState {

  private static final StateFactory _factory = new BpelProcessState.Factory();


  BpelForEachActivityState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
  }

  protected Activity createActivity(StartElement se) {
    XmlAttributes atts = se.getAttributes();
    ForEachActivityImpl forEach = new ForEachActivityImpl();
    forEach.setCounterName(atts.getValue("counterName"));
    forEach.setParallel(checkYesNo(atts.getValue("parallel")));
    return forEach;
  }

  public void handleChildCompleted(State pn) throws ParseException {
    if (pn instanceof BpelScopeState) {
      ((ForEachActivityImpl)getActivity()).setScope((ScopeActivity) ((BpelScopeState)pn).getActivity());
    } else if (pn.getType() == BPEL_START_COUNTER_VALUE) {
      ((ForEachActivityImpl)getActivity()).setStartCounter(((BpelStartCounterValueState)pn).getExpression());
    } else if (pn.getType() == BPEL_FINAL_COUNTER_VALUE) {
      ((ForEachActivityImpl)getActivity()).setFinalCounter(((BpelFinalCounterValueState)pn).getExpression());
    } else if (pn.getType() == BPEL_ITERATOR) {
      ((ForEachActivityImpl)getActivity()).setStartCounter(((BpelIteratorState)pn).getStartCounterValue());
      ((ForEachActivityImpl)getActivity()).setFinalCounter(((BpelIteratorState)pn).getFinalCounterValue());
    } else if (pn.getType() == BPEL_COMPLETION_CONDITION) {
      ((ForEachActivityImpl)getActivity())
              .setCompletionCondition(((BpelCompletionConditionState)pn).getCompletionCondition());
    } else {
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
    return BPEL_FOREACH;
  }

  static class Factory implements StateFactory {

    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelForEachActivityState(se,pc);
    }
  }

}
