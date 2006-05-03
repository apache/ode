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
package org.apache.ode.sax.fsa.bpel_1_1;

import org.apache.ode.sax.fsa.*;
import org.apache.ode.sax.evt.SaxEvent;
import org.apache.ode.sax.evt.StartElement;


/**
 * <p>
 * Bucket state to capture schema-level extensibility elements.  Extensibility
 * attributes must be handled elsewhere.
 * </p>
 */
class ExtensibilityBucketState extends AbstractState {
  
  private static final StateFactory _factory = new Factory();
  
  ExtensibilityBucketState(StartElement se, ParseContext pc) {
    super(pc);
  }
  
  
  static class Factory implements StateFactory {
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new ExtensibilityBucketState(se,pc);
    }
  }

  public void handleSaxEvent(SaxEvent se) throws ParseException{
    /*
     * For the moment, this is a no-op implementation, but if supporting extensions
     * is desired, those extensions can be hooked from here.  Ideally, we'd have
     * some kind of registry implementation that routes SaxEvent streams based on
     * URI or some other scheme.  However, for the moment, we don't have any use
     * cases.  WS-BPEL 2.0 extensibility can be implemented according to the spec,
     * once that's settled.
     */
  }

  public StateFactory getFactory() {
    return _factory;
  }

  public int getType() {
    return EXTENSIBILITY_ELEMENT;
  }
}
