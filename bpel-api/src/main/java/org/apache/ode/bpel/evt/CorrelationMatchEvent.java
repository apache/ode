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
package org.apache.ode.bpel.evt;

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.CorrelationKeySet;

import javax.xml.namespace.QName;

/**
 * Correlation matched a process instance on inbound message.
 */
public class CorrelationMatchEvent extends ProcessMessageExchangeEvent {
    private static final long serialVersionUID = 1L;

    // left out for backward-compatibility
    private CorrelationKey _correlationKey;
    private CorrelationKeySet _correlationKeySet;

    public CorrelationMatchEvent(QName processName, QName processId, Long processInstanceId, CorrelationKeySet correlationKeySet) {
        super(PROCESS_INPUT, processName, processId, processInstanceId);
        _correlationKeySet = correlationKeySet;
    }

    public CorrelationKeySet getCorrelationKeySet() {
        // backward compatibility; add up
        if (_correlationKey != null) {
            if( _correlationKeySet == null ) {
                _correlationKeySet = new CorrelationKeySet();
            }
            if(!_correlationKeySet.contains(_correlationKey)) {
                _correlationKeySet.add(_correlationKey);
            }
        }

        return _correlationKeySet;
    }

    public void setCorrelationKey(CorrelationKeySet correlationKeySet) {
        _correlationKeySet = correlationKeySet;
    }

    public TYPE getType() {
        return TYPE.correlation;
    }
}