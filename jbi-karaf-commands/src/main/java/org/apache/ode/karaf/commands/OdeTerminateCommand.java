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

import java.util.List;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.felix.gogo.commands.*;
import org.apache.ode.bpel.pmapi.TInstanceInfo;

@Command(scope = "ode", name = "terminate", description = "Terminate active ODE process instances")
public class OdeTerminateCommand extends OdeCommandsBase {

    private static final Logger __log = LoggerFactory.getLogger(OdeTerminateCommand.class);

    @Argument(name = "iids", description = "Instance IDs to terminate", multiValued = true)
    private static Long[] iids;

    @Option(name = "-a", aliases = "--all", description = "Terminate all active instances")
    private boolean terminateAll;

    private long timeoutInSeconds = 30;

    @Override
    protected Object doExecute() throws Exception {
        try {
            if (terminateAll) {
                List<TInstanceInfo> instances = getActiveInstances(timeoutInSeconds);
                if (instances != null) {
                    for (TInstanceInfo instance : instances) {
                        terminate(Long.parseLong(instance.getIid()),
                                timeoutInSeconds);
                    }
                }
            } else {
                if (iids == null) {
                    System.out.println("No instance ids to terminate");
                } else {
                    for (Long iid : iids) {
                        terminate(iid, timeoutInSeconds);
                    }
                }
            }
        } catch (TimeoutException e) {
            __log.error("Timed out after " + timeoutInSeconds + " seconds", e);
        }

        return null;
    }

}
