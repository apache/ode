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
package com.fs.naming;

import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Vector;

import javax.naming.*;
import javax.naming.spi.NamingManager;

@SuppressWarnings("unchecked")
final class BindingMapEnumeration implements NamingEnumeration {

  //~ Instance/static variables ...............................................

  /** An enumeration of the memory binding collection. */
  private final Enumeration _enum;

  //~ Constructors ............................................................

  /**
   * Construct a new enumeration.
   * 
   * @param bindings The memory bindings to enumerate
   * @param names True to return an enumeration of {@link NameClassPair},
   *        false to return an enumeration of {@link Binding}
   * @param parent The parent context is required to create sub-contexts
   *        clones in the returned enumeration
   * @throws NamingException DOCUMENTME
   * @throws RuntimeException DOCUMENTME
   */
  BindingMapEnumeration(BindingMap bindings, boolean names, Context parent) throws NamingException {
    Vector noContexts;
    Object object;
    String key;
    Object[] keys;

    try {
      noContexts = new Vector();
      keys = bindings.keys();
    }
    catch (RemoteException re) {
      throw new NamingException(re.getMessage());
    }

    for (int i = 0; i < keys.length; ++i) {
      key = (String)keys[i];

      try {
        object = bindings.get(key);
      }
      catch (RemoteException re) {
        throw new RuntimeException(re.getMessage());
      }

      if (object instanceof BindingMap) {
        if (names) {
          noContexts.addElement(new NameClassPair(key, BindingMapContextImpl.class.getName(), true));
        }
        else {
          try {
            // If another context, must use lookup to create a duplicate.
            object = parent.lookup(key);
            noContexts.addElement(new Binding(key, object.getClass().getName(), object, true));
          }
          catch (NamingException except) {
            except.printStackTrace();
            throw new NamingException(except.getMessage());
          }
        }
      }
      else if (object instanceof Reference) {
        if (names)
          noContexts.addElement(new NameClassPair(key, ((Reference)object).getClassName(), true));
        else {
          try {
            Object prev;
            do {
              prev = object;
              object = NamingManager.getObjectInstance(prev, parent.getNameParser("").parse(key),
                  parent, null);
            }
            while (object != null && (object instanceof Reference) && (object != prev));

            noContexts.addElement(new Binding(key, object.getClass().getName(), object, true));
          }
          catch (Exception except) {
            throw new NamingException(except.getMessage());
          }
        }
      }
      else if (!(object instanceof LinkRef)) {
        if (names) {
          noContexts.addElement(new NameClassPair(key, object.getClass().getName(), true));
        }
        else {
          noContexts.addElement(new Binding(key, object.getClass().getName(), object, true));
        }
      }
    }

    _enum = noContexts.elements();
  }

  // ~ Methods .................................................................

  /**
   * DOCUMENTME
   */
  public void close() {
  }

  /**
   * DOCUMENTME
   * 
   * @return DOCUMENTME
   */
  public boolean hasMore() {
    return _enum.hasMoreElements();
  }

  /**
   * DOCUMENTME
   * 
   * @return DOCUMENTME
   */
  public boolean hasMoreElements() {
    return _enum.hasMoreElements();
  }

  /**
   * DOCUMENTME
   * 
   * @return DOCUMENTME
   */
  public Object next() {
    return _enum.nextElement();
  }

  /**
   * DOCUMENTME
   * 
   * @return DOCUMENTME
   */
  public Object nextElement() {
    return _enum.nextElement();
  }

}