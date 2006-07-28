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

package org.apache.ode.bpel.dd;

import org.apache.ode.bpel.o.OBase;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OScope;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates event filters declared on scopes.
 */
public class EventFilterValidator implements DDValidator {

  public void validate(TDeploymentDescriptor dd, OProcess process) throws DDValidationException {
    List<String> scopeNames = getProcessScopeNames(process);
    if (dd.getProcessEvents() != null && dd.getProcessEvents().getScopeEventsList().size() > 0) {
      for (TDeploymentDescriptor.ProcessEvents.ScopeEvents scopeEvents : dd.getProcessEvents().getScopeEventsList()) {
        if (!scopeNames.contains(scopeEvents.getName()))
          throw new DDValidationException("Event filters are declared for scope " +
                  scopeEvents.getName() + " in your deployment descriptor but this scope " +
                  "can't be found in the process definition.");
      }
    }
  }

  private List<String> getProcessScopeNames(OProcess process) {
    ArrayList<String> names = new ArrayList<String>();
    for (OBase oBase : process.getChildren()) {
      if (oBase instanceof OScope) names.add(((OScope)oBase).name);
    }
    return names;
  }
}
