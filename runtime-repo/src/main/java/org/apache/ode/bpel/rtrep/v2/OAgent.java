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
package org.apache.ode.bpel.o;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for active BPEL agents.
 */
public class OAgent extends OBase {
  private static final long serialVersionUID = 6391205087340931483L;

  /** Links entering this agent. */
  public final Set<OLink> incomingLinks = new HashSet<OLink>();

  /** Links exiting this agent. */
  public final Set<OLink> outgoingLinks = new HashSet<OLink>();

  /** Variables read from. */
  public final Set<OScope.Variable> variableRd = new HashSet<OScope.Variable>();

  /** Variables written to. */
  public final Set<OScope.Variable> variableWr = new HashSet<OScope.Variable>();

  /** The children of this agent. */
  public final Set<OAgent> nested = new HashSet<OAgent>();

  public OAgent(OProcess owner) {
    super(owner);
  }
}
