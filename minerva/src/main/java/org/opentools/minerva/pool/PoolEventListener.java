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

/**
 * A listener for object pool events.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public interface PoolEventListener {
    /**
     * The pooled object was closed and should be returned to the pool.
     */
    public void objectClosed(PoolEvent evt);
    /**
     * The pooled object had an error and should be returned to the pool.
     */
    public void objectError(PoolEvent evt);
    /**
     * The pooled object was used and its timestamp should be updated.
     */
    public void objectUsed(PoolEvent evt);
}
