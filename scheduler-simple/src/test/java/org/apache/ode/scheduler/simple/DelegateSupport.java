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

package org.apache.ode.scheduler.simple;

import java.sql.Connection;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.ode.utils.GUID;
import org.hsqldb.jdbc.jdbcDataSource;


/**
 * Support class for creating a JDBC delegate (using in-mem HSQL db).
 * 
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 */
public class DelegateSupport {

    protected DataSource _ds;
    protected JdbcDelegate _del;

    public DelegateSupport() throws Exception {
    	this(null);
    }

    public DelegateSupport(TransactionManager txm) throws Exception {
    	initialize(txm);
    }

    protected void initialize(TransactionManager txm) throws Exception {
        jdbcDataSource ds = new jdbcDataSource();
        ds.setDatabase("jdbc:hsqldb:mem:" + new GUID().toString());
        ds.setUser("sa");
        ds.setPassword("");
        _ds = ds;
        
        setup();
        _del = new JdbcDelegate(_ds);
    }
    
    public DatabaseDelegate delegate() {
        return _del;
    }
    
    public void setup() throws Exception {
        Connection c = _ds.getConnection();
        try {
            c.createStatement().executeUpdate("CREATE ALIAS MOD FOR \"org.apache.ode.scheduler.simple.DelegateSupport.mod\";");
            String sql = "CREATE TABLE \"ODE_JOB\" (\"JOBID\" CHAR(64) NOT NULL, \"TS\" NUMERIC  NOT NULL, \"NODEID\" char(64)  NULL, \"SCHEDULED\" int  NOT NULL, \"TRANSACTED\" int  NOT NULL, \"DETAILS\" BINARY(4096)  NULL, PRIMARY KEY(\"JOBID\"));";
            c.createStatement().executeUpdate(sql);
        } finally {
            c.close();
        }
        
    }
    
    public static long mod(long a, long b) {
        return a % b;
    }
}

