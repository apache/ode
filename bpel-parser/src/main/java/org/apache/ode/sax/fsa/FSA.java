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
package org.apache.ode.sax.fsa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.sax.evt.Characters;
import org.apache.ode.sax.evt.EndElement;
import org.apache.ode.sax.evt.SaxEvent;
import org.apache.ode.sax.evt.StartElement;

import java.util.Stack;

public class FSA {
    
  /*
   * The path in the graph, at present.  As we exit elements, we'll move backwards
   * in the path.
   */
  private Stack<ParseState> _state;
  private String _start;
  private State _startState;
  private GraphProvider _graphProvider;
  private ParseContext _pc;
  
  private static final Log __log = LogFactory.getLog(FSA.class);
  
  protected FSA(GraphProvider provider) {
    _state = new Stack<ParseState>();
    _graphProvider = provider;
  }
  
  protected void onStateChange(String fromState, String toState) throws ParseException {}
  
  public void setGraphProvider(GraphProvider graphProvider){
  	_graphProvider = graphProvider;
  }
  
  public void setStart(String name, State node) {
    _start = name;
    _startState = node;
  }
  
  public void setParseContext(ParseContext pc) {
    _pc = pc;
  }
  
  protected ParseContext getParseContext() {
    return _pc;
  }
  
  public void begin() {
    if (__log.isDebugEnabled()) {
      __log.debug("FSA.begin()");
    }
    if (_start == null || _startState == null) {
      throw new IllegalStateException("A start state must be set.");
    }
    _state.push(new ParseState(_start,_startState));
  }
  
  public void end() {
    if (__log.isDebugEnabled()) {
      __log.debug("FSA.end()");
    }
  }
  
  public State getStart() {
    return _startState;
  }
  
    
  private ParseState current() {
    return _state.peek();
  }
    
  public void handleSaxEvent(SaxEvent se) throws ParseException {
    if (__log.isDebugEnabled()) {
      String msg;
      if (se.getType() == SaxEvent.CHARACTERS && ((Characters)se).getContent().trim().length() == 0) {
        msg = "<<whitespace>>";
      } else {
        msg = se.toString();
      }
      __log.debug("FSA handling " + msg + " from state " + current().stateName +
          " at depth " + current().depth);
    }
    switch (se.getType()) {
    case SaxEvent.CHARACTERS:
      current().state.handleSaxEvent(se);
      break;
    case SaxEvent.START_ELEMENT:
      StartElement ste = (StartElement) se;
      String target = _graphProvider.getQNameEdge(current().stateName,ste.getName());
      if (target == null) {
        target = _graphProvider.getOtherEdge(current().stateName, ste.getName().getNamespaceURI());
      }
      if (target != null) {
        StateFactory factory = _graphProvider.getStateFactory(target);
        if(factory == null)
          throw new IllegalStateException("No state factory for target '" + target + "'");
        String old = current().stateName;
        _state.push(new ParseState(target,factory.newInstance(ste,_pc)));
        onStateChange(old, target);
      } else {
        ++current().depth;
        current().state.handleSaxEvent(ste);
      }
      break;
    case SaxEvent.END_ELEMENT:
      EndElement ee = (EndElement)se;
      if (current().isDone()) {
        State pn =  current().state;   
        pn.done();
        _state.pop();
        current().state.handleChildCompleted(pn);
      } else {
        --current().depth;
        current().state.handleSaxEvent(ee);
      }
      break;
    }
    if (__log.isDebugEnabled()) {
      __log.debug("new state is " + current().stateName + " at depth " + current().depth);
    }
    
  }
  
   
  private class ParseState {
  
    String stateName;
    State state;
    int depth;
 
    ParseState(String name, State node) {
      stateName = name;
      state = node;
      depth = 0;
    }

    void sink() {
      ++depth;
    }
    
    void rise() {
      --depth;
      if (depth < 0) {
        // bitch about it!
      }
    }
    
    boolean isDone() {
      return depth == 0;
    }
  }

}
