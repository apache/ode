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
package org.apache.ode.dao.jpa;

import javax.persistence.EntityManager;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.dao.DAOConnection;

public class JpaConnection implements DAOConnection {

    private static final Log __log = LogFactory.getLog(JpaConnection.class);
    final protected EntityManager _em;
    final protected TransactionManager _mgr;
    final protected JpaTxContext _txCtx;
    final protected JpaOperator _operator;

    public JpaConnection(EntityManager em, TransactionManager mgr, JpaOperator operator) {
        _em = em;
        _mgr = mgr;
        if (mgr != null) {
            _txCtx = new JpaJtaContext();
        } else {
            _txCtx = new JpaNonTxContext();
        }
        _operator = operator;
    }

    public EntityManager getEntityManager() {
        return _em;
    }

    public JpaOperator getJPADaoOperator() {
        return _operator;
    }

    public void close() {
    	
    }

    public boolean isClosed() {
        return _em == null ? true : !_em.isOpen();
    }

    /** Clear out the entity manager after a commit so no stale entites are
     *  preserved across transactions and all JPA operations pull data directly from
     *  the DB.
     *
     */
    public void clearOnComplete() {
        try {
            _mgr.getTransaction().registerSynchronization(new Synchronization() {

                public void afterCompletion(int i) {
                    _em.clear();
                    if(__log.isDebugEnabled()) {
                    	__log.debug("-------> clear the entity manager");
                    }
                }

                public void beforeCompletion() {
                }
            });
        } catch (Exception e) {
            __log.error("Error adding commit synchronizer", e);
        }
    }

    protected interface JpaTxContext {

        public void begin();

        public void commit();

        public void rollback();
    }

    class JpaJtaContext implements JpaTxContext {

        /**
         * Due to the way ODE re-uses connection on ThreadLocals it could be possible
         * for the JPA EntityManager to not be created on the current JTA transaction
         * and threfore it must by manually bound to the current transaction.
         */
        public void begin() {
            try {
                if (_mgr.getStatus() == Status.STATUS_ACTIVE) {
                    _em.joinTransaction();
                    clearOnComplete();
                }
            } catch (SystemException se) {
                __log.error(se);
            }
        }

        public void commit() {
        }

        public void rollback() {
            try {
                if (_mgr.getStatus() == Status.STATUS_ACTIVE) {
                    _mgr.setRollbackOnly();
                }
            } catch (Exception ex) {
                __log.error("Unable to set rollbackOnly", ex);
            }
        }
    }

    class JpaNonTxContext implements JpaTxContext {

        public void begin() {
            _em.getTransaction().begin();
        }

        public void commit() {
            _em.getTransaction().commit();
            _em.clear();
        }

        public void rollback() {
            _em.getTransaction().rollback();
            _em.clear();
        }
    }
}
