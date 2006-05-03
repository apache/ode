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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.parser.BpelProcessBuilder;
import org.apache.ode.sax.fsa.AbstractGraphProvider;
import org.apache.ode.sax.fsa.StateFactory;

public class BpelGraph_2_0 extends AbstractGraphProvider implements Bpel20QNames {

  private static final HashMap<QName,StateFactory>  _ACTIVITIES = new HashMap<QName,StateFactory>();
  static {
    _ACTIVITIES.put(Bpel20QNames.ASSIGN,new BpelAssignActivityState.Factory());
    _ACTIVITIES.put(Bpel20QNames.EMPTY,new BpelEmptyActivityState.Factory());
    _ACTIVITIES.put(Bpel20QNames.INVOKE,new BpelInvokeActivityState.Factory());
    _ACTIVITIES.put(Bpel20QNames.RECEIVE,new BpelReceiveActivityState.Factory());
    _ACTIVITIES.put(Bpel20QNames.REPLY,new BpelReplyActivityState.Factory());
    _ACTIVITIES.put(Bpel20QNames.WAIT,new BpelWaitActivityState.Factory());
    _ACTIVITIES.put(Bpel20QNames.THROW,new BpelThrowActivityState.Factory());
    _ACTIVITIES.put(Bpel20QNames.COMPENSATE,new BpelCompensateActivityState.Factory());
    _ACTIVITIES.put(Bpel20QNames.RETHROW,new BpelRethrowActivityState.Factory());
    _ACTIVITIES.put(Bpel20QNames.EXIT,new BpelExitActivityState.Factory());
    _ACTIVITIES.put(Bpel20QNames.FLOW,new BpelFlowActivityState.Factory());
    _ACTIVITIES.put(Bpel20QNames.SWITCH,new BpelSwitchActivityState.Factory());
    _ACTIVITIES.put(Bpel20QNames.WHILE,new BpelWhileActivityState.Factory());
    _ACTIVITIES.put(Bpel20QNames.SEQUENCE,new BpelSequenceActivityState.Factory());
    _ACTIVITIES.put(Bpel20QNames.PICK,new BpelPickActivityState.Factory());
    _ACTIVITIES.put(Bpel20QNames.SCOPE,new BpelScopeState.Factory());
    _ACTIVITIES.put(Bpel20QNames.IF, new BpelIfActivityState.Factory());
  }
  
