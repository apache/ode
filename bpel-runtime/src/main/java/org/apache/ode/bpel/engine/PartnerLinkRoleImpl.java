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
package org.apache.ode.bpel.engine;

import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.rapi.PartnerLinkModel;

abstract class PartnerLinkRoleImpl {
    protected PartnerLinkModel _plinkDef;
    protected EndpointReference _initialEPR;
    protected ODEProcess _process;
    protected Contexts _contexts;

    PartnerLinkRoleImpl(ODEProcess process, PartnerLinkModel plink) {
        _plinkDef = plink;
        _process = process;
        _contexts = _process._contexts;
    }
    String getPartnerLinkName() {
        return _plinkDef.getName();
    }
    /**
     * Get the initial value of this role's EPR. This value is obtained from
     * the integration layer when the process is enabled on the server.
     *
     * @return initial epr
     */
    EndpointReference getInitialEPR() {
        return _initialEPR;
    }

}
