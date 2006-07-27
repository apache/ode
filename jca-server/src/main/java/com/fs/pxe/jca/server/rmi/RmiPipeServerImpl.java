/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */


package com.fs.pxe.jca.server.rmi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.WeakHashMap;

import com.fs.pxe.jca.server.Interaction;
import com.fs.pxe.ra.transports.rmi.PxeTransportPipeRemote;
import com.fs.utils.Reflect;


/**
 * Implementation of an RMI-based transport pipe.
 */
class RmiPipeServerImpl implements PxeTransportPipeRemote {

  private RmiTransportServerImpl _server;
  private String[] _connectionClass;
  private HashMap<String,Method> _methodMap = new HashMap<String,Method>();

  final long createTime = System.currentTimeMillis();
  long lastActivityTime = createTime;

  PxeTransportPipeRemote remote;
  Object target;
  private WeakHashMap<Object,RmiPipeServerImpl> _interactions = new WeakHashMap<Object,RmiPipeServerImpl>();

  /** Constructor. */
  public RmiPipeServerImpl(RmiTransportServerImpl server, Object target, String[] connectionClass) {
    _server = server;
    _connectionClass = connectionClass;
    
    ArrayList<String> connectionClassNames = new ArrayList<String>();
    for (String cn : connectionClass)
      connectionClassNames.add(cn);
    
    for (Class i : target.getClass().getInterfaces()) {
//      if (!connectionClassNames.contains(i.getName()))
//        continue;
      for (Method m : i.getMethods()) 
        _methodMap.put(Reflect.generateMethodSignature(m),m);
    }
    this.target = target;
  }

  public void close() {
    _server.pipeClosed(this);
    remote = null;
  }

  public String[] getConnectionClassNames() {
    return _connectionClass;
  }

  public Object invokeConnectionMethod(String name, Object[] args) throws RemoteException, InvocationTargetException {
    lastActivityTime = System.currentTimeMillis();
    Method m = _methodMap.get(name);

    if (m == null)
      throw new RemoteException("Unknown method: " + name);
    
    ClassLoader old = Thread.currentThread().getContextClassLoader();
    Thread.currentThread()
          .setContextClassLoader(getClass().getClassLoader());
    try {
      Object ret = m.invoke(target,args);
      if (ret != null && ret instanceof Interaction) {
        RmiPipeServerImpl remoteProxy = _interactions.get(ret);
        if (remoteProxy != null)
          return remoteProxy;
        Class[]interfaces = ret.getClass().getInterfaces();
        String inames[] = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; ++i)
          inames[i] = interfaces[i].getName();
        remoteProxy = new RmiPipeServerImpl(_server,ret, inames);
        _interactions.put(ret,remoteProxy);
        return remoteProxy;
      }
      return ret;
    } catch (IllegalArgumentException e) {
      throw new RemoteException("Illegal Argument", e);
    } catch (IllegalAccessException e) {
      throw new RemoteException("Illegal Access", e);
    } catch (InvocationTargetException e) {
      throw e;
    } finally {
      Thread.currentThread().setContextClassLoader(old);
    }
  }
}
