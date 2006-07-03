/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.jacob;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Base-class for method-list objects. Method-lists objects should extends
 * this class <em>and</em> implement one <code>Channel</code> interface.
 */
public abstract class ML<CT extends Channel> extends JavaClosure {
  private static Log __log = LogFactory.getLog(ML.class);

  private transient Set<Method> _implementedMethods;
  private transient CT _channel;

  protected ML(CT channel)
        throws IllegalStateException {

    if (this.getClass()
                  .getSuperclass()
                  .getSuperclass() != ML.class) {
      throw new IllegalStateException("Inheritence in ML classes not allowed!");
    }

    if (channel == null)
      throw new IllegalArgumentException("Null channel!");
    _channel = channel;

  }

  public CT getChannel() { return _channel; }

  public void setChannel(CT channel) { _channel = channel; }

  public Set<ML> or(ML other) {
    HashSet<ML> retval = new HashSet<ML>();
    retval.add(this);
    retval.add(other);
    return retval;
  }

  public Set<ML> or(Set<ML> other) {
    HashSet<ML> retval = new HashSet<ML>(other);
    retval.add(this);
    return retval;
  }
  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public Set<Method> getImplementedMethods() {
    if (_implementedMethods == null) {
    	Set<Method> implementedMethods = new HashSet<Method>();
      getImplementedMethods(implementedMethods, getClass().getSuperclass()); 
      _implementedMethods = Collections.unmodifiableSet(implementedMethods);
    }

    return _implementedMethods;
  }
  
  private Set<Method> getImplementedMethods(Set<Method> methods, Class clazz){
  	Class[] interfaces = clazz.getInterfaces();

    for (int i = 0 ; i < interfaces.length; ++i) {
      if (interfaces[i] != Channel.class) {
      	Method[] allmethods = interfaces[i].getDeclaredMethods();

        for (int j = 0; j < allmethods.length; ++j) {
        	methods.add(allmethods[j]);
        }
        
        getImplementedMethods(methods, interfaces[i]);
      }
    }
  	return methods;
  }

  /**
   * DOCUMENTME
   *
   * @param method DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public Method getMethod(String method) {
    for (Iterator<Method> i = getImplementedMethods().iterator();i.hasNext();) {
      Method meth = i.next();
      if (meth.getName().equals(method)) {
        return meth;
      }
    }
    
    assert !_implementedMethods.contains(method);

    throw new IllegalArgumentException("No such method: " + method + " in " +  _implementedMethods);

  }

  /**
   * Get a description of the object for debugging purposes.
   *
   * @return human-readable description.
   */
  public String toString() {
    StringBuffer buf = new StringBuffer(getClassName());
    buf.append('{');
    for (Iterator<Method> i = getImplementedMethods().iterator(); i.hasNext();) {
      Method method = i.next();
      buf.append(method.getName());
      buf.append("()");

      if (i.hasNext()) {
        buf.append("&");
      }
    }

    buf.append('}');

    return buf.toString();
  }

  protected Log log() {
    return __log;
  }
}
