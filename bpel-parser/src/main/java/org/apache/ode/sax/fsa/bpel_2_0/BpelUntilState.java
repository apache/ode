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

import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.StartElement;

class BpelUntilState extends BpelExpressionState {
  /**
   * @param se
   * @throws ParseException
   */
  public BpelUntilState(StartElement se, ParseContext pc) throws ParseException {
    super(se,pc);
  }
  /**
   * @see org.apache.ode.sax.fsa.State#getType()
   */
  public int getType() {
    return BPEL_UNTIL;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelUntilState(se,pc);
    }
  }

}
