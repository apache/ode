/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_1_1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.namespace.QName;

import com.fs.pxe.bpel.parser.BpelProcessBuilder;
import com.fs.pxe.sax.fsa.AbstractGraphProvider;
import com.fs.pxe.sax.fsa.StateFactory;

public class BpelGraph_1_1 extends AbstractGraphProvider implements Bpel11QNames {
  
  private static final HashMap<QName,StateFactory> _11ACTIVITIES = new HashMap<QName,StateFactory>();
  static {
    _11ACTIVITIES.put(Bpel11QNames.ASSIGN,new Bpel11AssignActivityState.Factory());
    _11ACTIVITIES.put(Bpel11QNames.EMPTY,new Bpel11EmptyActivityState.Factory());
    _11ACTIVITIES.put(Bpel11QNames.INVOKE,new Bpel11InvokeActivityState.Factory());
    _11ACTIVITIES.put(Bpel11QNames.RECEIVE,new Bpel11ReceiveActivityState.Factory());
    _11ACTIVITIES.put(Bpel11QNames.REPLY,new Bpel11ReplyActivityState.Factory());
    _11ACTIVITIES.put(Bpel11QNames.WAIT,new Bpel11WaitActivityState.Factory());
    _11ACTIVITIES.put(Bpel11QNames.THROW,new Bpel11ThrowActivityState.Factory());
    _11ACTIVITIES.put(Bpel11QNames.TERMINATE,new Bpel11TerminateActivityState.Factory());
    _11ACTIVITIES.put(Bpel11QNames.FLOW,new Bpel11FlowActivityState.Factory());
    _11ACTIVITIES.put(Bpel11QNames.SWITCH,new Bpel11SwitchActivityState.Factory());
    _11ACTIVITIES.put(Bpel11QNames.WHILE,new Bpel11WhileActivityState.Factory());
    _11ACTIVITIES.put(Bpel11QNames.SEQUENCE,new Bpel11SequenceActivityState.Factory());
    _11ACTIVITIES.put(Bpel11QNames.PICK,new Bpel11PickActivityState.Factory());
    _11ACTIVITIES.put(Bpel11QNames.SCOPE,new Bpel11ScopeState.Factory());
  }
  
