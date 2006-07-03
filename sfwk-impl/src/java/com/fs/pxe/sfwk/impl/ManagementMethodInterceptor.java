/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * An interceptor for establishing a management context.
 */
class ManagementMethodInterceptor implements InvocationHandler {
  static Log __log = LogFactory.getLog(ManagementMethodInterceptor.class);

  private Object _target;
  private DomainNodeImpl _domainNode;

  /** Constructor. */
  ManagementMethodInterceptor(DomainNodeImpl domainNode, Object target) {
    _domainNode = domainNode;
    _target = target;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    _domainNode.associateWithThread();

    try {
      return method.invoke(_target, args);
    } catch (InvocationTargetException ite) {
      throw ite.getTargetException();
    } finally {
      _domainNode.disassociateFromThread();
    }
  }
}
