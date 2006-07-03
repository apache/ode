/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modhibernatedao;

import com.fs.pxe.bpel.dao.BpelDAOConnectionFactory;
import com.fs.pxe.sfwk.bapi.dao.DAOConnectionFactory;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 * JNDI {@link ObjectFactory} implementation for Hibernate-based
 * connection factory objects. 
 */
public class HibernateDaoObjectFactory implements ObjectFactory {

  public Object getObjectInstance(Object objref, Name name, Context ctx, Hashtable env)
    throws Exception
  {
    Reference ref = (Reference) objref;
    if (ref.getClassName().equals(DAOConnectionFactory.class.getName())) {
      return ModHibernateDAO.getInstance(ref)._sscfImpl;
    } else if (ref.getClassName().equals(BpelDAOConnectionFactory.class.getName())) {
      return ModHibernateDAO.getInstance(ref)._bpelSscfImpl;
    }

    throw new RuntimeException("The reference class name \"" + ref.getClassName() + "\" is unknown.");
  }

}
