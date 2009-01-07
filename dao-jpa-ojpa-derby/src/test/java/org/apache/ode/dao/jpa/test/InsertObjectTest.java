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

package org.apache.ode.dao.jpa.test;

import junit.framework.TestCase;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.CorrelationKeySet;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.ScopeStateEnum;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.apache.ode.dao.jpa.BPELDAOConnectionFactoryImpl;
import org.apache.ode.il.EmbeddedGeronimoFactory;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.hsqldb.jdbc.jdbcDataSource;
import org.xml.sax.SAXException;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;

public class InsertObjectTest extends TestCase {
	
	private static final String TEST_NS = "http://org.apache.ode.jpa.test";
	private static final String CORRELATOR_ID1 = "testCorrelator1";
	private static final String CORRELATOR_ID2 = "testCorrelator2";
	private static final Calendar cal = new GregorianCalendar();

    TransactionManager _txm;
    DataSource _ds;
    BPELDAOConnectionFactoryImpl factory;
    ProcessDAO _process;

    @Override
	protected void setUp() throws Exception {
        jdbcDataSource hsqlds = new jdbcDataSource();
        hsqlds.setDatabase("jdbc:hsqldb:mem:" + new GUID().toString());
        hsqlds.setUser("sa");
        hsqlds.setPassword("");
        _ds = hsqlds;

        _txm = new EmbeddedGeronimoFactory().getTransactionManager();

        factory = new BPELDAOConnectionFactoryImpl();
        factory.setDataSource(_ds);
        factory.setTransactionManager(_txm);
        Properties props = new Properties();
        props.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=false)");
        factory.init(props);

        _txm.begin();
    }
	
	public void testStart() throws Exception {
        createStuff(factory);
    }

    void createStuff(BPELDAOConnectionFactoryImpl factory) throws Exception {
        BpelDAOConnection conn = factory.getConnection();

        CorrelatorDAO corr = createProcess(conn,"testPID1","testType");
		ProcessInstanceDAO pi1 = createProcessInstance(_process, corr);
    }

    @Override
	protected void tearDown() throws Exception {
        _txm.commit();
		_ds = null;
        _txm = null;
    }
	
	private MessageExchangeDAO createMessageExchange(ProcessDAO p, ProcessInstanceDAO pi, PartnerLinkDAO pl ) throws SAXException, IOException {
		MessageExchangeDAO me = pi.getConnection().createMessageExchange('0');
		
		me.setCallee(new QName(TEST_NS,"testCallee"));
		me.setChannel("testChannel");
		me.setCorrelationId("testCorrelationId");
		me.setCorrelationStatus("testCorrelationStatus");
		me.setEPR(DOMUtils.stringToDOM("<testEPR>EPR</testEPR>"));
		me.setFault(new QName("testFault"));
		me.setFaultExplanation("testFaultExplanation");
		me.setInstance(pi);
		me.setOperation("testOperation");
		me.setPartnerLink(pl);
		me.setPartnerLinkModelId(1);
		me.setPattern("testPattern");
		me.setPortType(new QName(TEST_NS,"testPortType"));
		me.setProcess(p);
		me.setProperty("testProp1Key", "testProp1");
		me.setProperty("testProp2Key", "testProp2");
		me.setRequest(createMessage(me,"testRequest"));
		me.setResponse(createMessage(me,"testResponse"));
		me.setStatus("testStatus");
		
		return me;
	}
	
	private MessageDAO createMessage(MessageExchangeDAO me, String name) throws SAXException, IOException {
		MessageDAO m = me.createMessage(new QName(TEST_NS,name));
		
		m.setType(new QName(TEST_NS,name));
		m.setData(DOMUtils.stringToDOM("<testData>some test data</testData>"));
		
		return m;
	}
	
	private CorrelatorDAO createProcess(BpelDAOConnection conn, String pid, String type) {
		_process = conn.createProcess(new QName(TEST_NS,pid), new QName(TEST_NS,type),"GUID1",1);
		CorrelatorDAO corr = _process.addCorrelator(CORRELATOR_ID1);
		_process.addCorrelator(CORRELATOR_ID2);
		return corr;
	}
	
	private ProcessInstanceDAO createProcessInstance(ProcessDAO process, CorrelatorDAO corr) throws SAXException, IOException {
		ProcessInstanceDAO pi = null;
		String[] actions = { "action1","action2" };
		String[] correlationKeys = { "key1", "key2" };
		CorrelationKey key1 = new CorrelationKey("key1",correlationKeys);
		CorrelationKey key2 = new CorrelationKey("key2",correlationKeys);
		CorrelationKey[] corrkeys = {key1,key2}; 
		QName[] names = { new QName(TEST_NS,"name1"), new QName(TEST_NS,"name2") };

        pi = process.createInstance(corr);
		
		pi.setExecutionState(new String("test execution state").getBytes());
		pi.setFault(new QName(TEST_NS,"testFault"), "testExplanation", 1, 1, DOMUtils.stringToDOM("<testFaultMessage>testMessage</testFaultMessage>"));
		pi.setLastActiveTime(cal.getTime());
		pi.setState((short) 1);
		
		pi.createActivityRecovery("testChannel1", 3, "testReason1", cal.getTime(), DOMUtils.stringToDOM("<testData>testData1</testData>"), actions, 2);
		pi.createActivityRecovery("testChannel2", 4, "testReason2", cal.getTime(), DOMUtils.stringToDOM("<testData>testData2</testData>"), actions, 2);
		
		ScopeDAO root = pi.createScope(null, "Root", 1);
		root.setState(ScopeStateEnum.ACTIVE);
		ScopeDAO child1 = pi.createScope(root, "Child1", 2);
		child1.setState(ScopeStateEnum.ACTIVE);
		XmlDataDAO var1 = child1.getVariable("var1");
		var1.set(DOMUtils.stringToDOM("<testData>testData</testData>"));
		var1.setProperty("key1", "prop1");
		var1.setProperty("key2", "prop2");
		XmlDataDAO var2 = child1.getVariable("var2");
		var2.set(DOMUtils.stringToDOM("<testData>testData</testData>"));
		var2.setProperty("key1", "prop1");
		var2.setProperty("key2", "prop2");
		
		CorrelationSetDAO cs1 = child1.getCorrelationSet("TestCorrelationSet1");
		cs1.setValue(names,key1);
		
		PartnerLinkDAO pl1 = child1.createPartnerLink(1, "Test PartnerLink1", "MyRole1", "PartnerRole1");
		pl1.setMyEPR(DOMUtils.stringToDOM("<testEPR>testMyEPR</testEPR>"));
		pl1.setMyRoleServiceName(new QName(TEST_NS,"testRoleService"));
		pl1.setMySessionId("TestMySessionID");
		pl1.setPartnerEPR(DOMUtils.stringToDOM("<testEPR>testPartnerEPR</testEPR>"));
		pl1.setPartnerSessionId("TestPartnerSessionID");
		
		MessageExchangeDAO mex = createMessageExchange(process,pi,pl1);
		
		corr.addRoute("testRoute", pi, 1, new CorrelationKeySet().add(key1), "one");
		corr.enqueueMessage(mex, new CorrelationKeySet().add(corrkeys[0]).add(corrkeys[1]));
		
		return pi;
	}

}
