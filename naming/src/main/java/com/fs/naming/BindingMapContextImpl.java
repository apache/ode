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

import com.fs.naming.mem.InMemoryBindingMapImpl;
import org.apache.ode.utils.msg.MessageBundle;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.*;
import javax.naming.spi.NamingManager;


/**
 * JNDI <code>Context</code> implementation wrapping a remote
 * <code>BindingMap</code> object.
 */
@SuppressWarnings("unchecked")
public class BindingMapContextImpl implements Context {
  private static final NamingMessages __msgs = MessageBundle.getMessages(NamingMessages.class);

  /**
   * Environment attribute to set a context read-only. The value must be a
   * string equal to <tt>true</tt>. Once the context has been set read-only,
   * it cannot be reset to read-write.
   */
  public static final String PROP_READONLY = "readOnly";

  /** The default name separator for this context is '/'. */
  public static final String NAMESEPERATOR = "/";

  /** The default name parser for this context. */
  public final NameParser _defaultNameParser;

  /**
   * Holds the bindings associated with this context. Multiple contexts may
   * share the same binding. The binding is selected based on the {@link
   * Context.PROVIDER_URL} attribute. The context's name in the name space is
   * know to the bindings.
   */
  private BindingMap _bindings;

  /**
   * The environment attributes used to construct this context. Will be passed
   * on to all contexts constructed by this context.
   */
  private Hashtable _env = new Hashtable();

  /**
   * True if this context has been set read-only. Once it has been set
   * read-only, it cannot revert to writable and all contexts returns by this
   * context are read-only.
   */
  private boolean _readOnly;

  /**
   * Construct a new context with the specified environment attributes. The
   * environment property {@link Context#PROVIDER_URL} names the underlying
   * bindings. If the property is absent, the returned context has it's own
   * binding space which is not shared with other contexts created in this
   * manner.
   *
   * @param env The environment attributes
   *
   * @throws NamingException The attribute {@link Context#PROVIDER_URL} does
   *         not specify a context
   */
  public BindingMapContextImpl(Hashtable env)
                        throws NamingException {
    Enumeration en;
    String name;
    _defaultNameParser = new DefaultNameParser();

    // Use addToEnvironment to duplicate the environment variables.
    // This takes care of setting certain flags appropriately.
    try {
      if (env != null) {
        _bindings = new InMemoryBindingMapImpl(_defaultNameParser);
        en = env.keys();

        while (en.hasMoreElements()) {
          name = (String)en.nextElement();
          addToEnvironment(name, env.get(name));
        }
      } else {
        _bindings = new InMemoryBindingMapImpl(_defaultNameParser);
      }
    } catch (RemoteException re) {
      //highly unlikely (i.e. stub didn't compile)
      re.printStackTrace();
    }
  }

  /**
   * Construct a new context with the specified bindings and environment
   * attributes.
   *
   * @param bindings DOCUMENTME
   * @param env DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   * @throws CommunicationException DOCUMENTME
   */
  public BindingMapContextImpl(BindingMap bindings, Hashtable env)
                        throws NamingException {
    Enumeration en;
    String name;
    _bindings = bindings;

    // Use addToEnvironment to duplicate the environment variables.
    // This takes care of setting certain flags appropriately.
    if (env != null) {
      en = env.keys();

      while (en.hasMoreElements()) {
        name = (String)en.nextElement();
        addToEnvironment(name, env.get(name));
      }
    }

    try {
      _defaultNameParser = bindings.getNameParser();
    } catch (RemoteException re) {
      throw new CommunicationException(re.getMessage());
    }
  }

  /**
   * Returns the bindings represented by this context. Used when assigning a
   * memory context into the ENC.
   *
   * @return DOCUMENTME
   */
  public BindingMap getBindings() {
    return _bindings;
  }

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public Hashtable getEnvironment() {
    return _env;
  }

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   * @throws CommunicationException DOCUMENTME
   */
  public String getNameInNamespace()
                            throws NamingException {
    try {
      return _bindings.getName();
    } catch (RemoteException re) {
      throw new CommunicationException(re.getMessage());
    }
  }

  //--------------------//
  // Naming composition //
  //--------------------//

