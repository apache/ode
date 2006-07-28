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

/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.opentools.minerva.pool;

import java.io.PrintWriter;
import java.io.Serializable;

/**
 * Convenience wrapper for all ObjectPool parameters.  See the
 * ObjectPool setters and getters for descriptions of the parameters.
 * @see org.opentools.minerval.pool.ObjectPool
 *
 * @author Aaron Mulder ammulder@alumni.princeton.edu
 */
public class PoolParameters implements Serializable {
    public final static String MIN_SIZE_KEY = "MinSize";
    public final static String MAX_SIZE_KEY = "MaxSize";
    public final static String BLOCKING_KEY = "Blocking";
    public final static String GC_ENABLED_KEY = "GCEnabled";
    public final static String IDLE_TIMEOUT_ENABLED_KEY = "IdleTimeoutEnabled";
    public final static String INVALIDATE_ON_ERROR_KEY = "InvalidateOnError";
    public final static String TRACK_LAST_USED_KEY = "TimestampUsed";
    public final static String GC_INTERVAL_MS_KEY = "GCIntervazlMillis";
    public final static String GC_MIN_IDLE_MS_KEY = "GCMinIdleMillis";
    public final static String IDLE_TIMEOUT_MS_KEY = "IdleTimeoutMillis";
    public final static String MAX_IDLE_TIMEOUT_PERCENT_KEY = "MaxIdleTimeoutPercent";
    public final static String LOGGER_KEY = "Logger";
    public final static String LOGGER_ENABLED = "LoggingEnabled";

    public int minSize                 = 0;
    public int maxSize                 = 0;
    public boolean blocking            = true;
    public boolean gcEnabled           = false;
    public boolean idleTimeoutEnabled  = false;
    public boolean invalidateOnError   = false;
    public boolean trackLastUsed       = false;
    public long gcIntervalMillis       = 120000l;
    public long gcMinIdleMillis        = 1200000l;
    public long idleTimeoutMillis      = 1800000l;
    public float maxIdleTimeoutPercent = 1.0f;
    public PrintWriter logger          = null;

    public PoolParameters() {
    }
}
