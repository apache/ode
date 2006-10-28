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
package com.fs.naming.mem;

import com.fs.naming.BindingMap;
import com.fs.naming.BindingMapListener;
import com.fs.naming.DefaultNameParser;

import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.naming.*;
import javax.swing.event.EventListenerList;


/**
 * Simple, in-memory store implementation. This is used for implementing
 * transient ENC-type namespaces.
 */
@SuppressWarnings("unchecked")
public final class InMemoryBindingMapImpl
  extends java.rmi.server.UnicastRemoteObject implements BindingMap {
  private static final long serialVersionUID = -7500098402877289337L;

  /** The path of this binding. */
  private String _name = "";

  /** The name/value bindings in this space. */
  private final Hashtable _bindings = new Hashtable();
  private final EventListenerList _listeners = new EventListenerList();

  /** The parent space. */
  protected BindingMap _parent;
  private NameParser _namingParser;

  /**
   * Creates a new InMemoryBindingMapImpl object.
   *
   * @throws RemoteException DOCUMENTME
   */
  public InMemoryBindingMapImpl()
                         throws RemoteException {
    this(new DefaultNameParser());
  }

  /**
   * Creates a new InMemoryBindingMapImpl object.
   *
   * @param np DOCUMENTME
   *
   * @throws RemoteException DOCUMENTME
   */
  public InMemoryBindingMapImpl(NameParser np)
                         throws RemoteException {
    super();
    _namingParser = np;
  }

  /**
   * Called when binding these bindings to a parent binding.
   *
   * @param parent The parent binding
   * @param name The name of this binding
   */
  public void setContext(BindingMap parent, String name) {
    _parent = parent;
    _name = name;
  }

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public boolean isEmpty() {
    return _bindings.isEmpty();
  }

  /**
   * DOCUMENTME
   *
   * @param name DOCUMENTME
   */
  public void setName(String name) {
    _name = name;
  }

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   * @throws RemoteException DOCUMENTME
   */
  public String getName()
                 throws NamingException, RemoteException {
    if ((_parent != null) && (_parent.getName()
                                           .length() > 0)) {

      return _namingParser.parse(_parent.getName())
                          .add(_name)
                          .toString();
    }

    return _name;
  }

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public NameParser getNameParser() {
    return _namingParser;
  }

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public boolean isRoot() {
    return (_parent == null);
  }

  /**
   * DOCUMENTME
   *
   * @param cl DOCUMENTME
   *
   * @throws RuntimeException DOCUMENTME
   */
  public void addMemoryBindingListener(BindingMapListener cl) {
    if (_parent != null) {
      throw new RuntimeException("Trying to add a listener to a non-root MemoryBinding");
    }

    _listeners.add(BindingMapListener.class, cl);
  }

  /**
   * @see com.fs.naming.BindingMap#contains(String)
   */
  public boolean contains(String simpleName)
                   throws RemoteException {
    return _bindings.containsKey(simpleName);
  }

  /**
   * Called when destroying the subcontext and binding associated with it.
   */
  public void destroy() {
    _bindings.clear();
  }

  /**
   * DOCUMENTME
   *
   * @param key DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public Object get(String key) {
    return _bindings.get(key);
  }

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public String[] keys() {
    String[] keys = new String[_bindings.keySet()
                                        .size()];
    _bindings.keySet()
             .toArray(keys);

    return keys;
  }

  /**
   * DOCUMENTME
   *
   * @param key DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws RemoteException DOCUMENTME
   */
  public BindingMap newBindingMap(String key)
                           throws RemoteException {
    InMemoryBindingMapImpl mb = new InMemoryBindingMapImpl(_namingParser);
    mb.setContext(this, key);
    _bindings.put(key, mb);

    return mb;
  }

  /**
   * DOCUMENTME
   *
   * @param key DOCUMENTME
   * @param value DOCUMENTME
   */
  public void put(String key, Reference value) {
    _bindings.put(key, value);
    fireBindingChange(key.toString());
  }

  /**
   * DOCUMENTME
   *
   * @param key DOCUMENTME
   *
   * @throws RemoteException DOCUMENTME
   */
  public void remove(String key)
              throws RemoteException {
    Object value;
    value = _bindings.remove(key);

    if (value instanceof BindingMap) {
      ((BindingMap)value).destroy();
    }

    fireBindingChange(key);
  }

  /**
   * @see com.fs.naming.BindingMap#rename(String, String)
   */
  public void rename(String oldName, String newName)
              throws NameAlreadyBoundException, NameNotFoundException, 
                     RemoteException {
    if (_bindings.containsKey(newName)) {
      throw new NameAlreadyBoundException(newName);
    }

    Object obj = _bindings.get(oldName);

    if (obj == null) {
      throw new NameNotFoundException(oldName);
    }

    if (obj instanceof BindingMap) {
      ((InMemoryBindingMapImpl)obj).setContext(this, newName);
    }

    _bindings.put(newName, obj);
    _bindings.remove(oldName);
    fireBindingChange(oldName);
    fireBindingChange(newName);
  }

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public int size() {
    return _bindings.size();
  }

  /**
   * DOCUMENTME
   *
   * @param dn DOCUMENTME
   */
  protected void fireBindingChange(String dn) {
    InMemoryBindingMapImpl parent = this;

    while (parent._parent != null) {
      parent = (InMemoryBindingMapImpl)parent._parent;
    }

    Object[] listeners = parent._listeners.getListenerList();

    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == BindingMapListener.class) {
        // Lazily create the event:
        ((BindingMapListener)listeners[i + 1]).bindingChanged(dn);
      }
    }
  }
}
