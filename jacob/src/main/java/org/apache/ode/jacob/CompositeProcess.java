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
package org.apache.ode.jacob;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


@SuppressWarnings("serial")
public final class CompositeProcess extends ChannelListener {
    private Set<ChannelListener> processes = new HashSet<ChannelListener>();

    public CompositeProcess() {
    }
    
    public Method getMethod(String methodName) {
        // Must call getMethod(String) on each of the getProcesses(). Use instanceof if necessary.
        throw new IllegalStateException("Calling getMethod() on a CompositeProcess is illegal.");
    }

    public Set<Method> getImplementedMethods() {
        // Must call getImplementedMethods on each of the getProcesses(). Use instanceof if necessary.
        throw new IllegalStateException("Calling getImplementationMethods() on a CompositeProcess is illegal.");
    }

    public Set<ChannelListener> getProcesses() {
        return Collections.unmodifiableSet(processes);
    }

    public CompositeProcess or(ChannelListener process) {
        processes.add(process);
        return this;
    }
}