  /**
   * DOCUMENTME
   *
   * @param name DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   */
  public NameParser getNameParser(String name)
                           throws NamingException {
    if (name.length() == 0) {
      return _defaultNameParser;
    }

    return getNameParser(_defaultNameParser.parse(name));
  }

  /**
   * DOCUMENTME
   *
   * @param name DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   * @throws CommunicationException DOCUMENTME
   * @throws NotContextException DOCUMENTME
   */
  public NameParser getNameParser(Name name)
                           throws NamingException {
    String simple;
    BindingMap bindings;
    Object object;

    // If the first part of the name contains empty parts, we discard
    // them and keep on looking in this context.
    while (!name.isEmpty() && (name.get(0)
                                         .length() == 0))
      name = name.getSuffix(1);

    if (name.isEmpty()) {
      return _defaultNameParser;
    }

    // Simple is the first part of the name for a composite name,
    // for looking up the subcontext, or the last part of the
    // name for looking up the binding.
    simple = name.get(0);
    bindings = _bindings;

    while (name.size() > 1) {
      // Composite name, keep looking in subcontext until we
      // find the binding.
      try {
        object = bindings.get(simple);
      } catch (RemoteException re) {
        throw new CommunicationException(re.getMessage());
      }

      if (object instanceof BindingMap) {
        // Found another binding level, keep looking in that one.
        bindings = (BindingMap)object;
      } else {
        // Could not find another level for this name part,
        // must report that name part is not a subcontext.
        throw new NotContextException(__msgs.msgNotContext(name));
      }

      name = name.getSuffix(1);
      simple = name.get(0);
    }

    return _defaultNameParser;
  }

  //-------------//
  // Environment //
  //-------------//

  /**
   * DOCUMENTME
   *
   * @param name DOCUMENTME
   * @param value DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   * @throws OperationNotSupportedException DOCUMENTME
   */
  public Object addToEnvironment(String name, Object value)
                          throws NamingException {
    if (name.equals(PROP_READONLY)) {
      boolean readOnly;
      readOnly = value.toString()
                      .equalsIgnoreCase("true");

      if (_readOnly && !readOnly) {
        throw new OperationNotSupportedException("Context is read-only");
      }

      _readOnly = readOnly;
    }

    return _env.put(name, value);
  }

  //---------//
  // Binding //
  //---------//

  /**
   * DOCUMENTME
   *
   * @param name DOCUMENTME
   * @param value DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   */
  public void bind(String name, Object value)
            throws NamingException {
    bind(_defaultNameParser.parse(name), value);
  }

  /**
   * DOCUMENTME
   *
   * @param name DOCUMENTME
   * @param value DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   */
  public void bind(Name name, Object value)
            throws NamingException {
    internalBind(name, value, false);
  }

  /**
   * DOCUMENTME
   */
  public void close() {
    _env = null;
    _bindings = null;
  }

  /**
   * DOCUMENTME
   *
   * @param name DOCUMENTME
   * @param prefix DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   */
  public Name composeName(Name name, Name prefix)
                   throws NamingException {
    prefix = (Name)prefix.clone();

    return prefix.addAll(name);
  }

  /**
   * DOCUMENTME
   *
   * @param name DOCUMENTME
   * @param prefix DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   */
  public String composeName(String name, String prefix)
                     throws NamingException {
    return composeName(_defaultNameParser.parse(name),
                       _defaultNameParser.parse(prefix))
             .toString();
  }

  //-------------//
  // Subcontexts //
  //-------------//

  /**
   * DOCUMENTME
   *
   * @param name DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   */
  public Context createSubcontext(String name)
                           throws NamingException {
    return createSubcontext(_defaultNameParser.parse(name));
  }

