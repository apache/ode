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

import java.io.InputStream;
import java.sql.Connection;

import java.util.Properties;
import javax.transaction.TransactionManager;
import junit.framework.TestCase;
import org.apache.ode.dao.scheduler.SchedulerDAOConnectionFactory;
import org.apache.ode.il.EmbeddedGeronimoFactory;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.il.dbutil.Database;
import org.apache.ode.il.txutil.TxManager;
import org.apache.ode.scheduler.simple.jdbc.SchedulerDAOConnectionFactoryImpl;

/**
 * Support class for creating a JDBC delegate (using in-mem HSQL db).
 * 
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 */
public class SchedulerTestBase extends TestCase {

  protected Database _db;
  protected SchedulerDAOConnectionFactory _factory;
  protected TransactionManager _txm;

  @Override
  public void setUp() throws Exception {
    Properties props = new Properties();
    props.put(OdeConfigProperties.PROP_DAOCF_SCHEDULER, System.getProperty(OdeConfigProperties.PROP_DAOCF_SCHEDULER,OdeConfigProperties.DEFAULT_DAOCF_SCHEDULER_CLASS));
    OdeConfigProperties odeProps = new OdeConfigProperties(props, "");
    TxManager tx = new TxManager(odeProps);
    _txm = tx.createTransactionManager();
    _db = new Database(odeProps);
    _db.setTransactionManager(_txm);
    _db.start();
    _factory = _db.createDaoSchedulerCF();

    if (_factory instanceof SchedulerDAOConnectionFactoryImpl) {
      Connection c = _db.getDataSource().getConnection();
      try {
        StringBuffer sql = new StringBuffer();

        {
          InputStream in = getClass().getResourceAsStream("/simplesched-h2.sql");
          int v;
          while ((v = in.read()) != -1) {
            sql.append((char) v);
          }
        }

        String[] cmds = sql.toString().split(";");
        for (String cmd : cmds) {
          c.createStatement().executeUpdate(cmd);
        }
      } finally {
        c.close();
      }
    }

  }

  @Override
  public void tearDown() throws Exception {
    _factory.shutdown();
    _db.shutdown();

  }

  public static long mod(long a, long b) {
    return a % b;
  }
}

