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
package org.apache.ode.bpel.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 
 * WARNING --- EXPERIMENTAL
 * 
 * Mechanism for obtaining instance-level locks. Very simple implementation at the moment, that is only valid
 * for a single processing node. To move to multi-processor setup we'll need to implement this lock in the database.
 * 
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 */
public class InstanceLockManager {

    private final Lock _mutex = new java.util.concurrent.locks.ReentrantLock();
    private final Map<Long, InstanceInfo> _locks = new HashMap<Long,InstanceInfo> ();
    
    public void lock(Long iid, int time, TimeUnit tu) throws InterruptedException, TimeoutException {
        InstanceInfo li;

        _mutex.lock();
        try {
            
            while (true) {
                li = _locks.get(iid);
                if (li == null) {
                    li = new InstanceInfo(iid, Thread.currentThread());
                    _locks.put(iid, li);
                } else {
                    if (!li.available.await(time, tu)) 
                        throw new TimeoutException();
                }
            }
            
        } finally {
            _mutex.unlock();
        }
                
    }
    
    public void unlock(Long iid)  {
        
        _mutex.lock();
        try {
            InstanceInfo li = _locks.get(iid);
            if (li == null)
                throw new IllegalStateException("Instance not locked, cannot unlock!");
            
            _locks.remove(iid);
            
            // Note, that we have to signall all threads, because new holder will create a new 
            // instance of "available" condition variable, so all the waiters need to try again
            li.available.signalAll();
                
        } finally {
            _mutex.unlock();
        }
        
    }
    
    
    @Override
    public String toString() {
        return "{InstanceLockManager: " + _locks +  "}";
    }
    
    /**
     * Information about the lock state for a particular instance.
     * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
     */
    private class InstanceInfo {
        final long iid;
        
        /** Thread that acquired the lock. */
        final Thread acquierer;

        /** Condition-Variable indicating that the lock has become available. */
        Condition available = _mutex.newCondition();

        
        InstanceInfo(long iid, Thread t) {
            this.iid = iid;
            this.acquierer = t;
        }
        
        @Override
        public String toString() {
            return "{Lock for Instance #" + iid +", acquired by " +  acquierer + "}";
        }
    }
    
    /** Exception class indicating a time-out occured while obtaining a lock. */
    public static final class TimeoutException extends Exception {
        private static final long serialVersionUID = 7247629086692580285L;
    }
}
