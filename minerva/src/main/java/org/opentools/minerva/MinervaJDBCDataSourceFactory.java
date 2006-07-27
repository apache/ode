/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.opentools.minerva;

import java.util.Hashtable;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;

import org.opentools.minerva.connector.jdbc.JDBCDataSource;


/**
 * Uses JCA style
 */
public class MinervaJDBCDataSourceFactory implements ObjectFactory {
  
  static final String REF_ID = "refId";

  /**
   * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object,
   *      javax.naming.Name, javax.naming.Context, java.util.Hashtable)
   */
  public Object getObjectInstance(Object obj, Name n, Context nameCtx,
                                  Hashtable environment)
                           throws Exception {
    Reference ref = (Reference)obj;
    MinervaPool pool = MinervaPool.get((String)((StringRefAddr)ref.get(REF_ID)).getContent());
    return new JDBCDataSource(pool._connManager, pool._mcf);
  }
  
}
