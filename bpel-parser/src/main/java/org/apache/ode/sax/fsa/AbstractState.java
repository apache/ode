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

import org.apache.ode.sax.evt.Characters;
import org.apache.ode.sax.evt.SaxEvent;

public abstract class AbstractState implements State{

  public static final int EXTENSIBILITY_ELEMENT = 0;
  
  private ParseContext _pc;

  protected AbstractState(ParseContext pc) {
    super();
      _pc = pc;
  }
  
  public final ParseContext getParseContext() {
    return _pc;
  }
  
   public void handleChildCompleted(State pn) throws ParseException {
    switch (pn.getType()) {
      case EXTENSIBILITY_ELEMENT:
        // ignore.  This will get logged up front.
        break;
    default:
      throw new IllegalStateException(
          "Implementation error; unknown state " + pn.getClass().getName() +
          " encountered.");
    }
  }
  
  
  public void handleSaxEvent(SaxEvent se) throws ParseException {
    if (se.getType() == SaxEvent.CHARACTERS) {
      Characters c = (Characters) se;
      if (c.getContent().trim().length() != 0) {
        // TODO: Non-whitespace content -- throw exception.
      }
    } else {
      getParseContext().parseError(ParseError.ERROR,se,"","Unexpected SAX event " + 
          se.toString()); // TODO: Error key.
    }
  }
  
  public void done(){}
}
