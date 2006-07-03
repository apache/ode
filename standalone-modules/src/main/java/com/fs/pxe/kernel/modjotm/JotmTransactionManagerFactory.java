/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modjotm;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * An {@link ObjectFactory} implementation that can be used to bind the
 * JOTM {@link TransactionManager} implementation in JNDI.
 */ 
public class JotmTransactionManagerFactory implements ObjectFactory {

  public Object getObjectInstance(Object objref, Name name, Context ctx, Hashtable env) throws Exception {
    Reference ref = (Reference) objref;

    if (ref.getClassName().equals(TransactionManager.class.getName())) {
      return ModJOTM.getTransactionManager(ref);
    }
    else if (ref.getClassName().equals(UserTransaction.class.getName())) {
      return ModJOTM.getUserTransaction(ref);
    }

    throw new RuntimeException("The reference class name \"" + ref.getClassName() + "\" is unknown.");
  }

}
