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

import java.util.*;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.gogo.commands.*;
import org.apache.ode.bpel.pmapi.*;
import org.apache.ode.bpel.pmapi.TScopeInfo.Activities;

/**
 * Lists the deployed process as well as the active instances
 *
 * @author daniel
 */
@Command(scope = "ode", name = "list", description = "Lists ode processes and active instances")
public class OdeListCommand extends OdeCommandsBase {

    private static final Log __log = LogFactory.getLog(OdeListCommand.class);

    @Option(name = "-a", aliases = "--all", description = "Show all (even completed) instances")
    private boolean showAll;

    private long timeoutInSeconds = 30;

    @Override
    protected Object doExecute() throws Exception {
        try {
            System.out.println("Existing processes");
            System.out.println("------------------");
            List<TProcessInfo> processes = getProcesses(timeoutInSeconds);
            if (processes != null) {
                System.out.println("[ ] [Version] [PID                                                            ]");
                Set<String> sorted = new TreeSet<String>(
                        String.CASE_INSENSITIVE_ORDER);
                for (TProcessInfo info : processes) {
                    StringBuilder line = new StringBuilder();
                    line.append("[");
                    line.append(info.getStatus().toString().charAt(0));
                    line.append("] [");
                    line.append(getNameString(Long.toString(info.getVersion()), 7, false));
                    line.append("] [");
                    line.append(getNameString(info.getPid().toString(), 63, true));
                    line.append("]");

                    sorted.add(line.toString());
                }
                for (String s : sorted) {
                    System.out.println(s);
                }
            }
            System.out.println();

            System.out.println("Active instances");
            System.out.println("----------------");
            List<TInstanceInfo> instances = showAll ? getAllInstances(timeoutInSeconds) : getActiveInstances(timeoutInSeconds);
            if (instances != null) {
                System.out.println("[ ] [IID  ] [Process Name                   ] [Failed Activities              ]");
                for (TInstanceInfo info : instances) {
                    StringBuilder line = new StringBuilder();
                    line.append("[");
                    line.append(info.getStatus().toString().charAt(0));
                    line.append("] [");
                    line.append(getNameString(info.getIid(), 5, false));
                    line.append("] [");
                    line.append(getNameString(info.getPid(), 31, true));
                    line.append("] [");
                    StringBuilder failedString = new StringBuilder();
                    List<TActivityInfo> failedActivities = getFailedActivities(info);
                    if (!failedActivities.isEmpty()) {
                        boolean first = true;
                        for (TActivityInfo failed : failedActivities) {
                            if (!first) {
                                failedString.append(", ");
                            }
                            failedString.append(failed.getAiid());
                            first = false;
                        }
                    }
                    line.append(getNameString(failedString.toString(), 31, false));
                    line.append("]");
                    System.out.println(line.toString());
                }
            }
        } catch (TimeoutException e) {
            __log.error("Timed out after " + timeoutInSeconds + " seconds", e);
        }

        return null;
    }
    
    private List<TActivityInfo> getFailedActivities(TInstanceInfo instance) {
        List<TActivityInfo> failedActivites = new ArrayList<TActivityInfo>();
        try {
            TScopeInfo scopeInfo = getScopeInfo(instance.getRootScope());
            if (scopeInfo != null) {
                collectFailedActivities(scopeInfo, failedActivites);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return failedActivites;
    }
    
    private TScopeInfo getScopeInfo(TScopeRef scopeRef) {
        if (scopeRef != null) {
            try {
                ScopeInfoDocument scopeInfoDoc = invoke("getScopeInfoWithActivity", new Object[] {scopeRef.getSiid(), true}, 
                        new String[] {String.class.getName(), boolean.class.getName()}, 30);
                if (scopeInfoDoc != null) {
                    return scopeInfoDoc.getScopeInfo();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    private void collectFailedActivities(TScopeInfo scopeInfo, List<TActivityInfo> bin) {
        Activities acts = scopeInfo.getActivities();
        if (acts != null) {
            for (TActivityInfo actInfo : acts.getActivityInfoList()) {
                if (actInfo.getStatus() == TActivityStatus.FAILURE) {
                    bin.add(actInfo);
                }
            }
        }
        TScopeInfo.Children children = scopeInfo.getChildren();
        if (children != null) {
            for (TScopeRef child : children.getChildRefList()) {
                TScopeInfo childScopeInfo = getScopeInfo(child);
                if (childScopeInfo != null) {
                    collectFailedActivities(childScopeInfo, bin);
                }
            }
        }
        
    }

    private String getNameString(String name, int colLength, boolean stripBefore) {
        String ret = name;
        if (name.length() > colLength) {
            if (stripBefore) {
                ret = "..." + name.substring(name.length() - (colLength - 3));
            } else {
                ret = name.substring(0, colLength - 3) + "...";
            }
        }
        for (int i = 0; i < colLength - name.length(); i++) {
            ret = ret + " ";
        }
        return ret;
    }

}
