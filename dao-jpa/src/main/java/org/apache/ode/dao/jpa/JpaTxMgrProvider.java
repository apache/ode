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

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.openjpa.ee.ManagedRuntime;
import org.apache.openjpa.util.GeneralException;

public class JpaTxMgrProvider implements ManagedRuntime {
    private TransactionManager _txMgr;

    public JpaTxMgrProvider(TransactionManager txMgr) {
        _txMgr = txMgr;
    }

    public TransactionManager getTransactionManager() throws Exception {
        return _txMgr;
    }

    public void setRollbackOnly(Throwable cause) throws Exception {
        // there is no generic support for setting the rollback cause
        getTransactionManager().getTransaction().setRollbackOnly();
    }

    public Throwable getRollbackCause() throws Exception {
        // there is no generic support for setting the rollback cause
        return null;
    }

    public Object getTransactionKey() throws Exception, SystemException {
        return _txMgr.getTransaction();
    }

    public void doNonTransactionalWork(java.lang.Runnable runnable) throws NotSupportedException {
        TransactionManager tm = null;
        Transaction transaction = null;

        try {
            tm = getTransactionManager();
            transaction = tm.suspend();
        } catch (Exception e) {
            NotSupportedException nse =
                new NotSupportedException(e.getMessage());
            nse.initCause(e);
            throw nse;
        }

        runnable.run();

        try {
            tm.resume(transaction);
        } catch (Exception e) {
            try {
                transaction.setRollbackOnly();
            }
            catch(SystemException se2) {
                throw new GeneralException(se2);
            }
            NotSupportedException nse =
                new NotSupportedException(e.getMessage());
            nse.initCause(e);
            throw nse;
        }
    }
}