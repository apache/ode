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
import org.apache.ode.bpel.dao.ActivityRecoveryDAO;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.FaultDAO;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.ScopeStateEnum;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.apache.ode.dao.jpa.BPELDAOConnectionFactoryImpl;
import org.apache.ode.il.EmbeddedGeronimoFactory;
import org.apache.ode.utils.GUID;
import org.hsqldb.jdbc.jdbcDataSource;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Properties;
import java.util.List;

public class SelectObjectTest extends TestCase {
	
	private EntityManager em;
	private static final String TEST_NS = "http://org.apache.ode.jpa.test";
	private String[] correlationKeys = { "key1", "key2" };
	private String[] actions = { "action1","action2" };
	private CorrelationKey key1 = new CorrelationKey("key1",correlationKeys);
	private static final String CORRELATOR_ID1 = "testCorrelator1";
	private static final String CORRELATOR_ID2 = "testCorrelator2";

    TransactionManager _txm;
    DataSource _ds;
    BPELDAOConnectionFactoryImpl factory;

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
	
	public void testGetObject() throws Exception {
        new InsertObjectTest().createStuff(factory);

        _txm.commit();
        _txm.begin();        

        BpelDAOConnection conn = factory.getConnection();
		
		// Assert the ProcessDAO
		ProcessDAO p = conn.getProcess(new QName(TEST_NS,"testPID1"));
		assertNotNull( p );
		Collection<ProcessInstanceDAO> insts = p.findInstance(key1);
		assertNotNull( insts );
		assertTrue( insts.size() > 0 );
		assertNotNull(p.getType());
		assertNotNull(p.getProcessId());
		assertEquals( p.getVersion() , 1 );
		
		// Assert the CorrelatorDAO
		CorrelatorDAO corr = p.getCorrelator(CORRELATOR_ID1);
		assertNotNull( corr );
		assertEquals(corr.getCorrelatorId(),CORRELATOR_ID1);
		
		// Assert the MessageRouteDAO
		List<MessageRouteDAO> routes = corr.findRoute(new CorrelationKeySet().add(key1));
        MessageRouteDAO route = null;
        if (routes != null && routes.size() > 0) {
            route = routes.get(0);
        }
		assertNotNull( route );
		assertEquals(route.getGroupId(),"testRoute" );
		assertEquals(route.getIndex() , 1 );
		assertNotNull(route.getTargetInstance() );

		// Assert the ProcessInstanceDAO
		for ( ProcessInstanceDAO inst : insts ) {
			Long id = inst.getInstanceId();
			assertNotNull( id );
			
			ProcessInstanceDAO inst2 = conn.getInstance(id);
			assertSame(inst2,inst);
			
			ProcessInstanceDAO inst3 = p.getInstance(id);
			assertSame( inst3 , inst );
			
			Long mon = inst.genMonotonic();
			assertEquals(inst.getActivityFailureCount() , 2);
			assertNotNull(inst.getActivityFailureDateTime() );
			assertNotNull(inst.getCreateTime() );
			assertTrue(inst.getExecutionState().length > 0 );
			assertNotNull(inst.getLastActiveTime() );
			assertSame(inst.getProcess() , p );
			assertEquals(inst.getPreviousState() , 0);
			assertEquals(inst.getState() , 1);
			
			// Assert the Root ScopeDAO
			ScopeDAO rs = inst.getRootScope();
			assertNotNull( rs );
			assertNotNull(rs.getChildScopes());
			ScopeDAO child1 = null;
			for ( ScopeDAO childItr : rs.getChildScopes()){
				child1 = childItr;
				break;
			}
			assertNotNull(child1);
			assertNotNull(rs.getCorrelationSets());
			assertEquals(rs.getCorrelationSets().size() , 0 );
			assertEquals(rs.getModelId(),1);
			assertEquals(rs.getName(),"Root");
			assertTrue(rs.getParentScope() == null);
			assertNotNull(rs.getPartnerLinks());
			assertEquals(rs.getPartnerLinks().size() ,0);
			assertSame(rs.getProcessInstance(),inst);
			assertNotNull(rs.getScopeInstanceId());
			assertEquals(rs.getState(),ScopeStateEnum.ACTIVE);
			assertNotNull(rs.getVariables());
			assertEquals(rs.getVariables().size(),0);
			
		
			// Assert the ActivityRecoveryDAO
			assertNotNull(inst.getActivityRecoveries());
			ActivityRecoveryDAO rec1 = null;
			for (ActivityRecoveryDAO recItr : inst.getActivityRecoveries()) {
       if (recItr.getActivityId() == 3) {
         rec1 = recItr;break;
       }
      }
			assertNotNull(rec1);
			String tmpAct = rec1.getActions();
//			assertEquals(rec1.getActionsList(),actions);
			assertEquals(rec1.getActivityId(),3);
			assertEquals(rec1.getChannel(),"testChannel1");
			assertNotNull(rec1.getDateTime());
//			assertNotNull(rec1.getDetails());
			assertEquals(rec1.getReason(),"testReason1");
			assertEquals(rec1.getRetries(),2);
			
			// Assert the CorrelationSetDAO
			//assertNotNull(inst.getCorrelationSets());
			//CorrelationSetDAO cs1 = null;
			//for ( CorrelationSetDAO csItr : inst.getCorrelationSets() ) {
			//	cs1 = csItr;
			//	break;
			//}
			//assertNotNull(cs1);
			
			// Assert the FaultDAO
			FaultDAO fault = inst.getFault();
			assertNotNull(fault);
			assertEquals(fault.getActivityId(),1);
			assertNotNull(fault.getData());
			assertEquals(fault.getExplanation(),"testExplanation");
			assertEquals(fault.getLineNo(),1);
			assertEquals(fault.getName(),new QName(TEST_NS,"testFault"));
			
			// Assert MessageExchangeDAO
			CorrelatorDAO ic = inst.getInstantiatingCorrelator();
			assertNotNull(ic);
			assertEquals(ic.getCorrelatorId(),CORRELATOR_ID1);
			// The message is dequeued but not persisted
			MessageExchangeDAO me = ic.dequeueMessage(new CorrelationKeySet().add(key1));
			assertNotNull(me);
			assertEquals(me.getCallee(),new QName(TEST_NS,"testCallee"));
			assertEquals(me.getPropagateTransactionFlag(),false);
			assertEquals(me.getChannel(),"testChannel");
			assertEquals(me.getCorrelationId(),"testCorrelationId");
			//assertNotNull(me.getCreateTime());
			assertEquals(me.getDirection(),'0');
			assertNotNull(me.getEPR());
			assertEquals(me.getFault().toString(),"testFault");
			assertEquals(me.getFaultExplanation(),"testFaultExplanation");
			assertSame(me.getInstance(),inst);
			assertEquals(me.getOperation(),"testOperation");
			assertNotNull(me.getPartnerLink());
			assertEquals(me.getPartnerLinkModelId(),1);
			assertEquals(me.getPattern(),"testPattern");
			assertEquals(me.getPortType(),new QName(TEST_NS,"testPortType"));
			assertSame(me.getProcess(),p);
			assertEquals(me.getProperty("testProp1Key"),"testProp1");
			assertNotNull(me.getRequest());
			assertNotNull(me.getResponse());
			assertEquals(me.getStatus(),"testStatus");
			
			// Assert MessageDAO
			MessageDAO m = me.getRequest();
			assertNotNull(m.getData());
			assertSame(m.getMessageExchange(),me);
			assertEquals(m.getType(),new QName(TEST_NS,"testRequest"));
			
			
			//Assert Child ScopeDAO
			assertNotNull(inst.getScopes());
			assertTrue(inst.getScopes().size() > 0);
			assertNotNull(inst.getScopes("Child1"));
			assertTrue(inst.getScopes("Child1").size() == 1);
			ScopeDAO childS = inst.getScopes("Child1").iterator().next();
			assertSame(childS,child1);
			assertSame(childS.getParentScope(),rs);
			assertNotNull(childS.getChildScopes());
			assertEquals(childS.getChildScopes().size(), 0);
			assertNotNull(childS.getVariables());
			assertTrue(childS.getVariables().size() > 0);
			assertNotNull(childS.getVariable("var1"));
			XmlDataDAO chsVar = childS.getVariable("var1");
			assertNotNull(childS.getPartnerLinks());
			assertTrue(childS.getPartnerLinks().size() > 0);
			PartnerLinkDAO spl = childS.getPartnerLinks().iterator().next();
			assertSame(spl,me.getPartnerLink());
			assertSame(spl,childS.getPartnerLink(spl.getPartnerLinkModelId()));
			assertNotNull(childS.getCorrelationSets());
			assertTrue(childS.getCorrelationSets().size() > 0);
			assertNotNull(childS.getCorrelationSet("TestCorrelationSet1"));
			
			// Assert CorrelationSetDAO
			CorrelationSetDAO cs = childS.getCorrelationSet("TestCorrelationSet1");
			assertEquals(cs.getName(),"TestCorrelationSet1");
			assertNotNull(cs.getProperties());
			assertTrue(cs.getProperties().size() > 0);
			assertSame(cs.getScope(),childS);
			assertNotNull(cs.getValue());
			assertEquals(cs.getProperties().get(new QName(TEST_NS,"name1")),"key1");
			
			
			// Assert PartnerLinkDAO
			assertNotNull(spl.getMyEPR());
			assertEquals(spl.getMyRoleName(),"MyRole1");
			assertEquals(spl.getMyRoleServiceName(),new QName(TEST_NS,"testRoleService"));
			assertEquals(spl.getMySessionId(),"TestMySessionID");
			assertNotNull(spl.getPartnerEPR());
			assertEquals(spl.getPartnerLinkModelId(),1);
			assertEquals(spl.getPartnerLinkName(),"Test PartnerLink1");
			assertEquals(spl.getPartnerRoleName(),"PartnerRole1");
			assertEquals(spl.getPartnerSessionId(),"TestPartnerSessionID");
			
			// Assert Variables
			assertNotNull(inst.getVariables("var1", 2));
			assertEquals(inst.getVariables("var1", 2).length,1);
			XmlDataDAO[] vars = inst.getVariables("var1", 2);
			assertSame(chsVar,vars[0]);
			assertNotNull(vars[0].get());
			assertEquals(vars[0].getName(),"var1");
			// assertEquals(vars[0].getProperty("key1"),"prop1");
			assertSame(vars[0].getScopeDAO(),childS);
			
		}
	}

	@Override
	protected void tearDown() throws Exception {
        _txm.commit();
		_txm = null;
        _ds = null;
    }
	
}
