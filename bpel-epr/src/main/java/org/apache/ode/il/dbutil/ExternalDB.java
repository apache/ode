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

package org.apache.ode.il.dbutil;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.ode.il.config.OdeConfigProperties;

public class ExternalDB extends Database {

    public ExternalDB(OdeConfigProperties props) {
        super(props);
    }

    @Override
    protected void initDataSource() throws DatabaseConfigException {
        try {
            _datasource = (DataSource) lookupInJndi(_odeConfig.getDbDataSource());
            __log.info(__msgs.msgOdeUsingExternalDb(_odeConfig.getDbDataSource()));
        } catch (Exception ex) {
            String msg = __msgs.msgOdeInitExternalDbFailed(_odeConfig.getDbDataSource());
            __log.error(msg, ex);
            throw new DatabaseConfigException(msg, ex);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> T lookupInJndi(String objName) throws Exception {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            InitialContext ctx = null;
            try {
                ctx = new InitialContext();
                return (T) ctx.lookup(objName);
            } finally {
                if (ctx != null)
                    try {
                        ctx.close();
                    } catch (Exception ex1) {
                        __log.error("Error closing JNDI connection.", ex1);
                    }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }
}
