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
import com.fs.naming.BindingMapContextImpl;
import com.fs.naming.DefaultNameParser;

import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.spi.InitialContextFactory;


/**
 * JNDI context factory for in-memory JNDI stores.
 */
public final class InMemoryContextFactory implements InitialContextFactory {
  /** The shared root of the binding tree. */
  private static BindingMap _root;

  /**
   * Returns a binding in the specified path. If the binding does not exist,
   * the full path is created and a new binding is returned. The binding is
   * always obtained from the shared root.
   *
   * @param path The path
   *
   * @return The memory binding for the path
   *
   * @throws NamingException Name is invalid or caller does not have adequate
   *         permission to access shared memory context
   */
  public static synchronized BindingMap getBindings(String path)
                                             throws NamingException {
    BindingMap binding;
    BindingMap newBinding;
    CompositeName name;
    int i;
    name = new CompositeName(path);

    try {
      if (_root == null) {
        _root = new InMemoryBindingMapImpl(new DefaultNameParser());
      }

      binding = _root;

      for (i = 0; i < name.size(); ++i) {
        if (name.get(i)
                      .length() > 0) {
          try {
            newBinding = (BindingMap)binding.get(name.get(i));

            if (newBinding == null) {
              newBinding = binding.newBindingMap(name.get(i));
            }

            binding = newBinding;
          } catch (ClassCastException except) {
            throw new NotContextException(path + " does not specify a context");
          }
        }
      }
    } catch (RemoteException re) {
      // unlikely
      re.printStackTrace();
      throw new NamingException(re.getMessage());
    }

    return binding;
  }

  /**
   * DOCUMENTME
   */
  public static void setEnvironment() {
    System.setProperty(Context.INITIAL_CONTEXT_FACTORY, InMemoryContextFactory.class.getName());

    // provider url can be anything
    System.setProperty(Context.PROVIDER_URL, "staged");
  }

  /**
   * Returns an initial context based on the {@link Context#PROVIDER_URL}
   * environment attribute. If this attribute is missing or an empty string,
   * the root will be returned. If not, the specified context will be
   * returned relative to the root.
   *
   * @param env DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   */
  public Context getInitialContext(Hashtable env)
                            throws NamingException {
    if (env.get(Context.PROVIDER_URL) == null) {
      return new BindingMapContextImpl(env);
    } else {
      String url = env.get(Context.PROVIDER_URL).toString();
      return new BindingMapContextImpl(getBindings(url), env);
    }
  }
}
