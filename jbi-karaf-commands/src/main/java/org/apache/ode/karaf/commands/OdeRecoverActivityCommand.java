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

package org.apache.ode.karaf.commands;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.gogo.commands.*;

@Command(scope = "ode", name = "recoverActivity", description = "Recover a failed BPEL activity in ODE")
public class OdeRecoverActivityCommand extends OdeCommandsBase {
    private static final Log __log = LogFactory.getLog(OdeRecoverActivityCommand.class);
    
    private static final String RETRY = "retry";
    private static final String FAULT = "fault";
    private static final String CANCEL = "cancel";
    
    @Argument(name="iid", description="Instance ID", index=0, required=true)
    private Long instanceId;
    
    @Argument(name="aid", description="Activity IDs to attempt recovery", index=1, required=true, multiValued=true)
    private Long[] activityIds;
    
    @Option(name="-r", aliases="--retry", description="Retry the activity (default=retry)")
    private boolean retry = false;
    
    @Option(name="-f", aliases="--fault", description="Fault the activity (default=retry)")
    private boolean fault = false;
    
    @Option(name="-c", aliases="--cancel", description="Cancel the activity (default=retry)")
    private boolean cancel = false;
    
    @Override
    protected Object doExecute() throws Exception {
        /*
         * Unfortunatly there isn't a way to make options mutually exclusive, so we give precedence in this order
         * retry > fault > cancel
         */
        String action = null;
        
        if (retry) {
            action = RETRY;
        } else if (fault) {
            action = FAULT;
        } else if (cancel) {
            action = CANCEL;
        } else {
            // Also make retry the default action
            action = RETRY;
        }
        
        for (Long aiid : activityIds) {
            try {
                recoverActivity(instanceId, aiid, action, 30);
            } catch (Exception e) {
                __log.error("An error occuring trying to recover activity", e);
            }
        }
        
        return null;
    }

}
