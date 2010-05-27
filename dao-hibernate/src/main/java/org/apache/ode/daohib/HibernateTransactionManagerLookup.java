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
package org.apache.ode.daohib;

import java.util.Properties;

import javax.transaction.TransactionManager;
import javax.transaction.Transaction;

import org.hibernate.HibernateException;
import org.hibernate.transaction.TransactionManagerLookup;

/**
 * Implementation of the {@link org.hibernate.transaction.TransactionManagerLookup} interface that
 * uses {@link SessionManager} to obtain the JTA {@link TransactionManager} object.
 */
public class HibernateTransactionManagerLookup implements TransactionManagerLookup {

    /** Constructor. */
    public HibernateTransactionManagerLookup() {
        super();
    }

    public TransactionManager getTransactionManager(Properties props)
            throws HibernateException {
        return SessionManager.getTransactionManager(props);
    }

    public String getUserTransactionName() {
        return null;
    }

    public Object getTransactionIdentifier(Transaction transaction) {
        return transaction;
    }
}