  private static final HashSet<QName> _11EXTENSIBLE_ELEMENTS = new HashSet<QName>();
  static {
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.PROCESS);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.PARTNERLINKS);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.PARTNERLINK);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.PARTNERS);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.PARTNER);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.FAULTHANDLERS);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.ONALARM);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.CATCH);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.COMPENSATIONHANDLER);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.EVENTHANDLERS);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.ONMESSAGE);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.VARIABLE);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.CORRELATION);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.CORRELATIONS);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.CORRELATIONSET);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.CORRELATIONSETS);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.SOURCE);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.TARGET);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.PROCESS);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.COPY);
    /*
     * <bp11:from> cannot be handled as extensible because it can contain literal
     * content.  Extensibility in this case in the schema is used for the literal
     * case.
     */
    //_11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.FROM);
    /*
     * <bp11:to> should probably be extensible, but the schema only allows attribute
     * extension
     */
    
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.LINK);
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.LINKS);
    // Add all activities.
    for(Iterator<QName> it = _11ACTIVITIES.keySet().iterator();it.hasNext();) {
      _11EXTENSIBLE_ELEMENTS.add(it.next());
    }
    _11EXTENSIBLE_ELEMENTS.add(Bpel11QNames.COMPENSATE);
  }
  
  public BpelGraph_1_1() {
    super();
    initBpel11();
  }
  
  public static QName get11QName(String s) {
    return new Bpel11QName(s);
  }
  
  public static StateFactory getRootStateFactory() {
    return new Bpel11ProcessState.Factory();
  }
  
  /*
   * Build the graph for BPEL4WS 1.1
   */
  private void initBpel11() {
    
    add11Activities();
    
    addQNameEdge("11ASSIGN","11COPY",Bpel11QNames.COPY);
    addStateFactory("11COPY",new Bpel11CopyState.Factory());
    addStateFactory("11FROM",new Bpel11FromState.Factory());
    addQNameEdge("11COPY","11FROM",Bpel11QNames.FROM);
    addStateFactory("11TO",new Bpel11ToState.Factory());
    addQNameEdge("11COPY","11TO",Bpel11QNames.TO);
    
    addStateFactory("11COMPENSATE",new Bpel11CompensateActivityState.Factory());
    
    addStateFactory("11PARTNERLINKS",new Bpel11PartnerLinksState.Factory());
    addStateFactory("11PARTNERLINK",new Bpel11PartnerLinkState.Factory());
    addQNameEdge("11PARTNERLINKS","11PARTNERLINK",Bpel11QNames.PARTNERLINK);
    
    addStateFactory("11PARTNERS",new Bpel11PartnersState.Factory());
    addStateFactory("11PARTNER",new Bpel11PartnerState.Factory());
    addQNameEdge("11PARTNERS","11PARTNER",Bpel11QNames.PARTNER);

    addStateFactory("11VARIABLES",new Bpel11VariablesState.Factory());
    addStateFactory("11VARIABLE",new Bpel11VariableState.Factory());
    addQNameEdge("11VARIABLES","11VARIABLE",Bpel11QNames.VARIABLE);
    
    addStateFactory("11CORRELATIONSETS",new Bpel11CorrelationSetsState.Factory());
    addStateFactory("11CORRELATIONSET",new Bpel11CorrelationSetState.Factory());
    addQNameEdge("11CORRELATIONSETS","11CORRELATIONSET",Bpel11QNames.CORRELATIONSET);

    addStateFactory("11CORRELATIONS",new Bpel11CorrelationsState.Factory());
    addStateFactory("11CORRELATION",new Bpel11CorrelationState.Factory());
    addQNameEdge("11CORRELATIONS","11CORRELATION",Bpel11QNames.CORRELATION);
        
    addStateFactory("11FAULTHANDLERS",new Bpel11FaultHandlersState.Factory());
    addStateFactory("11CATCH",new Bpel11CatchState.Factory());
    addStateFactory("11CATCHALL",new Bpel11CatchAllState.Factory());
    addQNameEdge("11FAULTHANDLERS","11CATCH",Bpel11QNames.CATCH);
    addQNameEdge("11FAULTHANDLERS","11CATCHALL",Bpel11QNames.CATCHALL);
    
    addStateFactory("11EVENTHANDLERS",new Bpel11EventHandlersState.Factory());
    addStateFactory("11ONMESSAGE",new Bpel11OnMessageState.Factory());
    addQNameEdge("11ONMESSAGE","11CORRELATIONS",Bpel11QNames.CORRELATIONS);
    addQNameEdge("11EVENTHANDLERS","11ONMESSAGE",Bpel11QNames.ONMESSAGE);
    addStateFactory("11ONALARM",new Bpel11OnAlarmState.Factory());
    addQNameEdge("11EVENTHANDLERS","11ONALARM",Bpel11QNames.ONALARM);
    
    addStateFactory("11COMPENSATIONHANDLER",new Bpel11CompensationHandlerState.Factory());
    
    addStateFactory("11PROCESS",new Bpel11ProcessState.Factory());
    addQNameEdge("11PROCESS","11PARTNERS",Bpel11QNames.PARTNERS);
    addQNameEdge("11PROCESS","11PARTNERLINKS",Bpel11QNames.PARTNERLINKS);
    addQNameEdge("11PROCESS","11VARIABLES",Bpel11QNames.VARIABLES);
    addQNameEdge("11PROCESS","11CORRELATIONSETS",Bpel11QNames.CORRELATIONSETS);
    addQNameEdge("11PROCESS","11FAULTHANDLERS",Bpel11QNames.FAULTHANDLERS);
    addQNameEdge("11PROCESS","11COMPENSATIONHANDLER",Bpel11QNames.COMPENSATIONHANDLER);
    addQNameEdge("11PROCESS","11EVENTHANDLERS",Bpel11QNames.EVENTHANDLERS);
    
    addQNameEdge("11SCOPE","11VARIABLES",Bpel11QNames.VARIABLES);
    addQNameEdge("11SCOPE","11CORRELATIONSETS",Bpel11QNames.CORRELATIONSETS);
    addQNameEdge("11SCOPE","11FAULTHANDLERS",Bpel11QNames.FAULTHANDLERS);
    addQNameEdge("11SCOPE","11COMPENSATIONHANDLER",Bpel11QNames.COMPENSATIONHANDLER);
    addQNameEdge("11SCOPE","11EVENTHANDLERS",Bpel11QNames.EVENTHANDLERS);
    
    add11ActivityTransitionsTo("11PROCESS");
    add11ActivityTransitionsTo("11COMPENSATIONHANDLER");
    add11ActivityTransitionsTo("11CATCH");
    addQNameEdge("11CATCH","11COMPENSATE",Bpel11QNames.COMPENSATE);
    add11ActivityTransitionsTo("11CATCHALL");
    addQNameEdge("11CATCHALL","11COMPENSATE",Bpel11QNames.COMPENSATE);
    addQNameEdge("11SWITCH","11CASE", Bpel11QNames.CASE);
    addQNameEdge("11SWITCH","11OTHERWISE", Bpel11QNames.OTHERWISE);
    addStateFactory("11CASE", new Bpel11CaseState.Factory());
    addStateFactory("11OTHERWISE", new Bpel11OtherwiseState.Factory());
    add11ActivityTransitionsTo("11CASE");
    add11ActivityTransitionsTo("11OTHERWISE");
    add11ActivityTransitionsTo("11SEQUENCE");
    add11ActivityTransitionsTo("11FLOW");
    add11ActivityTransitionsTo("11WHILE");
    add11ActivityTransitionsTo("11SCOPE");
    add11ActivityTransitionsTo("11ONMESSAGE");
    add11ActivityTransitionsTo("11ONALARM");
    
    addQNameEdge("11PICK","11ONMESSAGE",Bpel11QNames.ONMESSAGE);
    addQNameEdge("11PICK","11ONALARM",Bpel11QNames.ONALARM);    
    
    addQNameEdge("11INVOKE","11CORRELATIONS",Bpel11QNames.CORRELATIONS);
    addQNameEdge("11INVOKE","11CATCH",Bpel11QNames.CATCH);
    addQNameEdge("11INVOKE","11CATCHALL",Bpel11QNames.CATCHALL);
    addQNameEdge("11INVOKE","11COMPENSATIONHANDLER",Bpel11QNames.COMPENSATIONHANDLER);
    
    addQNameEdge("11RECEIVE","11CORRELATIONS",Bpel11QNames.CORRELATIONS);
    addQNameEdge("11REPLY","11CORRELATIONS",Bpel11QNames.CORRELATIONS);
    
    addStateFactory("11LINKS",new Bpel11LinksState.Factory());
    addStateFactory("11LINK",new Bpel11LinkState.Factory());
    addQNameEdge("11FLOW","11LINKS",Bpel11QNames.LINKS);
    addQNameEdge("11LINKS","11LINK",Bpel11QNames.LINK);
    
    addExtensibilityEdges();
    
    //addQNameEdge(START,"11PROCESS",Bpel11QNames.PROCESS);
  }
  
  private void addExtensibilityEdges() {
    addStateFactory("EXTENSIBILITY_BUCKET", new ExtensibilityBucketState.Factory());
    for(Iterator<QName> it = _11EXTENSIBLE_ELEMENTS.iterator();it.hasNext();) {
      QName qn = it.next();
      String nodename = "11" + qn.getLocalPart().toUpperCase();
      addOtherEdge(nodename,"EXTENSIBILITY_BUCKET",BpelProcessBuilder.BPEL4WS_NS);
    }
  }
  
  private void add11Activities() {
    addStateFactory("11SOURCE",new Bpel11LinkSourceState.Factory());
    addStateFactory("11TARGET",new Bpel11LinkTargetState.Factory());
    for (Iterator<QName> it = _11ACTIVITIES.keySet().iterator();it.hasNext();) {
      QName qn = it.next();
      String nodename = "11" + qn.getLocalPart().toUpperCase();
      addStateFactory(nodename, _11ACTIVITIES.get(qn));
      addQNameEdge(nodename,"11SOURCE",Bpel11QNames.SOURCE);
      addQNameEdge(nodename,"11TARGET",Bpel11QNames.TARGET);
    }
  }
  
  private void add11ActivityTransitionsTo(String source) {
    for (Iterator<QName> it = _11ACTIVITIES.keySet().iterator();it.hasNext();) {
      QName qn = it.next();
      String nodename = "11" + qn.getLocalPart().toUpperCase();
      addQNameEdge(source,nodename,qn);
    }
  }
}
