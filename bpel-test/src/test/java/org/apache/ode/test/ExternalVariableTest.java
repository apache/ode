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

package org.apache.ode.test;

import java.sql.Connection;
import java.sql.Statement;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.extvar.jdbc.JdbcExternalVariableModule;
import org.apache.ode.utils.GUID;
import org.hsqldb.jdbc.jdbcDataSource;
import org.junit.Test;

/**
 * Simple test of external variables.
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 * 
 */
public class ExternalVariableTest extends BPELTestAbstract {

    private JdbcExternalVariableModule _jdbcext;

    private jdbcDataSource _ds;

    public void setUp() throws Exception {
        super.setUp();
        _ds = new org.hsqldb.jdbc.jdbcDataSource();
        _ds.setDatabase("jdbc:hsqldb:mem:" + new GUID().toString());
        _ds.setUser("sa");

        _jdbcext = new JdbcExternalVariableModule();
        _jdbcext.registerDataSource("testds", _ds);
        _server.registerExternalVariableEngine(_jdbcext);

        Connection conn = _ds.getConnection();
        Statement s = conn.createStatement();
        s.execute("create table extvartable1 (" + "id1 VARCHAR PRIMARY KEY," + "_id2_ VARCHAR," + "pid VARCHAR, " + "iid INT,"
                + "cts DATETIME," + "uts DATETIME," + "foo VARCHAR," + "bar VARCHAR );");

        s.execute("insert into extvartable1(id1,pid,foo) values ('123','"
                + new QName("http://ode/bpel/unit-test","HelloWorld2-1").toString()
                + "','thefoo');");

        s.execute("CREATE TABLE costPerCustomer (value0 varchar(250), key1 varchar(250) primary key)");
        
        conn.close();
    }

    @Test
    public void testHelloWorld2() throws Throwable {
        go("/bpel/2.0/ExtVar");
    }

    @Test
    public void testExtVar2() throws Throwable {
        go("/bpel/2.0/ExtVar2");
    }

}
