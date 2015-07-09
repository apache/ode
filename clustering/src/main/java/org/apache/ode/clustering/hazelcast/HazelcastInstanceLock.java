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
import org.apache.ode.bpel.AbstractInstanceLockManager;

import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HazelcastInstanceLock extends AbstractInstanceLockManager implements ClusterLock {
    private static final Log __log = LogFactory.getLog(HazelcastInstanceLock.class);

    private IMap<String, String> _lock_map;


    HazelcastInstanceLock(IMap<String, String> lock_map) {
        _lock_map = lock_map;
    }

    public void putIfAbsent(String key, String keyVal) {
        _lock_map.putIfAbsent(key, keyVal);
    }

    public void lock(Long iid, int time, TimeUnit tu) throws InterruptedException,
            AbstractInstanceLockManager.TimeoutException {
        if (iid == null) {
            if(__log.isDebugEnabled()) {
                __log.debug(" Instance Id null at lock[]");
            }
            return;
        }

        String thrd = Thread.currentThread().toString();

        if(__log.isDebugEnabled()) {
            __log.debug(thrd + ": lock(iid=" + iid + ", time=" + time + tu + ")");
        }

        putIfAbsent(iid.toString(),iid.toString());

        if (!tryLockMap(iid.toString(),time, tu)) {

            if(__log.isDebugEnabled()) {
                __log.debug(thrd + ": lock(iid=" + iid + ", " +
                        "time=" + time + tu + ")-->TIMEOUT");
            }
            throw new AbstractInstanceLockManager.TimeoutException();
        }

    }

    public void unlock(Long iid) {
        if (iid == null) {
            if(__log.isDebugEnabled()) {
                __log.debug(" unlock, instance id is null");
            }
            return;
        }

        String thrd = Thread.currentThread().toString();

        unlockMap(iid.toString());

        if(__log.isDebugEnabled()) {
            __log.debug(thrd + " unlock(iid=" + iid + ")");
        }
    }

    public boolean lockMap(String key) {
        _lock_map.lock(key);
        return true;
    }

    public boolean unlockMap(String key) {
        if (_lock_map.get(key) == "true") {
            _lock_map.unlock(key);
            _lock_map.replace(key,"false");
        }
        return true;
    }

    public boolean tryLockMap(String key) {
        boolean state = _lock_map.tryLock(key);
        return state;
    }

    public boolean tryLockMap(String key,int time, TimeUnit tu) {
        boolean state = true;
        try {
        state = _lock_map.tryLock(key,time,tu);
        } catch (InterruptedException ex) {
            __log.error(ex);
        }

        _lock_map.replace(key,"" +state);
        return state;
    }
}