    private static final HashSet<QName> _20EXTENSIBLE_ELEMENTS = new HashSet<QName>();
    static {
        //
        // Aligned with Sept 6th, 2005 draft WSBPEL
        //
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.ASSIGN);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.COPY);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.CATCH);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.CATCHALL);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.CONDITION);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.CORRELATION);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.CORRELATIONS);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.CORRELATIONSETS);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.COMPENSATE);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.COMPENSATIONHANDLER);
        // _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.DOCUMENTATION);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.ELSE);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.ELSEIF);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.EMPTY);
        // not defined yet
        // _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.EXTENSION_ACTIVITY);
        // not defined yet
        // _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.EXTENSIBLE_ASSIGN);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.EVENTHANDLERS);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.EXIT);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.FAULTHANDLERS);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.FLOW);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.FOR);
        // not defined yet
        // _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.FOREACH);

        // Creates a problem with literal assignment: 
        //_20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.FROM);
        
        // _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.FROM_PART);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.IF);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.IMPORT);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.INVOKE);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.JOIN_CONDITION);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.LINK);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.LINKS);
        // _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.LITERAL);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.ONALARM);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.ONEVENT);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.ONMESSAGE);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.PARTNERLINK);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.PARTNERLINKS);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.PICK);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.PROCESS);
        // _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.PROPERTY);
        // _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.PROPERTY_ALIAS);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.QUERY);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.RECEIVE);
        // not defined yet
        //_20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.REPEAT_UNTIL);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.REPLY);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.REPEAT_EVERY);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.RETHROW);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.SEQUENCE);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.SERVICE_REF);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.SCOPE);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.SOURCE);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.SOURCES);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.TARGET);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.TARGETS);
        // not defined yet
        // _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.TERMINATE);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.TERMINATIONHANDLER);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.THEN);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.TRANSITION_CONDITION);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.THROW);
        
        // May create problems with expression languages:
        // _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.TO);
        // _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.TO_PART);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.UNTIL);
        // not defined yet
        // _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.VALIDATE);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.VARIABLE);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.VARIABLES);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.WAIT);
        _20EXTENSIBLE_ELEMENTS.add(Bpel20QNames.WHILE);

        // Add all activities.
        for (Iterator<QName> it = _ACTIVITIES.keySet().iterator(); it.hasNext();) {
            _20EXTENSIBLE_ELEMENTS.add(it.next());
        }
    }

  public BpelGraph_2_0() {
    super();
    initBpel();
  }
  
  public static QName get20QName(String s) {
    return new Bpel20QName(s);
  }
  
  public static StateFactory getRootStateFactory() {
    return new BpelProcessState.Factory();
  }
  /*
   * Build the graph for BPEL4WS 2.0
   */
  private void initBpel() {
    
    addActivities();
    
    addQNameEdge("ASSIGN","COPY",Bpel20QNames.COPY);
    addStateFactory("COPY",new BpelCopyState.Factory());
    addStateFactory("FROM",new BpelFromState.Factory());
    addStateFactory("EXPR", new BpelExpressionState.Factory());
    addStateFactory("QUERY", new BpelQueryState.Factory());
    
    addQNameEdge("COPY","FROM",Bpel20QNames.FROM);
    // TODO Add edge to service-ref
    addStateFactory("TO",new BpelToState.Factory());
    addQNameEdge("COPY","TO",Bpel20QNames.TO);
    
    addStateFactory("IMPORT", new BpelImportState.Factory());
    
    addStateFactory("PARTNERLINKS",new BpelPartnerLinksState.Factory());
    addStateFactory("PARTNERLINK",new BpelPartnerLinkState.Factory());
    addQNameEdge("PARTNERLINKS","PARTNERLINK",Bpel20QNames.PARTNERLINK);
    
    addStateFactory("VARIABLES",new BpelVariablesState.Factory());
    addStateFactory("VARIABLE",new BpelVariableState.Factory());
    addQNameEdge("VARIABLES","VARIABLE",Bpel20QNames.VARIABLE);
    
    addStateFactory("CORRELATIONSETS",new BpelCorrelationSetsState.Factory());
    addStateFactory("CORRELATIONSET",new BpelCorrelationSetState.Factory());
    addQNameEdge("CORRELATIONSETS","CORRELATIONSET",Bpel20QNames.CORRELATIONSET);

    addStateFactory("CORRELATIONS",new BpelCorrelationsState.Factory());
    addStateFactory("CORRELATION",new BpelCorrelationState.Factory());
    addQNameEdge("CORRELATIONS","CORRELATION",Bpel20QNames.CORRELATION);
        
    addStateFactory("FAULTHANDLERS",new BpelFaultHandlersState.Factory());
    addStateFactory("CATCH",new BpelCatchState.Factory());
    addStateFactory("CATCHALL",new BpelCatchAllState.Factory());
    addQNameEdge("FAULTHANDLERS","CATCH",Bpel20QNames.CATCH);
    addQNameEdge("FAULTHANDLERS","CATCHALL",Bpel20QNames.CATCHALL);
    
    addStateFactory("EVENTHANDLERS",new BpelEventHandlersState.Factory());
    
    addStateFactory("ONEVENT",new BpelOnEventState.Factory());
    addQNameEdge("ONEVENT","CORRELATIONS",Bpel20QNames.CORRELATIONS);
    addQNameEdge("EVENTHANDLERS","ONEVENT",Bpel20QNames.ONEVENT);
    addStateFactory("ONALARM",new BpelOnAlarmState.Factory());
    addQNameEdge("EVENTHANDLERS","ONALARM",Bpel20QNames.ONALARM);
    
    addStateFactory("COMPENSATIONHANDLER",new BpelCompensationHandlerState.Factory());
    addStateFactory("TERMINATIONHANDLER",new BpelTerminationHandlerState.Factory());
    
    addStateFactory("20PROCESS",new BpelProcessState.Factory());
    addQNameEdge("20PROCESS","IMPORT",Bpel20QNames.IMPORT);
    addQNameEdge("20PROCESS","PARTNERLINKS",Bpel20QNames.PARTNERLINKS);
    addQNameEdge("20PROCESS","VARIABLES",Bpel20QNames.VARIABLES);
    addQNameEdge("20PROCESS","CORRELATIONSETS",Bpel20QNames.CORRELATIONSETS);
    addQNameEdge("20PROCESS","FAULTHANDLERS",Bpel20QNames.FAULTHANDLERS);
    addQNameEdge("20PROCESS","COMPENSATIONHANDLER",Bpel20QNames.COMPENSATIONHANDLER);
    addQNameEdge("20PROCESS","TERMINATIONHANDLER",Bpel20QNames.TERMINATIONHANDLER);
    addQNameEdge("20PROCESS","EVENTHANDLERS",Bpel20QNames.EVENTHANDLERS);
    
    addQNameEdge("SCOPE","VARIABLES",Bpel20QNames.VARIABLES);
    addQNameEdge("SCOPE","PARTNERLINKS",Bpel20QNames.PARTNERLINKS);
    addQNameEdge("SCOPE","CORRELATIONSETS",Bpel20QNames.CORRELATIONSETS);
    addQNameEdge("SCOPE","FAULTHANDLERS",Bpel20QNames.FAULTHANDLERS);
    addQNameEdge("SCOPE","COMPENSATIONHANDLER",Bpel20QNames.COMPENSATIONHANDLER);
    addQNameEdge("SCOPE","TERMINATIONHANDLER",Bpel20QNames.TERMINATIONHANDLER); 
    addQNameEdge("SCOPE","EVENTHANDLERS",Bpel20QNames.EVENTHANDLERS);
    
    addStateFactory("CONDITION", new BpelExpressionState.Factory());
    addStateFactory("FOR", new BpelForState.Factory());
    addStateFactory("UNTIL", new BpelUntilState.Factory());
    addStateFactory("REPEATEVERY", new BpelRepeatEveryState.Factory());
    
    addActivityTransitionsTo("20PROCESS");
    addActivityTransitionsTo("COMPENSATIONHANDLER");
    addActivityTransitionsTo("TERMINATIONHANDLER");
    addActivityTransitionsTo("CATCH");
    addActivityTransitionsTo("CATCHALL");
    addQNameEdge("SWITCH","CASE", Bpel20QNames.CASE);
    addQNameEdge("SWITCH","OTHERWISE", Bpel20QNames.OTHERWISE);
    addStateFactory("CASE", new BpelSwitchActivityState.CaseState.Factory());
    addQNameEdge("CASE", "CONDITION", Bpel20QNames.CONDITION);
    addStateFactory("OTHERWISE", new BpelSwitchActivityState.OtherwiseState.Factory());
    
    addStateFactory("IF", new BpelIfActivityState.Factory());
    addStateFactory("THEN", new BpelIfActivityState.ThenState.Factory());
    addStateFactory("ELSEIF", new BpelIfActivityState.ElseIfState.Factory());
    addStateFactory("ELSE", new BpelIfActivityState.ElseState.Factory());
    addQNameEdge("IF", "CONDITION", Bpel20QNames.CONDITION);
    addQNameEdge("IF", "THEN", Bpel20QNames.THEN);
    addQNameEdge("IF", "ELSEIF", Bpel20QNames.ELSEIF);
    addQNameEdge("IF", "ELSE", Bpel20QNames.ELSE);
    addQNameEdge("ELSEIF", "CONDITION", Bpel20QNames.CONDITION);
    
    addQNameEdge("WHILE", "CONDITION", Bpel20QNames.CONDITION);
    addQNameEdge("WAIT", "FOR", Bpel20QNames.FOR);
    addQNameEdge("WAIT", "UNTIL", Bpel20QNames.UNTIL);
    
    addActivityTransitionsTo("THEN");
    addActivityTransitionsTo("ELSEIF");
    addActivityTransitionsTo("ELSE");
    addActivityTransitionsTo("CASE");
    addActivityTransitionsTo("OTHERWISE");
    addActivityTransitionsTo("SEQUENCE");
    addActivityTransitionsTo("FLOW");
    addActivityTransitionsTo("WHILE");
    addActivityTransitionsTo("SCOPE");
    addActivityTransitionsTo("ONMESSAGE");
    addActivityTransitionsTo("ONEVENT");
    addActivityTransitionsTo("ONALARM");
    
    // onMessage is only used by pick in 2.0.
    addStateFactory("ONMESSAGE",new BpelOnMessageState.Factory());
    addQNameEdge("ONMESSAGE","CORRELATIONS",Bpel20QNames.CORRELATIONS);
    addQNameEdge("PICK","ONMESSAGE",Bpel20QNames.ONMESSAGE);
    addQNameEdge("PICK","ONALARM",Bpel20QNames.ONALARM);
    
    addQNameEdge("ONALARM", "FOR", Bpel20QNames.FOR);
    addQNameEdge("ONALARM", "UNTIL", Bpel20QNames.UNTIL);
    addQNameEdge("ONALARM", "REPEATEVERY", Bpel20QNames.REPEAT_EVERY);
    
    addQNameEdge("INVOKE","CORRELATIONS",Bpel20QNames.CORRELATIONS);
    addQNameEdge("INVOKE","CATCH",Bpel20QNames.CATCH);
    addQNameEdge("INVOKE","CATCHALL",Bpel20QNames.CATCHALL);
    addQNameEdge("INVOKE","COMPENSATIONHANDLER",Bpel20QNames.COMPENSATIONHANDLER);
    
    addQNameEdge("RECEIVE","CORRELATIONS",Bpel20QNames.CORRELATIONS);
    addQNameEdge("REPLY","CORRELATIONS",Bpel20QNames.CORRELATIONS);
    
    addStateFactory("LINKS",new BpelLinksState.Factory());
    addStateFactory("LINK",new BpelLinkState.Factory());
    addQNameEdge("FLOW","LINKS",Bpel20QNames.LINKS);
    addQNameEdge("LINKS","LINK",Bpel20QNames.LINK);
    
    //addQNameEdge(START,"PROCESS",Bpel20QNames.PROCESS);
    
    addExtensibilityEdges();
  }

  private void addExtensibilityEdges() {
      addStateFactory("EXTENSIBILITY_BUCKET", new ExtensibilityBucketState.Factory());
      addOtherEdge("20PROCESS","EXTENSIBILITY_BUCKET",BpelProcessBuilder.WSBPEL2_0_NS);
      for(Iterator<QName> it = _20EXTENSIBLE_ELEMENTS.iterator();it.hasNext();) {
        QName qn = it.next();
        String nodename = qn.getLocalPart().toUpperCase();
        addOtherEdge(nodename,"EXTENSIBILITY_BUCKET",BpelProcessBuilder.WSBPEL2_0_NS);
      }
    }
  
  private void addActivities() {
    addStateFactory("SOURCES",new BpelLinkSourcesState.Factory());
    addStateFactory("SOURCE",new BpelLinkSourceState.Factory());
    addStateFactory("TARGETS",new BpelLinkTargetsState.Factory());
    addStateFactory("JOINCONDITION", new BpelExpressionState.Factory());
    addStateFactory("TARGET",new BpelLinkTargetState.Factory());
    addStateFactory("TCONDITION",  new BpelExpressionState.Factory());
    addQNameEdge("TARGETS", "JOINCONDITION", Bpel20QNames.JOIN_CONDITION);
    addQNameEdge("TARGETS", "TARGET", Bpel20QNames.TARGET);
    addQNameEdge("SOURCE", "TCONDITION", Bpel20QNames.TRANSITION_CONDITION);
    addQNameEdge("SOURCES", "SOURCE", Bpel20QNames.SOURCE);
    
    for (Iterator<QName> it = _ACTIVITIES.keySet().iterator();it.hasNext();) {
      QName qn = it.next();
      String nodename = qn.getLocalPart().toUpperCase();
      addStateFactory(nodename, _ACTIVITIES.get(qn));
      addQNameEdge(nodename,"SOURCES",Bpel20QNames.SOURCES);
      addQNameEdge(nodename,"TARGETS",Bpel20QNames.TARGETS);
    }
  }
  
  private void addActivityTransitionsTo(String source) {
    for (Iterator<QName> it = _ACTIVITIES.keySet().iterator();it.hasNext();) {
      QName qn = it.next();
      String nodename = qn.getLocalPart().toUpperCase();
      addQNameEdge(source,nodename,qn);
    }
  }
}
