/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.spi.InteractionHandler;
import com.fs.pxe.sfwk.spi.PxeException;
import com.fs.utils.ObjectPrinter;
import com.fs.utils.Reflect;
import com.fs.utils.msg.MessageBundle;
import com.fs.utils.uuid.UUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.transaction.TransactionManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Service Provider Session backend.
 */
class SpSessionBackend {
  private static final Log __log = LogFactory.getLog(SpSessionBackend.class);
  private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

  // TODO the Transactionmanager does not seem to be used?
  private final TransactionManager _txm;
  private final long _createTime = System.currentTimeMillis();
  private final String _uuid = new UUID().toString();
  private final Map<String, Method> _methodMap = new HashMap<String, Method>();
  private Object _handler;
  private final Class _interactionClass;
  private ServiceProviderBackend _serviceProviderBackend;

  SpSessionBackend(ServiceProviderBackend spb,
                    TransactionManager txm,
                    Object listener,
                    Class interactionClass) {
    super();

    _serviceProviderBackend = spb;
    _txm = txm;
    _handler = listener;
    _interactionClass = interactionClass;

    assert interactionClass != null;
    assert interactionClass.isInterface();

    Method methods[] = _interactionClass.getMethods();

    for (int i = 0 ; i < methods.length; ++i) {
      assert !_methodMap.containsKey(methods[i].getName());
      _methodMap.put(Reflect.generateMethodSignature(methods[i]), methods[i]);
    }
  }

  String getId() { return _uuid; }

  /**
   * Invoke a method on the {@link InteractionHandler} object. The service
   * provider can expect that no multi-threaded invocations will be made
   * on the same {@link InteractionHandler} object (this class ensures
   * that this is the case).
   * @param name method name
   * @param args method arguments
   * @return method return value
   * @throws PxeException
   * @throws InvocationTargetException
   */
  synchronized Object invoke(String name, Object[] args) throws PxeException, InvocationTargetException {
    if (_handler == null) {
      throw new PxeExceptionImpl(__msgs.msgSessionConnectionClosed(), null);
    }

    Method method = _methodMap.get(name);

    if (method == null) {
      throw new PxeExceptionImpl("No such method: " + name + "; methodMap=" + _methodMap, null);
    }

    try {
      // Only one invoke at a time for each session.
      return method.invoke(_handler, args);
    } catch (InvocationTargetException ite) {
      Throwable target = ite.getTargetException();
      Class allowed[] = method.getExceptionTypes();
      for (Class anAllowed : allowed) {
        if (anAllowed.isAssignableFrom(target.getClass()))
          throw ite;
      }
      String errmsg =
              __msgs.msgSessionUnexpectedException(
                      _serviceProviderBackend.getSpClass().getName(),
                      _serviceProviderBackend.getSpURI(),
                      target.getClass().getName());
      __log.error(errmsg, target);
      throw new PxeExceptionImpl(errmsg, target);
    } catch (Throwable ex) {
      String errmsg = __msgs.msgSessionUnexpectedException(
              _serviceProviderBackend.getSpClass().getName(),
              _serviceProviderBackend.getSpURI(),
              ex.getClass().getName());
      throw new PxeExceptionImpl(errmsg, ex);
    } finally {
    }
  }


  /**
   * Close this session.
   */
  synchronized void close() {
    if (_handler == null)
      return;
    _handler = null;
  }

  public String toString() {
    return ObjectPrinter.toString(this,new Object[] {
      "id", _uuid,
      "createTime", Long.valueOf(_createTime),
      "listener", _handler });
  }

}
