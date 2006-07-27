/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.engine;

import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.iapi.Scheduler;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Encapusulates access to a BPEL process database.
 */
class BpelProcessDatabase extends BpelDatabase {
  static Log __log = LogFactory.getLog(BpelProcessDatabase.class);

  private QName _processId;

  /**
   * Constructor.
   * @param sscf BPEL state store connection factory
   * @param txm JTA transaction manager
   * @param processId name of the process
   */
	BpelProcessDatabase(BpelDAOConnectionFactory sscf,
                      Scheduler scheduler,
                      QName processId) {
    super(sscf,scheduler);
    _processId = processId;
	}
  
  QName getProcessId() {
    return _processId;
  }

  ProcessDAO getProcessDAO() {
    return getConnection().getProcess(_processId);

  }

  abstract class Callable<T> implements BpelDatabase.Callable<T> {
    public T exec() throws Exception {
      return BpelProcessDatabase.this.exec(this);
    }

    protected ProcessDAO getProcessDAO() {
      return BpelProcessDatabase.this.getProcessDAO();
    }
  }

}
