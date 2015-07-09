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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HazelcastDeploymentLock implements ClusterLock{
    private static final Log __log = LogFactory.getLog(HazelcastDeploymentLock.class);

    private IMap<String, String> _lock_map;

    HazelcastDeploymentLock(IMap<String, String> lock_map) {
        _lock_map = lock_map;
    }

    public void putIfAbsent(String key, String keyVal) {
        _lock_map.putIfAbsent(key, keyVal);
    }

    public boolean lockMap(String key) {
        _lock_map.lock(key);
        if (__log.isDebugEnabled()) {
            __log.debug("ThreadID:" + Thread.currentThread().getId() + " duLocked value for " + key + " file" + " after locking: " + true);
        }
        return true;
    }

    public boolean unlockMap(String key) {
        _lock_map.unlock(key);
        if (__log.isDebugEnabled()) {
            __log.debug("ThreadID:" + Thread.currentThread().getId() + " duLocked value for " + key + " file" + " after unlocking: " + false);
        }
        return true;
    }

    public boolean tryLockMap(String key) {
        boolean state = _lock_map.tryLock(key);
        if (__log.isDebugEnabled()) {
            __log.debug("ThreadID:" + Thread.currentThread().getId() + " duLocked value for " + key + " file" + " after locking: " + state);
        }
        return state;
    }
}
