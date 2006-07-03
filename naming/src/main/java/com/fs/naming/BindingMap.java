/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.naming;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.naming.*;

/**
 * Remote JNDI binding interface. This interface defines an abstract
 * representation of a JNDI store, that may be accessed through some remote
 * protocol.
 */
public interface BindingMap extends Remote {
  /**
   * Check whether this
   *
   * @return DOCUMENTME
   *
   * @throws RemoteException indicated communication error
   */
  public boolean isEmpty()
                  throws RemoteException;

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   * @throws RemoteException indicated communication error
   */
  public String getName()
                 throws NamingException, RemoteException;

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws RemoteException indicated communication error
   */
  public NameParser getNameParser()
                           throws RemoteException;

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws RemoteException indicated communication error
   */
  public boolean isRoot()
                 throws RemoteException;

  /**
   * DOCUMENTME
   *
   * @param simpleName DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   * @throws RemoteException DOCUMENTME
   */
  public boolean contains(String simpleName)
                   throws NamingException, RemoteException;

  /**
   * DOCUMENTME
   *
   * @throws RemoteException indicated communication error
   */
  public void destroy()
               throws RemoteException;

  /**
   * DOCUMENTME
   *
   * @param key DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   * @throws RemoteException indicated communication error
   */
  public Object get(String key)
             throws NamingException, RemoteException;

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws RemoteException indicated communication error
   */
  public String[] keys()
                throws RemoteException;

  /**
   * DOCUMENTME
   *
   * @param simpleName DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws NameAlreadyBoundException DOCUMENTME
   * @throws RemoteException indicated communication error
   */
  public BindingMap newBindingMap(String simpleName)
                           throws NameAlreadyBoundException, RemoteException;

  /**
   * DOCUMENTME
   *
   * @param simpleName DOCUMENTME
   * @param value DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   * @throws RemoteException indicated communication error
   */
  public void put(String simpleName, Reference value)
           throws NamingException, RemoteException;

  /**
   * DOCUMENTME
   *
   * @param simpleName name of object to be remove
   *
   * @throws NameNotFoundException DOCUMENTME
   * @throws NamingException DOCUMENTME
   * @throws RemoteException indicated communication error
   */
  public void remove(String simpleName)
              throws NameNotFoundException, NamingException, RemoteException;

  /**
   * Change the "name" part of a binding in this store.
   *
   * @param oldName DOCUMENTME
   * @param newName DOCUMENTME
   *
   * @throws NameAlreadyBoundException DOCUMENTME
   * @throws NameNotFoundException DOCUMENTME
   * @throws NamingException DOCUMENTME
   * @throws RemoteException DOCUMENTME
   */
  public void rename(String oldName, String newName)
              throws NameAlreadyBoundException, NameNotFoundException, 
                     NamingException, RemoteException;
}
