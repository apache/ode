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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.gogo.commands.*;
import org.apache.ode.bpel.pmapi.TInstanceInfo;

@Command(scope = "ode", name = "resume", description = "Resume suspended ODE process instances")
public class OdeResumeCommand extends OdeCommandsBase {

    private static final Log __log = LogFactory.getLog(OdeResumeCommand.class);

    @Argument(name = "iids", description = "Instance IDs to resume", multiValued = true)
    private static Long[] iids;

    @Option(name = "-a", aliases = "--all", description = "Resume all suspended instances")
    private boolean resumeAll;

    private long timeoutInSeconds = 30;

    @Override
    protected Object doExecute() throws Exception {
        try {
            if (resumeAll) {
                List<TInstanceInfo> instances = getSuspendedInstances(timeoutInSeconds);
                if (instances != null) {
                    for (TInstanceInfo instance : instances) {
                        resume(Long.parseLong(instance.getIid()),
                                timeoutInSeconds);
                    }
                }
            } else {
                if (iids == null) {
                    System.out.println("No instance ids to resume");
                } else {
                    for (Long iid : iids) {
                        resume(iid, timeoutInSeconds);
                    }
                }
            }
        } catch (TimeoutException e) {
            __log.error("Timed out after " + timeoutInSeconds + " seconds", e);
        }

        return null;
    }

}
