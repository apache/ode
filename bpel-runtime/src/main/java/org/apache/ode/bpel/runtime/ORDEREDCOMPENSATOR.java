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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.jacob.ReceiveProcess;
import org.apache.ode.jacob.Synch;


/**
 * Serially activates a list of compensations in order.
 */
class ORDEREDCOMPENSATOR extends BpelJacobRunnable  {
    private static final long serialVersionUID = -3181661355085428370L;

    private static final Log __log = LogFactory.getLog(ORDEREDCOMPENSATOR.class);

    private List<CompensationHandler> _compensations;
    private Synch _ret;

    public ORDEREDCOMPENSATOR(List<CompensationHandler> compensations, Synch ret) {
        _compensations = compensations;
        _ret = ret;
    }

    public void run() {
        if (_compensations.isEmpty()) {
            _ret.ret();
        } else {
            Synch r = newChannel(Synch.class);
            CompensationHandler cdata = _compensations.remove(0);
            cdata.compChannel.compensate(r);
            object(new ReceiveProcess<Synch>(r, new Synch() {
                public void ret() {
                    instance(ORDEREDCOMPENSATOR.this);
                }
            }) {
                private static final long serialVersionUID = 7173916663479205420L;
            });
        }
    }

    protected Log log() {
        return __log;
    }

    public String toString() {
        return new StringBuffer("ORDEREDCOMPENSATOR(comps=")
            .append(_compensations)
            .append(")")
            .toString();
    }

}
