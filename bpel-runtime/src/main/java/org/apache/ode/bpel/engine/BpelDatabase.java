/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.engine;



import javax.xml.namespace.QName;

import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.iapi.Scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Encapsulates transactional access to the BPEL database.
 */
class BpelDatabase {
  static Log __log = LogFactory.getLog(BpelDatabase.class);

  protected BpelDAOConnectionFactory _sscf;
  protected Scheduler _scheduler;

  BpelDatabase(BpelDAOConnectionFactory sscf, Scheduler scheduler) {
    if (sscf == null)
      throw new NullPointerException("sscf is null!");
    if (scheduler == null)
      throw new NullPointerException("scheduler is null!");
    
    _sscf = sscf;
    _scheduler = scheduler;
    
  }

  /**
   * Get a connection to the database with the correct store identifier.
   * @return a state store connection
   * @throws org.apache.ode.utils.dao.DConnectionException
   */
  BpelDAOConnection getConnection() {
    // Note: this will give us a connection associated with the current
    // transaction, so no need to worry about closing it.
    return _sscf.getConnection();
  }

  BpelProcessDatabase getProcessDb(QName pid) {
    return new BpelProcessDatabase(_sscf, _scheduler, pid);
  }

  /**
   * Execute a self-contained database transaction.
   * @param callable database transaction
   * @return
   * @throws DConnectionException
   */
  <T> T exec(final Callable<T> callable) throws Exception {
    return _scheduler.execTransaction(new java.util.concurrent.Callable<T>() {
      public T call() throws Exception {
        return callable.run(_sscf.getConnection());
      }
    });
  }

  interface Callable<T> {
     public T run(BpelDAOConnection conn) throws Exception;
  }
}
