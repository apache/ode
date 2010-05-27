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
package org.apache.ode.bpel.iapi;

import org.apache.ode.bpel.bdi.breaks.Breakpoint;

/**
 * Support for debugging a process:
 * breakpoints, suspend, continue, step and terminate.
 * <br/>
 * This object is associated to a particular process definition.
 * <p>
 * iid: Process instance id.
 * </p>
 *
 * @author mriou
 */
public interface DebuggerContext {

    boolean step(Long iid);

    boolean resume(final Long iid);

    void suspend(final Long iid);

    void terminate(final Long iid);

    Breakpoint[] getGlobalBreakpoints();

    Breakpoint[] getBreakpoints(Long iid);

    void addGlobalBreakpoint(Breakpoint breakpoint);

    void addBreakpoint(Long pid, Breakpoint breakpoint);

    void removeGlobalBreakpoint(Breakpoint breakpoint);

    void removeBreakpoint(Long iid, Breakpoint breakpoint);

    /**
     * @return the process model.
     * Currently an {@link org.apache.ode.bpel.o.OProcess}
     * However it is not guaranteed that it will remain an OProcess
     * in future versions of ODE or for different types
     * of process lanaguage than BPEL.
     */
    public Object getProcessModel();
}
