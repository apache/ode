/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.jacob.vpu;

import com.fs.jacob.Channel;
import com.fs.utils.ArrayUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ChannelFactory {
  private static final Method METHOD_OBJECT_EQUALS;
  private static final Method METHOD_CHANNEL_EXPORT;

  static {
    try {
      METHOD_OBJECT_EQUALS = Object.class.getMethod("equals", new Class[] { Object.class });
    } catch (Exception e) {
      throw new AssertionError("No equals(Object) method on Object!");
    }

    try {
      METHOD_CHANNEL_EXPORT = Channel.class.getMethod("export", ArrayUtils.EMPTY_CLASS_ARRAY);
    } catch (Exception e) {
      throw new AssertionError("No export() method on Object!");
    }
  }

  /**
   * DOCUMENTME
   *
   * @param channel DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public static Object getBackend(Channel channel) {
    ChannelInvocationHandler cih = (ChannelInvocationHandler)Proxy.getInvocationHandler(channel);
    return cih._backend;
  }


  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public static Channel createChannel(Object backend, Class type) {
    InvocationHandler h = new ChannelInvocationHandler(backend);
    Class[] ifaces = new Class[] { Channel.class, type };
    Object proxy = Proxy.newProxyInstance(Channel.class.getClassLoader(), ifaces, h);
    return (Channel)proxy;
  }

  public static final class ChannelInvocationHandler implements InvocationHandler {
    private Object _backend;

    ChannelInvocationHandler(Object backend) {
      _backend = backend;
    }

    public String toString() {
      return _backend.toString();
    }

    public boolean equals(Object other) {
      return ((ChannelInvocationHandler)other)._backend.equals(_backend);
    }

    public int hashCode() {
      return _backend.hashCode();
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (method.getDeclaringClass() == Object.class) {
        if (method.equals(METHOD_OBJECT_EQUALS)) {
          return Boolean.valueOf(this.equals(Proxy.getInvocationHandler(args[0])));
        }

        return method.invoke(this, args);
      }

      if (method.equals(METHOD_CHANNEL_EXPORT)) {
        return JacobVPU.activeJacobThread().exportChannel((Channel)proxy);
      }

      return JacobVPU.activeJacobThread().message((Channel)proxy, method, args);
    }

  }

}

