/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
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
