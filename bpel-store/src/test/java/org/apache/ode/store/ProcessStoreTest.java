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
package org.apache.ode.store;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import javax.transaction.TransactionManager;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.dao.store.ConfStoreDAOConnectionFactory;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.il.dbutil.Database;
import org.apache.ode.il.txutil.TxManager;
import org.apache.ode.utils.DOMUtils;

public class ProcessStoreTest extends TestCase {

  ProcessStoreImpl _ps;
  Database _db;
  ConfStoreDAOConnectionFactory _cf;
  private File _testdd;

  public void setUp() throws Exception {
     Properties props = new Properties();
     props.setProperty(OdeConfigProperties.PROP_DAOCF_STORE,System.getProperty(OdeConfigProperties.PROP_DAOCF_STORE,OdeConfigProperties.DEFAULT_DAOCF_STORE_CLASS));
     OdeConfigProperties odeProps = new OdeConfigProperties(props, "");
     _db = new Database(odeProps);
     TxManager tx = new TxManager(odeProps);
     TransactionManager txm = tx.createTransactionManager();
     _db.setTransactionManager(txm);
     _db.start();
     _cf = _db.createDaoStoreCF();
     _ps = new ProcessStoreImpl(null,txm,  _cf);
     _ps.loadAll();
     URI tdd = getClass().getResource("/testdd/deploy.xml").toURI();
     _testdd = new File(tdd.getPath()).getParentFile();
  }

  public void tearDown() throws Exception {
    _ps.shutdown();
    _cf.shutdown();
    _db.shutdown();
  }

  public void testSanity() {
    assertEquals(0, _ps.getProcesses().size());
    assertEquals(0, _ps.getPackages().size());
    assertNull(_ps.listProcesses("foobar"));
  }

  public void testDeploy() {
    Collection<QName> deployed = _ps.deploy(_testdd);
    assertNotNull(deployed);
    assertEquals(1, deployed.size());
  }

  public void testGetProcess() {
    Collection<QName> deployed = _ps.deploy(_testdd);
    QName pname = deployed.iterator().next();
    assertNotNull(deployed);
    assertEquals(1, deployed.size());
    ProcessConf pconf = _ps.getProcessConfiguration(pname);
    assertNotNull(pconf);
    assertEquals(_testdd.getName(), pconf.getPackage());
    assertEquals(pname, pconf.getProcessId());
    assertEquals(1, pconf.getPropagationRules().size());
    assertEquals(1, pconf.getContextInterceptors().keySet().size());
    assertNotNull(pconf.getContextInterceptors().get("org.apache.ode.bpel.context.TestInterceptor"));
    assertEquals("myparam1", DOMUtils.getElementContent(pconf.getContextInterceptors().get("org.apache.ode.bpel.context.TestInterceptor")).getLocalName());
  }

  public void testGetProcesses() {
    Collection<QName> deployed = _ps.deploy(_testdd);
    QName pname = deployed.iterator().next();
    assertNotNull(deployed);
    assertEquals(1, deployed.size());
    List<QName> pconfs = _ps.getProcesses();
    assertEquals(pname, pconfs.get(0));
  }
}
