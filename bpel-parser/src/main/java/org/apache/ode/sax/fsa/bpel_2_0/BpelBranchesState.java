package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.CompletionCondition;
import org.apache.ode.bom.impl.nodes.CompletionConditionImpl;
import org.apache.ode.sax.fsa.*;
import org.apache.ode.sax.evt.SaxEvent;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

class BpelBranchesState extends BaseBpelState {

  private static final StateFactory _factory = new BpelBranchesState.Factory();
  private CompletionConditionImpl _expr;
  private DOMGenerator _domGenerator;

  BpelBranchesState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);

    XmlAttributes attr = se.getAttributes();
    if(attr.hasAtt("expressionLanguage")){
      _expr = new CompletionConditionImpl(attr.getValue("expressionLanguage"));
    }else{
      _expr = new CompletionConditionImpl();
    }
    if (attr.hasAtt("successfulBranchesOnly")) {
      _expr.setSuccessfulBranchesOnly(checkYesNo(attr.getValue("successfulBranchesOnly")));
    }
    _expr.setNamespaceContext(se.getNamespaceContext());
    _expr.setLineNo(se.getLocation().getLineNumber());

    _domGenerator = new DOMGenerator();
  }

  CompletionCondition getCompletionCondition(){
    return _expr;
  }

  /**
   * @see org.apache.ode.sax.fsa.State#handleSaxEvent(org.apache.ode.sax.evt.SaxEvent)
   */
  public void handleSaxEvent(SaxEvent se) throws ParseException {
    _domGenerator.handleSaxEvent(se);
  }
  /**
   * @see org.apache.ode.sax.fsa.State#done()
   */
  public void done(){
    _expr.setNode(_domGenerator.getRoot());
  }

  /**
   * @see org.apache.ode.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return BpelBranchesState._factory;
  }

  /**
   * @see org.apache.ode.sax.fsa.State#getType()
   */
  public int getType() {
    return BPEL_BRANCHES;
  }

  static class Factory implements StateFactory {

    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelBranchesState(se,pc);
    }
  }
}
