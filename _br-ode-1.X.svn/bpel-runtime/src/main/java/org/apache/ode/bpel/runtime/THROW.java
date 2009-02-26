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

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.o.OThrow;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Throw BPEL fault activity.
 */
class THROW extends ACTIVITY {
    private static final long serialVersionUID = 1L;
    private static final Log __log = LogFactory.getLog(ACTIVITY.class);

    private OThrow _othrow;

    public THROW(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
        super(self, scopeFrame, linkFrame);
        _othrow = (OThrow) self.o;
    }

    public void run() {
        FaultData fault = null;
        if(_othrow.faultVariable != null){
            try {
                sendVariableReadEvent(_scopeFrame.resolve(_othrow.faultVariable));
                Node faultVariable = fetchVariableData(_scopeFrame.resolve(_othrow.faultVariable), false);
                fault = createFault(_othrow.faultName, (Element)faultVariable,_othrow.faultVariable.type,_othrow);
            } catch (FaultException e) {
                // deal with this as a fault (just not the one we hoped for)
                __log.error(e);
                fault = createFault(e.getQName(), _othrow);
            }
        }else{
            fault = createFault(_othrow.faultName, _othrow);
        }

        _self.parent.completed(fault, CompensationHandler.emptySet());
    }
}