  /**
   * DOCUMENTME
   *
   * @param name DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   * @throws InvalidNameException DOCUMENTME
   * @throws CommunicationException DOCUMENTME
   * @throws NotContextException DOCUMENTME
   * @throws NameAlreadyBoundException DOCUMENTME
   */
  public Context createSubcontext(Name name)
                           throws NamingException {
    Object object;
    String simple;
    BindingMap bindings;
    BindingMap newBindings;

    if (_readOnly) {
      throw new NamingException(__msgs.msgReadOnly(name));
    }

    // If the first part of the name contains empty parts, we discard
    // them and keep on looking in this context.
    while (!name.isEmpty() && (name.get(0)
                                         .length() == 0))
      name = name.getSuffix(1);

    if (name.isEmpty()) {
      throw new InvalidNameException(__msgs.msgInvalidName(name));
    }

    // Simple is the first part of the name for a composite name,
    // for looking up the subcontext, or the last part of the
    // name for looking up the binding.
    simple = name.get(0);
    bindings = _bindings;

    while (name.size() > 1) {
      // Composite name, keep looking in subcontext until we
      // find the binding.
      try {
        object = bindings.get(simple);
      } catch (RemoteException re) {
        throw new CommunicationException(re.getMessage());
      }

      if (object instanceof BindingMap) {
        // Found another binding level, keep looking in that one.
        bindings = (BindingMap)object;
      } else {
        // Could not find another level for this name part,
        // must report that name part is not a subcontext.
        throw new NotContextException(__msgs.msgNotContext(name));
      }

      name = name.getSuffix(1);
      simple = name.get(0);
    }

    try {
      object = bindings.get(simple);
    } catch (RemoteException re) {
      throw new CommunicationException(re.getMessage());
    }

    if (object != null) {
      throw new NameAlreadyBoundException(__msgs.msgNameAlreadyBound(name));
    }

    // Create a new binding for the subcontex and return a
    // new context.
    try {
      newBindings = bindings.newBindingMap(simple);
    } catch (RemoteException re) {
      throw new CommunicationException(re.getMessage());
    }

    return new BindingMapContextImpl(newBindings, _env);
  }

  /**
   * DOCUMENTME
   *
   * @param name DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   */
  public void destroySubcontext(String name)
                         throws NamingException {
    destroySubcontext(_defaultNameParser.parse(name));
  }

  /**
   * DOCUMENTME
   *
   * @param name DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   * @throws OperationNotSupportedException DOCUMENTME
   * @throws InvalidNameException DOCUMENTME
   * @throws CommunicationException DOCUMENTME
   * @throws NotContextException DOCUMENTME
   */
  public void destroySubcontext(Name name)
                         throws NamingException {
    Object object;
    String simple;
    BindingMap bindings;

    if (_readOnly) {
      throw new OperationNotSupportedException("Context is read-only");
    }

    // If the first part of the name contains empty parts, we discard
    // them and keep on looking in this context.
    while (!name.isEmpty() && (name.get(0)
                                         .length() == 0))
      name = name.getSuffix(1);

    if (name.isEmpty()) {
      throw new InvalidNameException(__msgs.msgInvalidName(name));
    }

    // Simple is the first part of the name for a composite name,
    // for looking up the subcontext, or the last part of the
    // name for looking up the binding.
    simple = name.get(0);
    bindings = _bindings;

    while (name.size() > 1) {
      // Composite name, keep looking in subcontext until we
      // find the binding.
      try {
        object = bindings.get(simple);
      } catch (RemoteException re) {
        throw new CommunicationException(re.getMessage());
      }

      if (object instanceof BindingMap) {
        // Found another binding level, keep looking in that one.
        bindings = (BindingMap)object;
      } else {
        // Could not find another level for this name part,
        // must report that name part is not a subcontext.
        throw new NotContextException(__msgs.msgNotContext(name));
      }

      name = name.getSuffix(1);
      simple = name.get(0);
    }

    try {
      object = bindings.get(simple);

      if (object == null) {
        return;
      }

      if (object instanceof BindingMap) {
        if (!((BindingMap)object).isEmpty()) {
          throw new ContextNotEmptyException(__msgs.msgContextNotEmpty(name));
        }

        ((BindingMap)object).destroy();
        bindings.remove(simple);
      } else {
        throw new NotContextException(__msgs.msgNotContext(name));
      }
    } catch (RemoteException re) {
      throw new CommunicationException(re.getMessage());
    }
  }

