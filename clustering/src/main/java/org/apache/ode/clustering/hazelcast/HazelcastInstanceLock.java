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
package org.apache.ode.clustering.hazelcast;

import com.hazelcast.core.IMap;
import org.apache.ode.bpel.clapi.ClusterLock;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HazelcastInstanceLock implements ClusterLock<Long> {
    private static final Log __log = LogFactory.getLog(HazelcastInstanceLock.class);

    private IMap<Long, Long> _lock_map;


    HazelcastInstanceLock(IMap<Long, Long> lock_map) {
        _lock_map = lock_map;
    }

    public void putIfAbsent(Long key, Long keyVal) {
        _lock_map.putIfAbsent(key, keyVal);
    }

    public void lock(Long key) {
        // Noting to do here.
    }

    public void lock(Long iid, int time, TimeUnit tu) throws InterruptedException,TimeoutException {
        if (iid == null) {
            if (__log.isDebugEnabled())
                __log.debug(" Instance Id null at lock[]");
            return;
        }

        String thrd = Thread.currentThread().toString();

        if (__log.isDebugEnabled())
            __log.debug(thrd + ": lock(iid=" + iid + ", time=" + time + tu + ")");

        putIfAbsent(iid, iid);

        if (!_lock_map.tryLock(iid, time, tu)) {

            if (__log.isDebugEnabled())
                __log.debug(thrd + ": lock(iid=" + iid + ", " +
                        "time=" + time + tu + ")-->TIMEOUT");
            throw new TimeoutException();
        }

    }

    public void unlock(Long iid) {
        if (iid == null) {
            if (__log.isDebugEnabled())
                __log.debug(" unlock, instance id is null");
            return;
        }

        String thrd = Thread.currentThread().toString();

        _lock_map.unlock(iid);

        if (__log.isDebugEnabled())
            __log.debug(thrd + " unlock(iid=" + iid + ")");
    }

    public boolean tryLock(Long key) {
        // Noting to do here.
        return false;
    }

    public boolean tryLock(Long key, int time, TimeUnit tu) {
        // Noting to do here.
        return false;
    }
}
