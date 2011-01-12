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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.management.*;

import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.apache.ode.bpel.pmapi.*;
import org.apache.ode.jbi.OdeContext;

public abstract class OdeCommandsBase extends OsgiCommandSupport {

    protected static String COMPONENT_NAME = "org.apache.servicemix:Type=Component,Name=OdeBpelEngine,SubType=Management";

    protected static final String LIST_INSTANCES = "listInstances";
    protected static final String LIST_ALL_INSTANCES = "listAllInstances";
    protected static final String LIST_ALL_PROCESSES = "listAllProcesses";
    protected static final String RECOVER_ACTIVITY= "recoverActivity";
    protected static final String TERMINATE = "terminate";
    protected static final String SUSPEND = "suspend";
    protected static final String RESUME = "resume";

    protected MBeanServer getMBeanServer() {
        OdeContext ode = OdeContext.getInstance();
        if (ode != null) {
            return ode.getContext().getMBeanServer();
        }
        return null;
    }

    /**
     * Invokes an operation on the ODE MBean server
     *
     * @param <T>
     * @param operationName
     * @param args
     * @param T
     * @return
     */
    @SuppressWarnings("unchecked")
    protected <T> T invoke(final String operationName, final Object[] params,
            final String[] signature, long timeoutInSeconds)
            throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<T> callable = new Callable<T>() {
            public T call() throws Exception {
                MBeanServer server = getMBeanServer();
                if (server != null) {
                    return (T) server.invoke(new ObjectName(COMPONENT_NAME),
                            operationName, params, signature);
                }
                return null;
            }
        };
        Future<T> future = executor.submit(callable);
        executor.shutdown();
        return future.get(timeoutInSeconds, TimeUnit.SECONDS);
    }

    protected List<TInstanceInfo> getActiveInstances(long timeoutInSeconds)
        throws Exception {
        return getFilteredInstances(timeoutInSeconds, "status=active");
    }

    protected List<TInstanceInfo> getSuspendedInstances(long timeoutInSeconds)
        throws Exception {
        return getFilteredInstances(timeoutInSeconds, "status=suspended");
    }

    protected List<TInstanceInfo> getFilteredInstances(long timeoutInSeconds, String filter)
        throws Exception {
        InstanceInfoListDocument instances = invoke(LIST_INSTANCES,
                new Object[] {filter, "pid", 10},
                new String[] {String.class.getName(), String.class.getName(), int.class.getName()},
                timeoutInSeconds);
        if (instances != null) {
            return instances.getInstanceInfoList().getInstanceInfoList();
        }
        return null;
    }

    protected List<TInstanceInfo> getAllInstances(long timeoutInSeconds)
            throws Exception {
        InstanceInfoListDocument instances = invoke(LIST_ALL_INSTANCES, null,
                null, timeoutInSeconds);
        if (instances != null) {
            return instances.getInstanceInfoList().getInstanceInfoList();
        }
        return null;
    }

    protected List<TProcessInfo> getProcesses(long timeoutInSeconds)
            throws Exception {
        ProcessInfoListDocument result = invoke(LIST_ALL_PROCESSES, null, null, timeoutInSeconds);
        if (result != null) {
            return result.getProcessInfoList().getProcessInfoList();
        }
        return null;
    }

    protected InstanceInfoDocument recoverActivity(Long instanceId, Long activityId, String action, long timeoutInSeconds) throws Exception {
        InstanceInfoDocument result = invoke(RECOVER_ACTIVITY, new Object[] {instanceId, activityId, action},
                new String[] {Long.class.getName(), Long.class.getName(), String.class.getName()},
                timeoutInSeconds);
        return result;
    }

    protected void terminate(Long iid, long timeoutInSeconds) throws Exception {
        invoke(TERMINATE, new Long[] { iid }, new String[] { Long.class
                .getName() }, timeoutInSeconds);
    }

    protected void suspend(Long iid, long timeoutInSeconds) throws Exception {
        invoke(SUSPEND, new Long[] { iid }, new String[] { Long.class
                .getName() }, timeoutInSeconds);
    }

    protected void resume(Long iid, long timeoutInSeconds) throws Exception {
        invoke(RESUME, new Long[] { iid }, new String[] { Long.class
                .getName() }, timeoutInSeconds);
    }

}