  /**
   * @see javax.naming.Context#list(String)
   */
  public NamingEnumeration list(String name)
                         throws NamingException {
    if (name.length() == 0) {
      return new BindingMapEnumeration(_bindings, true, this);
    } else {
      return list(_defaultNameParser.parse(name));
    }
  }

  /**
   * @see javax.naming.Context#list(Name)
   */
  public NamingEnumeration list(Name name)
                         throws NamingException {
    Object object;
    String simple;
    BindingMap bindings;

    // If the first part of the name contains empty parts, we discard
    // them and keep on looking in this context.
    while (!name.isEmpty() && (name.get(0)
                                         .length() == 0))
      name = name.getSuffix(1);

    if (name.isEmpty()) {
      return new BindingMapEnumeration(_bindings, true, this);
    }

    // Simple is the first part of the name for a composite name,
    // for looking up the subcontext, or the last part of the
    // name for looking up the binding.
    simple = name.get(0);
    bindings = _bindings;

    while (name.size() > 1) {
      // Composite name, keep looking in subcontext until we
      // find the binding.
      try {
        object = bindings.get(simple);
      } catch (RemoteException re) {
        throw new CommunicationException(re.getMessage());
      }

      if (object instanceof BindingMap) {
        // Found another binding level, keep looking in that one.
        bindings = (BindingMap)object;
      } else {
        // Could not find another level for this name part,
        // must report that name part is not a subcontext.
        throw new NotContextException(__msgs.msgNotContext(name));
      }

      name = name.getSuffix(1);
      simple = name.get(0);
    }

    // The end of the name is either '.' in which case list the
    // last bindings reached so far, or a name part in which case
    // lookup that binding and list it.
    if (simple.length() == 0) {
      return new BindingMapEnumeration(bindings, true, this);
    }

    try {
      object = bindings.get(simple);
    } catch (RemoteException re) {
      throw new CommunicationException(re.getMessage());
    }

    if (!(object instanceof BindingMap)) {
      throw new NotContextException(__msgs.msgNotContext(name));
    }

    return new BindingMapEnumeration((BindingMap)object, true, this);
  }

  /**
   * @see javax.naming.Context#listBindings(String)
   */
  public NamingEnumeration listBindings(String name)
                                 throws NamingException {
    if (name.length() == 0) {
      return new BindingMapEnumeration(_bindings, false, this);
    } else {
      return listBindings(_defaultNameParser.parse(name));
    }
  }

  /**
   * @see javax.naming.Context#listBindings(Name)
   */
  public NamingEnumeration listBindings(Name name)
                                 throws NamingException {
    Object object;
    String simple;
    BindingMap bindings;

    // If the first part of the name contains empty parts, we discard
    // them and keep on looking in this context.
    while (!name.isEmpty() && (name.get(0)
                                         .length() == 0))
      name = name.getSuffix(1);

    if (name.isEmpty()) {
      return new BindingMapEnumeration(_bindings, false, this);
    }

    simple = name.get(name.size() - 1);
    bindings = findParent(name);

    // The end of the name is either '.' in which case list the
    // last bindings reached so far, or a name part in which case
    // lookup that binding and list it.
    if (simple.length() == 0) {
      return new BindingMapEnumeration(bindings, false,
                                       new BindingMapContextImpl(bindings, _env));
    }

    try {
      object = bindings.get(simple);
    } catch (RemoteException re) {
      throw new NamingException(re.getMessage());
    }

    if (!(object instanceof BindingMap)) {
      throw new NotContextException(__msgs.msgNotContext(name));
    }

    return new BindingMapEnumeration((BindingMap)object, false,
                                     new BindingMapContextImpl((BindingMap)object,
                                                               _env));
  }

  /**
   * @see javax.naming.Context#lookup(String)
   */
  public Object lookup(String name)
                throws NamingException {
    return lookup(_defaultNameParser.parse(name));
  }

  /**
   * @see javax.naming.Context#lookup(Name)
   */
  public Object lookup(Name name)
                throws NamingException {
    return internalLookup(name, true);
  }

  /**
   * DOCUMENTME
   *
   * @param name DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   */
  public Object lookupLink(String name)
                    throws NamingException {
    return lookupLink(_defaultNameParser.parse(name));
  }

