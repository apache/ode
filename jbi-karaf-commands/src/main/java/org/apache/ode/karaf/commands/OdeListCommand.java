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
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.gogo.commands.Command;
import org.apache.ode.bpel.pmapi.TInstanceInfo;
import org.apache.ode.bpel.pmapi.TInstanceStatus;
import org.apache.ode.bpel.pmapi.TProcessInfo;

/**
 * Lists the deployed process as well as the active instances
 *
 * @author daniel
 */
@Command(scope = "ode", name = "list", description = "Lists ode processes and active instances")
public class OdeListCommand extends OdeCommandsBase {

    private static final Log __log = LogFactory.getLog(OdeListCommand.class);

    private long timeoutInSeconds = 30;

    @Override
    protected Object doExecute() throws Exception {
        try {
            System.out.println("Existing processes");
            System.out.println("------------------");
            List<TProcessInfo> processes = getProcesses(timeoutInSeconds);
            if (processes != null) {
                Set<String> sorted = new TreeSet<String>(
                        String.CASE_INSENSITIVE_ORDER);
                for (TProcessInfo info : processes) {
                    sorted.add(info.getDefinitionInfo().getProcessName()
                            .getLocalPart());
                }
                for (String s : sorted) {
                    System.out.println(s);
                }
            }
            System.out.println();

            System.out.println("Active instances");
            System.out.println("----------------");
            List<TInstanceInfo> instances = getActiveInstances(timeoutInSeconds);
            if (instances != null) {
                System.out.println("[Instance Id] [Process Name        ]");
                for (TInstanceInfo info : instances) {
                    if (info.getStatus() == TInstanceStatus.ACTIVE) {
                        StringBuilder line = new StringBuilder();
                        line.append("[");
                        line.append(getNameString(info.getIid(), 11));
                        line.append("] [");
                        line.append(getNameString(info.getProcessName()
                                .getLocalPart(), 20));
                        line.append("]");
                        System.out.println(line.toString());
                    }
                }
            }
        } catch (TimeoutException e) {
            __log.error("Timed out after " + timeoutInSeconds + " seconds", e);
        }

        return null;
    }

    private String getNameString(String name, int colLength) {
        String ret = name;
        for (int i = 0; i < colLength - name.length(); i++) {
            ret = ret + " ";
        }
        return ret;
    }

}
