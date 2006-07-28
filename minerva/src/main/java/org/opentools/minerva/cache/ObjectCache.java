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
package org.opentools.minerva.cache;

/**
 * A caching map.  This associates one or more values with a
 * key.  When a value is requested for a key, a cached value
 * will be returned if available, and a new value will be
 * generated and cached otherwise.  Instances of this interface
 * require a CachedObjectFactory to generate new values, and
 * control the caching in custom ways.
 * @see org.ollinace.minerva.cache.CachedObjectFactory
 * @author Aaron Mulder ammulder@alumni.princeton.edu
 */
public interface ObjectCache {
    /**
     * When setting the size, this constant means there should
     * be no limit on the size of the cache.  This is not
     * recommended under ordinary circumstances.
     */
    public final static int UNLIMITED_SIZE = 0;

    /**
     * Gets a cached object with the specified key.  This is not
     * an exclusive function - any number of clients may get the
     * same object at the same time.
     * @see #useObject
     */
    public Object getObject(Object key);

    /**
     * Gets a cached object with the specified key.  This is an
     * exclusive function - no other client may get the same
     * object at the same time.  You must return the object using
     * returnObject before another client may reuse this object.
     * @see #getObject
     * @see #returnObject
     */
    public Object useObject(Object key);

    /**
     * Returns an object to the cache that is currently in use
     * (checked out via useObject).  No other client can use
     * the same object until this method is called.  The original
     * client may not continue to use the object after this
     * method is called.
     * @see #useObject
     */
    public void returnObject(Object key, Object value);

    /**
     * Removes all objects from the cache that have this key.
     * There will only be more than one object with the same
     * key if clients are using useObject and returnObject
     * instead of getObject.
     */
    public void removeObjects(Object key);

    /**
     * Sets the maximum number of objects in the cache.  If the
     * number of objects is at the limit and a new object is
     * requested, other objects will be dropped from the cache.
     */
    public void setSize(int size);

    /**
     * Removes all cached objects and stops the cache.
     */
    public void close();
}