  /**
   * DOCUMENTME
   *
   * @param name DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   */
  public Object lookupLink(Name name)
                    throws NamingException {
    return internalLookup(name, false);
  }

  /**
   * DOCUMENTME
   *
   * @param name DOCUMENTME
   * @param value DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   */
  public void rebind(String name, Object value)
              throws NamingException {
    rebind(_defaultNameParser.parse(name), value);
  }

  /**
   * DOCUMENTME
   *
   * @param name DOCUMENTME
   * @param value DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   */
  public void rebind(Name name, Object value)
              throws NamingException {
    internalBind(name, value, true);
  }

  /**
   * DOCUMENTME
   *
   * @param name DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public Object removeFromEnvironment(String name) {
    return _env.remove(name);
  }

  /**
   * DOCUMENTME
   *
   * @param oldName DOCUMENTME
   * @param newName DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   */
  public void rename(String oldName, String newName)
              throws NamingException {
    rename(_defaultNameParser.parse(oldName), _defaultNameParser.parse(newName));
  }

  /**
   * @see javax.naming.Context#rename(Name, Name)
   */
  public void rename(Name oldName, Name newName)
              throws NamingException {
    if (_readOnly) {
      throw new NamingException(__msgs.msgReadOnly(oldName));
    }

    BindingMap src = findParent(oldName);
    BindingMap dst = findParent(newName);

    if (!src.equals(dst)) {
      throw new NamingException(__msgs.msgNotSameContext(oldName, newName));
    }

    try {
      src.rename(oldName.get(oldName.size() - 1),
                 newName.get(newName.size() - 1));
    } catch (RemoteException re) {
      throw new CommunicationException(__msgs.msgCommunicationError(re
                                                                  .getMessage()));
    } catch (NameNotFoundException nnfe) {
      throw new NameNotFoundException(__msgs.msgNameNotFound(oldName));
    } catch (NameAlreadyBoundException nab) {
      throw new NameAlreadyBoundException(__msgs.msgNameAlreadyBound(newName));
    }
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    try {
      if (_readOnly) {
        return _bindings.getName() + " (read-only)";
      } else {
        return _bindings.getName();
      }
    } catch (Exception re) {
      throw new RuntimeException(re.getMessage());
    }
  }

  /**
   * DOCUMENTME
   *
   * @param name DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   */
  public void unbind(String name)
              throws NamingException {
    unbind(_defaultNameParser.parse(name));
  }

  /**
   * DOCUMENTME
   *
   * @param name DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   * @throws InvalidNameException DOCUMENTME
   * @throws NotContextException DOCUMENTME
   * @throws CommunicationException DOCUMENTME
   */
  public void unbind(Name name)
              throws NamingException {
    Object object;
    String simple;
    BindingMap bindings;

    if (_readOnly) {
      throw new NamingException(__msgs.msgReadOnly(name));
    }

    // If the first part of the name contains empty parts, we discard
    // them and keep on looking in this context.
    while (!name.isEmpty() && (name.get(0)
                                         .length() == 0))
      name = name.getSuffix(1);

    if (name.isEmpty()) {
      throw new InvalidNameException(__msgs.msgInvalidName(name));
    }

    // Simple is the first part of the name for a composite name,
    // for looking up the subcontext, or the last part of the
    // name for looking up the binding.
    simple = name.get(0);
    bindings = _bindings;

    while (name.size() > 1) {
      // Composite name, keep looking in subcontext until we
      // find the binding.
      try {
        object = bindings.get(simple);
      } catch (RemoteException re) {
        throw new NamingException(re.getMessage());
      }

      if (object instanceof BindingMap) {
        // Found another binding level, keep looking in that one.
        bindings = (BindingMap)object;
      } else {
        // Could not find another level for this name part,
        // must report that name part is not a subcontext.
        throw new NotContextException(__msgs.msgNotContext(name));
      }

      name = name.getSuffix(1);
      simple = name.get(0);
    }

    // If the name is direct, we perform the unbinding in
    // this context. This method is idempotent;
    try {
      bindings.remove(simple);
    } catch (RemoteException re) {
      throw new CommunicationException(re.getMessage());
    }
  }

