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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class CountLRUDehydrationPolicy implements DehydrationPolicy {

    /** Maximum age of a process before it is quiesced */
    private long _processMaxAge = 20 * 60 * 1000;
    /** Maximum process count before oldest ones get quiesced */
    private int _processMaxCount = 1000;

    public List<BpelProcess> markForDehydration(List<BpelProcess> runningProcesses) {
        ArrayList<BpelProcess> ripped = new ArrayList<BpelProcess>();

        if (_processMaxAge > 0) {
            // The oldies have to go first
            long now = System.currentTimeMillis();
            for (BpelProcess process : runningProcesses) {
                if (now - process.getLastUsed() > _processMaxAge) {
                    ripped.add(process);
                }
            }
        }

        // If it's not enough, other ones must be put to the axe
        if (runningProcesses.size() - ripped.size() > _processMaxCount) {
            runningProcesses.removeAll(ripped);
            Collections.sort(runningProcesses, new Comparator<BpelProcess>() {
                public int compare(BpelProcess p1, BpelProcess p2) {
                    if (p1.getLastUsed() > p2.getLastUsed()) return -1;
                    if (p1.getLastUsed() < p2.getLastUsed()) return 1;
                    return 0;
                }
            });
            for (int m = _processMaxCount; m < runningProcesses.size(); m++) {
                ripped.add(runningProcesses.get(m));
            }
        }

        return ripped;
    }

    public void setProcessMaxAge(long processMaxAge) {
        _processMaxAge = processMaxAge;
    }

    public void setProcessMaxCount(int processMaxCount) {
        _processMaxCount = processMaxCount;
    }
}
