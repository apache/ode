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

import java.util.concurrent.TimeUnit;

/**
 * Abstract class to implement an instance lock manager. Instance lock provide process instance isolation from
 * concurrent access when entering jacob
 */
public abstract class AbstractInstanceLockManager {
    abstract void unlock(Long iid);

    abstract void lock(Long iid, int i, TimeUnit microseconds) throws InterruptedException,
            TimeoutException;

    /** Exception class indicating a time-out occured  while obtaining a lock. */
    public static final class TimeoutException extends Exception {
        private static final long serialVersionUID = 7247629086692580285L;
    }
}