  private BindingMap findParent(Name name)
                         throws NamingException {
    Name remName = name;
    String simple = remName.get(0);
    BindingMap bindings = _bindings;

    while (remName.size() > 1) {
      Object object;

      // Composite name, keep looking in subcontext until we
      // find the binding.
      try {
        object = bindings.get(simple);
      } catch (RemoteException re) {
        throw new CommunicationException(re.getMessage());
      }

      if (!(object instanceof BindingMap)) {
        throw new NotContextException(__msgs.msgNotContext(name));
      }

      bindings = (BindingMap)object;
      remName = remName.getSuffix(1);
      simple = remName.get(0);
    }

    return bindings;
  }

  private void internalBind(Name name, Object value, boolean rebind)
                     throws NamingException {
    if (_readOnly) {
      throw new NamingException(__msgs.msgReadOnly(name));
    }

    // If the first part of the name contains empty parts, we discard
    // them and keep on looking in this context.
    while (!name.isEmpty() && (name.get(0)
                                         .length() == 0))
      name = name.getSuffix(1);

    if (name.isEmpty()) {
      throw new InvalidNameException(__msgs.msgInvalidName(name));
    }

    // Simple is the first part of the name for a composite name,
    // for looking up the subcontext, or the last part of the
    // name for looking up the binding.
    BindingMap bindings = findParent(name);
    String simple = name.get(name.size() - 1);

    try {
      if (!rebind && bindings.contains(simple)) {
        throw new NameAlreadyBoundException(__msgs.msgNameAlreadyBound(name));
      }
    } catch (RemoteException re) {
      throw new CommunicationException(__msgs.msgCommunicationError(re
                                                                  .getMessage()));
    }

    try {
      if (value instanceof Reference) {
        bindings.put(simple, (Reference)value);
      } else if (value instanceof Referenceable) {
        Reference ref = ((Referenceable)value).getReference();
        bindings.put(simple, ref);
      } else if (value instanceof Serializable) {
        bindings.put(simple,
                     SerializableObjFactory.createSerializableRef(value));
      } else if (value instanceof RemoteObject) {
        bindings.put(simple,
                     SerializableObjFactory.createSerializableRef(value));
      } else {
        throw new NamingException(__msgs.msgNotBindable(name,
                                                      value.getClass().getName()));
      }
    } catch (RemoteException re) {
      throw new NamingException(re.getMessage());
    }
  }

  private Object internalLookup(Name name, boolean resolveLinkRef)
                         throws NamingException {
    // If the first part of the name contains empty parts, we discard
    // them and keep on looking in this context. If the name is empty,
    // we create a new context similar to this one.
    while (!name.isEmpty() && (name.get(0)
                                         .length() == 0))
      name = name.getSuffix(1);

    if (name.isEmpty()) {
      return new BindingMapContextImpl(_bindings, _env);
    }

    BindingMap bindings = findParent(name);
    String simple = name.get(name.size() - 1);
    Object object;

    // At this point name.size() == 1 and simple == name.get( 0 ).
    try {
      object = bindings.get(simple);
    } catch (RemoteException re) {
      throw new NamingException(re.getMessage());
    }

    if (object == null) {
      throw new NameNotFoundException(__msgs.msgNameNotFound(simple,
                                                           name.getPrefix(name
                                                                          .size()
                                                                          - 1)));
    }

    if (object instanceof BindingMap) {
      // If we found a subcontext, we must return a new context
      // to represent it and keep the environment set for this
      // context (e.g. read-only).
      return new BindingMapContextImpl((BindingMap)object, _env);
    } else if (object instanceof Reference) {
      // Reconstruct a reference
      Object prev = null;

      try {
        do {
          prev = object;
          object = NamingManager.getObjectInstance(prev, name,
                                                   new BindingMapContextImpl(bindings,
                                                                             _env),
                                                   _env);
        } while ((object != null) && (object instanceof Reference)
                       && (prev != object));

        return object;
      } catch (Exception except) {
        NamingException foo = new NamingException(__msgs.msgDeRefError(name));
        foo.setRootCause(except);
        throw foo;
      }
    } else {
      return object;
    }
  }
}
