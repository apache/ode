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
package org.opentools.minerva.connector.jdbc;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ManagedConnection;

/**
 * Abstract base class for ManagedConnections.
 * @author Aaron Mulder <ammulder@alumni.princeton.edu>
 * @version $Revision: 245 $
 */
public abstract class BaseManagedConnection implements ManagedConnection {
    protected PrintWriter logger;
    protected List<ConnectionEventListener> listeners;
    private String user;

    public BaseManagedConnection(String user) {
        this.user = user;
        listeners = new ArrayList<ConnectionEventListener>();
    }

    public void associateConnection(Object Tx) throws ResourceException {
        throw new ResourceException("associateConnection not supported");
    }

    public void addConnectionEventListener(ConnectionEventListener listener) {
        listeners.add(listener);
    }

    public void removeConnectionEventListener(ConnectionEventListener listener) {
        listeners.remove(listener);
    }

    public void setLogWriter(PrintWriter writer) throws ResourceException {
        logger = writer;
    }

    public PrintWriter getLogWriter() throws ResourceException {
        return logger;
    }

    public void destroy() throws ResourceException {
        listeners.clear();
        listeners = null;
        logger = null;
        user = null;
    }

    String getUser() {
        return user;
    }

    protected void fireConnectionEvent(ConnectionEvent evt) {
        List<ConnectionEventListener> local = new ArrayList<ConnectionEventListener>(listeners);
        for(int i=local.size()-1; i >= 0; i--) {
            if(evt.getId() == ConnectionEvent.CONNECTION_CLOSED) {
                local.get(i).connectionClosed(evt);
            }
            else if(evt.getId() == ConnectionEvent.CONNECTION_ERROR_OCCURRED) {
                local.get(i).connectionErrorOccurred(evt);
            }
        }
    }

}