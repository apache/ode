/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
