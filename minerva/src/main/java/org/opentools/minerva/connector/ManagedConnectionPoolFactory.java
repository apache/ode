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
package org.opentools.minerva.connector;

import java.io.PrintWriter;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;

import org.opentools.minerva.pool.ObjectPool;
import org.opentools.minerva.pool.PoolObjectFactory;

/**
 * PoolObjectFactory implementation for Connectors.  Creates a
 * ManagedConnection from a ManagedConnectionFactory and a
 * ConnectionParameters.  Handles cleanup when connections are
 * returned to the pool, destroy when they are removed from the
 * pool, and matching to determine eligible connections.
 *
 * @author Aaron Mulder ammulder@alumni.princeton.edu
 */
@SuppressWarnings("unchecked")
public class ManagedConnectionPoolFactory extends PoolObjectFactory {
    private ManagedConnectionFactory factory;
    private ObjectPool pool;
    private PrintWriter log = new PrintWriter(System.out);

    public ManagedConnectionPoolFactory(ManagedConnectionFactory factory) {
        this.factory = factory;
    }

    /**
     * Tracks the pool and logger.
     */
    public void poolStarted(ObjectPool pool, PrintWriter log) {
        super.poolStarted(pool, log);
        this.pool = pool;
        this.log = log;
    }

    public void poolClosing(ObjectPool pool) {
        super.poolClosing(pool);
        factory = null;
        pool = null;
        log = null;
    }

    /**
     * Creates a new ManagedConnection with the specified
     * parameters.
     */
    public Object createObject(Object parameters) {
        ConnectionParameters params = (ConnectionParameters)parameters;
        try {
            Object ob = factory.createManagedConnection(params == null ? null : params.subject,
                                                        params == null ? null : params.info);
            return ob;
        } catch(ResourceException e) {
            if(log != null)
            	log.println("Unable to create ManagedConnection: "+e);
            return null;
        }
    }

    /**
     * Runs a matchManagedConnections to see whether the candidate
     * connection is acceptable based on the request parameters.
     */
    public boolean checkValidObject(Object source, Object parameters) {
        if(parameters == null)
            return true;
        ConnectionParameters params = (ConnectionParameters)parameters;

        try {
            Object test = factory.matchManagedConnections(new SingleSet(source), params.subject, params.info);
            return test != null;
        } catch(ResourceException e) {
            return false;
        }
    }

    /**
     * Cleans up a returned ManagedConnection.
     */
    public Object returnObject(Object clientObject) {
        try {
            ((ManagedConnection)clientObject).cleanup();
        } catch(ResourceException e) {
            if(log != null){
            	log.println("Error cleaning up ManagedConnection:");
            	e.printStackTrace(log);
            }
            pool.markObjectAsInvalid(clientObject);
        }
        return clientObject;
    }

    /**
     * A set of one, used for mathcing connections.
     */
    private static class SingleSet extends AbstractSet {
        private Object ob;

        public SingleSet(Object ob) {
            this.ob = ob;
        }

        public int size() {
            return 1;
        }

        public Iterator iterator() {
            return new Iterator() {
                int pos = 0;
                public boolean hasNext() {
                    return pos < 1;
                }

                public Object next() {
                    if(pos < 1) {
                        ++pos;
                        return ob;
                    }
                    throw new NoSuchElementException();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
