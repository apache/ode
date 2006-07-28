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
 * Optional interface for an object in an ObjectPool.  If the objects created
 * by the ObjcetFactory implement this, the pool will register as a listener
 * when an object is checked out, and deregister when the object is returned.
 * Then if the object sends a close or error event, the pool will return the
 * object to the pool without the client having to do so explicitly.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public interface PooledObject {
    /**
     * Adds a new listener.
     */
    public void addPoolEventListener(PoolEventListener listener);
    /**
     * Removes a listener.
     */
    public void removePoolEventListener(PoolEventListener listener);
}
