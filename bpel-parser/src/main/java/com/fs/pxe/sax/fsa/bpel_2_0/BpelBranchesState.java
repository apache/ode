package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.CompletionCondition;
import com.fs.pxe.bom.impl.nodes.CompletionConditionImpl;
import com.fs.pxe.sax.fsa.*;
import com.fs.sax.evt.SaxEvent;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;

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
   * @see com.fs.pxe.sax.fsa.State#handleSaxEvent(com.fs.sax.evt.SaxEvent)
   */
  public void handleSaxEvent(SaxEvent se) throws ParseException {
    _domGenerator.handleSaxEvent(se);
  }
  /**
   * @see com.fs.pxe.sax.fsa.State#done()
   */
  public void done(){
    _expr.setNode(_domGenerator.getRoot());
  }

  /**
   * @see com.fs.pxe.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return BpelBranchesState._factory;
  }

  /**
   * @see com.fs.pxe.sax.fsa.State#getType()
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
