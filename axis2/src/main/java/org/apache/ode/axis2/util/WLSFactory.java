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

package org.apache.ode.axis2.util;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

/**
 * <code>TransactionManager</code> factory for Weblogic Server 10.0.
 */
public class WLSFactory {

    /**
     * Default constructor needed.
     */
    public WLSFactory() { }

    public TransactionManager getTransactionManager() {

        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "weblogic.jndi.WLInitialContextFactory");

        // Parameters for the WebLogic Server - not mandatory.
        // Substitute the correct hostname, port number
        // user name, and password for your environment:
        // env.put(Context.PROVIDER_URL, "t3://localhost:7001");
        // env.put(Context.SECURITY_PRINCIPAL, "admin");
        // env.put(Context.SECURITY_CREDENTIALS, "password");

        TransactionManager tm = null;
        try {
            Context ctx = new InitialContext(env);
            tm = (TransactionManager) ctx.lookup("javax.transaction.TransactionManager");
        } catch (NamingException e) {
            e.printStackTrace();
        }

        return tm;
    }
}
