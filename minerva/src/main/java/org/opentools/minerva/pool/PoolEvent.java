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

import java.util.EventObject;

/**
 * An event caused by an object in a pool.  The event indicates that the
 * object was used, closed, or had an error occur.  The typical response is
 * to update the last used time in the pool for used events, and return the
 * object to the pool for closed or error events.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class PoolEvent extends EventObject {
    /**
     * The object has been closed and should be returned to the pool.  Note this
     * is not a final sort of closing - the object must still be able to be
     * returned to the pool and reused.
     */
    public final static int OBJECT_CLOSED = -8986432;
    /**
     * Indicates that an error occured with the object.  The object will be
     * returned to the pool, since there will presumably be an exception
     * thrown that precludes the client from closing it or returning it
     * normally.  This should not be used for final or destructive errors - the
     * object must stil be able to be returned to the pool and reused.
     */
    public final static int OBJECT_ERROR  = -8986433;
    /**
     * Indicates that the object was used, and its timestamp should be updated
     * accordingly (if the pool tracks timestamps).
     */
    public final static int OBJECT_USED   = -8986434;

    private int type;
    private boolean catastrophic = false;

    /**
     * Create a new event.
     * @param source The source must be the object that was returned from the
     *        getObject method of the pool - the pool will use the source for
     *        some purpose depending on the type, so it cannot be an arbitrary
     *        object.
     * @param type The event type.
     */
    public PoolEvent(Object source, int type) {
        super(source);
        if(type != OBJECT_CLOSED && type != OBJECT_ERROR && type != OBJECT_USED)
            throw new IllegalArgumentException("Invalid event type!");
        this.type = type;
    }

    /**
     * Gets the event type.
     * @see #OBJECT_CLOSED
     * @see #OBJECT_USED
     * @see #OBJECT_ERROR
     */
    public int getType() {
        return type;
    }

    /**
     * Gets whether an object error was so bad that the object should not
     * be reused by the pool.  This is meaningful for error events only.
     */
    public boolean isCatastrophic() {
        return catastrophic;
    }

    /**
     * Marks this as an error so severe that the object should not be reused by
     * the pool.
     */
    public void setCatastrophic() {
        catastrophic = true;
    }
}
