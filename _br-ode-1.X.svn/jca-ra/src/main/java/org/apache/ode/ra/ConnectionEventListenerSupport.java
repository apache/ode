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
package org.apache.ode.ra;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;

/**
 * Multiplexer for {@link ConnectionEventListener}s. Sends the same event to
 * multiple listeners and managed the listener list.
 */
class ConnectionEventListenerSupport implements ConnectionEventListener {
  private final List<ConnectionEventListener> _eventListeners =
    new ArrayList<ConnectionEventListener>();

  public void connectionClosed(ConnectionEvent event) {
    for (Iterator i = _eventListeners.iterator();i.hasNext(); )
      ((ConnectionEventListener)i.next()).connectionClosed(event);
  }

  public void connectionErrorOccurred(ConnectionEvent event) {
    for (Iterator i = _eventListeners.iterator();i.hasNext(); )
      ((ConnectionEventListener)i.next()).connectionErrorOccurred(event);
  }

  public void localTransactionCommitted(ConnectionEvent event) {
    for (Iterator i = _eventListeners.iterator();i.hasNext(); )
      ((ConnectionEventListener)i.next()).localTransactionCommitted(event);
  }

  public void localTransactionRolledback(ConnectionEvent event) {
    for (Iterator i = _eventListeners.iterator();i.hasNext(); )
      ((ConnectionEventListener)i.next()).localTransactionRolledback(event);
  }

  public void localTransactionStarted(ConnectionEvent event) {
    for (Iterator i = _eventListeners.iterator();i.hasNext(); )
      ((ConnectionEventListener)i.next()).localTransactionStarted(event);
  }

  void addListener(ConnectionEventListener el) {
    _eventListeners.add(el);
  }

  void removeListener(ConnectionEventListener el) {
    _eventListeners.remove(el);
  }

}
