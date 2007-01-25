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

package org.apache.ode.test.scheduler;

import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Scheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class TestScheduler implements Scheduler {
    ThreadLocal<List<Scheduler.Synchronizer>> _synchros = new ThreadLocal<List<Scheduler.Synchronizer>>() {
        @Override
        protected List<Synchronizer> initialValue() {
            return new ArrayList<Synchronizer>();
        }

    };

    public String schedulePersistedJob(Map<String, Object> arg0, Date arg1) throws ContextException {
        return null;
    }

    public String scheduleVolatileJob(boolean arg0, Map<String, Object> arg1, Date arg2) throws ContextException {
        return null;
    }

    public void cancelJob(String arg0) throws ContextException {

    }

    public <T> T execTransaction(Callable<T> arg0) throws Exception, ContextException {
        begin();
        try {
            T retval = arg0.call();
            return retval;
        } finally {
            commit();
        }
    }

    public <T> T execIsolatedTransaction(Callable<T> arg0) throws Exception, ContextException {
        begin();
        try {
            T retval = arg0.call();
            return retval;
        } finally {
            commit();
        }
    }

    public void start() {
    }

    public void stop() {
    }

    public void shutdown() {
    }

    public void registerSynchronizer(Synchronizer synch) throws ContextException {
        _synchros.get().add(synch);
    }

    public void begin() {
        _synchros.get().clear();
    }

    public void commit() {
        for (Synchronizer s : _synchros.get())
            try {
                s.beforeCompletion();
            } catch (Throwable t) {
            }
        for (Synchronizer s : _synchros.get())
            try {
                s.afterCompletion(true);
            } catch (Throwable t) {
            }

        _synchros.get().clear();
    }

    public void rollback() {
        for (Synchronizer s : _synchros.get())
            try {
                s.beforeCompletion();
            } catch (Throwable t) {
            }
        for (Synchronizer s : _synchros.get())
            try {
                s.afterCompletion(false);
            } catch (Throwable t) {
            }
        _synchros.get().clear();
    }


    public void setJobProcessor(JobProcessor processor) throws ContextException {
        // Nothing to do.
    }
}
