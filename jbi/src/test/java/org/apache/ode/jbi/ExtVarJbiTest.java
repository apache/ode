/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ode.jbi;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

public class ExtVarJbiTest extends JbiTestBase {
	private void initDb() throws Exception {
		TransactionManager tm = (TransactionManager) getBean("transactionManager");
		tm.begin();
        Connection conn = ((DataSource) getBean("localDerbyDataSource")).getConnection();

        dropTable(conn, "extvartable1");
        Statement s = conn.createStatement();
        s.execute("create table extvartable1 (" + "id1 VARCHAR(200) PRIMARY KEY," +  " \"_id2_\" VARCHAR(200)," +  "pid2 VARCHAR(250), " + "iid INT,"
                + "cts TIMESTAMP," + "uts TIMESTAMP," + "foo VARCHAR(250)," + "bar VARCHAR(250))");

        s.execute("insert into extvartable1(id1,pid2,foo) values ('123','"
                + new QName("http://ode/bpel/unit-test","HelloWorld2-1").toString()
                + "','thefoo')");
        s.close();
        tm.commit();
	}
	
    private static void dropTable(Connection c, String name) {
        try {
        	Statement s = c.createStatement();
            s.execute("drop table "+name);
            s.close();
        } catch (SQLException e) {
            // ignore
        }
    }
	
    public void testExtVar() throws Exception {
    	initDb();
        go();
    }
}
