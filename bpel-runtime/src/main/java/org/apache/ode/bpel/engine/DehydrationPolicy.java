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

import java.util.List;

/**
 * Defines a policy to dehydrate running processes based on a limit in total
 * process count or processes that haven't been used for a while.
 * @author Matthieu Riou <mriou at apache dot org>
 */
public interface DehydrationPolicy {

    /**
     * Checks the currently running processes and marks some of them for
     * dehydration according to a specifically configured policy. The
     * returned processes will be dehydrated by the engine.
     * @param runningProcesses all running (currently hydrated) processes
     * @return processes elected for dehydration
     */
    List<BpelProcess> markForDehydration(List<BpelProcess> runningProcesses);
}
