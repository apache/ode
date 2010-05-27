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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.runtime.channels.FaultData;


/**
 * FaultActivity
 */
class RETHROW extends ACTIVITY {
  private static final long serialVersionUID = -6433171659586530126L;
  private static final Log __log = LogFactory.getLog(RETHROW.class);

  RETHROW(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
    super(self, scopeFrame, linkFrame);
  }

  public void run() {
    // find the faultData in the scope stack
    FaultData fault = _scopeFrame.getFault();
    if(fault == null){
      String msg = "Attempting to execute 'rethrow' activity with no visible fault in scope.";
      __log.error(msg);
      throw new InvalidProcessException(msg);
    }

    _self.parent.completed(fault,CompensationHandler.emptySet());
  }
}
