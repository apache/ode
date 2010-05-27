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
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;
import org.apache.ode.bpel.extvar.jdbc.JdbcExternalVariableModule;
import org.junit.Test;

/**
 * Simple test of external variables.
 */
public class ExternalVariableTest extends BPELTestAbstract {

    private JdbcExternalVariableModule _jdbcext;

    private DataSource _ds;

    public void setUp() throws Exception {
        super.setUp();

        EmbeddedConnectionPoolDataSource ds = new EmbeddedConnectionPoolDataSource();
        ds.setCreateDatabase("create");
        ds.setDatabaseName("target/ExternalVariableTest");
        _ds = ds;

        _jdbcext = new JdbcExternalVariableModule();
        _jdbcext.registerDataSource("testds", _ds);
        _server.registerExternalVariableEngine(_jdbcext);

        Connection conn = _ds.getConnection();
        Statement s = conn.createStatement();

        dropTable(s, "extvartable1");
        s.execute("create table extvartable1 (" + "id1 VARCHAR(200) PRIMARY KEY," +  " \"_id2_\" VARCHAR(200)," +  "pid VARCHAR(250), " + "iid INT,"
                + "cts TIMESTAMP," + "uts TIMESTAMP," + "foo VARCHAR(250)," + "bar VARCHAR(250))");

        s.execute("insert into extvartable1(id1,pid,foo) values ('123','"
                + new QName("http://ode/bpel/unit-test","HelloWorld2-1").toString()
                + "','thefoo')");

        dropTable(s, "costPerCustomer");
        s.execute("CREATE TABLE costPerCustomer (value0 varchar(250), key1 varchar(250) primary key)");

        dropTable(s, "DataTypesTest");
        s.execute("CREATE TABLE DataTypesTest (KEYSTRING VARCHAR(255), STRINGCOL VARCHAR(255), FLOATCOL FLOAT, "
                + "INTCOL INTEGER, NUMBERCOL NUMERIC, TIMESTAMPCOL TIMESTAMP, BOOLEANCOL SMALLINT)");

        dropTable(s, "GenKey");
        s.execute("CREATE TABLE GenKey (KEYSTRING INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY, STRINGCOL VARCHAR(255))");

        conn.close();
    }

    private static void dropTable(Statement s, String name) {
        try {
            s.execute("drop table "+name);
        } catch (SQLException e) {
            // ignore
        }
    }

    @Test
    public void testHelloWorld2() throws Throwable {
        go("/bpel/2.0/ExtVar");
    }

    @Test
    public void testExtVar2() throws Throwable {
        go("/bpel/2.0/ExtVar2");
    }

    @Test
    public void testExtVar3() throws Throwable {
        go("/bpel/2.0/ExtVar3");
    }

    @Test
    public void testExtVarKeyGen() throws Throwable {
        go("/bpel/2.0/ExtVar-GenKey");
    }
}
