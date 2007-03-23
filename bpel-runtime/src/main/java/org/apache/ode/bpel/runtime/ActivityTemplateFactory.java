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
package org.apache.ode.bpel.runtime;

import org.apache.ode.bpel.o.*;

/**
 * Factory for creating activity template objects.
 */
public class ActivityTemplateFactory {
  
  public ACTIVITY createInstance(OActivity type, ActivityInfo ai, ScopeFrame scopeFrame, LinkFrame linkFrame) {
    if (type instanceof OThrow) return new THROW(ai, scopeFrame, linkFrame);
    if (type instanceof OEmpty) return new EMPTY(ai, scopeFrame, linkFrame);
    if (type instanceof OAssign) return new ASSIGN(ai, scopeFrame, linkFrame);
    if (type instanceof OCompensate) return new COMPENSATE(ai, scopeFrame, linkFrame);
    if (type instanceof OFlow) return new FLOW(ai, scopeFrame, linkFrame);
    if (type instanceof OInvoke) return new INVOKE(ai, scopeFrame, linkFrame);
    if (type instanceof OPickReceive) return new PICK(ai, scopeFrame, linkFrame);
    if (type instanceof OReply) return new REPLY(ai, scopeFrame, linkFrame);
    if (type instanceof ORethrow) return new RETHROW(ai, scopeFrame, linkFrame);
    if (type instanceof OScope) return new SCOPEACT(ai, scopeFrame, linkFrame);
    if (type instanceof OSequence) return new SEQUENCE(ai, scopeFrame, linkFrame);
    if (type instanceof OSwitch) return new SWITCH(ai, scopeFrame, linkFrame);
    if (type instanceof OTerminate) return new TERMINATE(ai, scopeFrame, linkFrame);
    if (type instanceof OWait) return new WAIT(ai, scopeFrame, linkFrame);
    if (type instanceof OWhile) return new WHILE(ai, scopeFrame, linkFrame);
    if (type instanceof OForEach) return new FOREACH(ai, scopeFrame, linkFrame);
    if (type instanceof ORepeatUntil) return new REPEATUNTIL(ai,scopeFrame,linkFrame);

    throw new IllegalArgumentException("Unknown type: " + type);
  }

}
