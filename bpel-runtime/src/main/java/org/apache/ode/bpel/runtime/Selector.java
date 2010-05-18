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

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.CorrelationKeySet;
import org.apache.ode.utils.ObjectPrinter;

import java.io.Serializable;

public class Selector implements Serializable {
    private static final long serialVersionUID = 1L;

    public final PartnerLinkInstance plinkInstance;
    // here for the backward compatibility
    @SuppressWarnings("unused")
    public Object correlationKey = null;
    public CorrelationKeySet correlationKeySet;
    public final String opName;
    public final String messageExchangeId;
    public final int idx;
    public final boolean oneWay;
    public final String route;

    public Selector(int idx, PartnerLinkInstance plinkInstance, String opName, boolean oneWay, String mexId, CorrelationKeySet keySet, String route) {
        this.idx = idx;
        this.plinkInstance = plinkInstance;
        this.correlationKeySet = keySet;
        this.opName = opName;
        this.messageExchangeId = mexId;
        this.oneWay = oneWay;
        this.route = route;
    }

    public String toString() {
        return ObjectPrinter.toString(this, new Object[] {
                "plinkInstnace", plinkInstance,
                "ckeySet", correlationKeySet,
                "opName" ,opName,
                "oneWay", oneWay ? "yes" : "no",
                "mexId", messageExchangeId,
                "idx", Integer.valueOf(idx),
                "route", route
        });
    }
}
