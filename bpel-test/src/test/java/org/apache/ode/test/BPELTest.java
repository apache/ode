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

import junit.framework.TestCase;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.bpel.memdao.BpelDAOConnectionFactoryImpl;
import org.apache.ode.store.ProcessStoreImpl;
import org.apache.ode.test.scheduler.TestScheduler;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.Collection;
import java.util.Properties;
import java.util.regex.Pattern;

public abstract class BPELTest extends TestCase {

	protected BpelServerImpl server;
    protected ProcessStore store;
	protected MessageExchangeContextImpl mexContext;
	protected EntityManager em;
	protected EntityManagerFactory emf;
    protected TestScheduler scheduler;
    protected BpelDAOConnectionFactory _cf;

    @Override
	protected void setUp() throws Exception {
		server = new BpelServerImpl();
		mexContext = new MessageExchangeContextImpl();
		
		
		if ( Boolean.getBoolean("org.apache.ode.test.persitent")) {
			emf = Persistence.createEntityManagerFactory("ode-unit-test-embedded");
			em = emf.createEntityManager();
			String pr = Persistence.PERSISTENCE_PROVIDER;
            _cf = new org.apache.ode.dao.jpa.ojpa.BPELDAOConnectionFactoryImpl();
            server.setDaoConnectionFactory(_cf);
            scheduler = new TestScheduler() {
                @Override
                public void begin() {
                    super.begin();
                    em.getTransaction().begin();
                }

                @Override
                public void commit() {
                    super.commit();
                    em.getTransaction().commit();
                }
                @Override
                public void rollback() {
                    super.rollback();
                    em.getTransaction().rollback();
                }
                
            };
		} else {
            _cf = new BpelDAOConnectionFactoryImpl();
            server.setDaoConnectionFactory(_cf);
            scheduler = new TestScheduler();
		}
        server.setInMemDaoConnectionFactory(new BpelDAOConnectionFactoryImpl());
        server.setScheduler(scheduler);
		server.setBindingContext(new BindingContextImpl());
		server.setMessageExchangeContext(mexContext);
        store = new ProcessStoreImpl(null, true);
        store.registerListener(new ProcessStoreListener() {
            public void onProcessStoreEvent(ProcessStoreEvent event) {
                // bounce the process
                server.unregister(event.pid);
                if (event.type != ProcessStoreEvent.Type.UNDEPLOYED)
                    server.register(store.getProcessConfiguration(event.pid));
            }
        });
        server.init();
		server.start();
	}

	@Override
	protected void tearDown() throws Exception {
		if ( em != null ) em.close();
		if ( emf != null ) emf.close();
		server.stop();
	}

	protected void negative(String deployDir) throws Throwable {
		try {
			go(deployDir);
		} catch (junit.framework.AssertionFailedError ex) {
			return;
		}
		fail("Expecting test to fail");
	}

	protected void go(String deployDir) throws Throwable {

		/**
		 * The deploy directory must contain at least one "test.properties"
		 * file.
		 *
		 * The test.properties file identifies the service, operation and
		 * messages to be sent to the BPEL engine.
		 *
		 * The deploy directory may contain more than one file in the form of
		 * "testN.properties" where N represents a monotonic integer beginning
		 * with 1.
		 *
		 */

		int propsFileCnt = 0;
		File testPropsFile = new File(deployDir + "/test.properties");

		if (!testPropsFile.exists()) {
			propsFileCnt++;
			testPropsFile = new File(deployDir + "/test" + propsFileCnt
					+ ".properties");
			if (!testPropsFile.exists()) {
				System.err.println("can't find " + testPropsFile.toString());
			}
		}

		scheduler.begin();
		try {
			Collection<QName> procs =  store.deploy(new File(deployDir));
            for (QName procName : procs) {
                server.register(store.getProcessConfiguration(procName));
            }

        } catch (ContextException bpelE) {
			Properties testProps = new Properties();
			testProps.load(testPropsFile.toURL().openStream());
			String responsePattern = testProps.getProperty("response1");
            bpelE.printStackTrace();
			testResponsePattern(bpelE.getMessage(), responsePattern);
            return;
		} catch ( Exception e ) {
			e.printStackTrace();
			fail();
		}
		scheduler.commit();
        
		while (testPropsFile.exists()) {

			scheduler.begin();
			
			Properties testProps = new Properties();
			testProps.load(testPropsFile.toURL().openStream());

			QName serviceId = new QName(testProps.getProperty("namespace"),
					testProps.getProperty("service"));
			String operation = testProps.getProperty("operation");

			MyRoleMessageExchange mex = server.getEngine()
					.createMessageExchange("", serviceId, operation);

			/**
			 * Each property file must contain at least one request/response
			 * property tuple.
			 *
			 * The request/response tuple should be in the form
			 *
			 * requestN=<message>some XML input message</message>
			 * responseN=.*some response message.*
			 *
			 * Where N is a monotonic integer beginning with 1.
			 *
			 * If a specific MEP is expected in lieu of a response message use:
			 * responseN=ASYNC responseN=ONE_WAY responseN=COMPLETED_OK
			 *
			 */
			try {
				for (int i = 1; testProps.getProperty("request" + i) != null; i++) {
	
					String in = testProps.getProperty("request" + i);
					String responsePattern = testProps.getProperty("response" + i);
	
					mexContext.clearCurrentResponse();
	
					Message request = mex.createMessage(null);
	
					Element elem = DOMUtils.stringToDOM(in);
					request.setMessage(elem);
	
	
					mex.invoke(request);
	
	
					switch (mex.getStatus()) {
					case RESPONSE:
						testResponsePattern(mex.getResponse(), responsePattern);
						// TODO: test for response fault
						break;
					case ASYNC:
	
						switch (mex.getMessageExchangePattern()) {
						case REQUEST_ONLY:
							if (!responsePattern.equals("ASYNC"))
								fail();
							break;
						case REQUEST_RESPONSE:
							testResponsePattern(mexContext.getCurrentResponse(),
									responsePattern);
						default:
							break;
						}
	
						break;
					case COMPLETED_OK:
						if (!responsePattern.equals("COMPLETED_OK"))
							testResponsePattern(mexContext.getCurrentResponse(),
									responsePattern);
						break;
					case FAULT:
						// TODO: handle Fault
						System.out.println("=> " + mex.getFaultExplanation());
						fail();
						break;
					case COMPLETED_FAILURE:
						// TODO: handle Failure
						System.out.println("=> " + mex.getFaultExplanation());
						fail();
						break;
					case COMPLETED_FAULT:
						// TODO: handle Failure
						System.out.println("=> " + mex.getFaultExplanation());
						fail();
						break;
					case FAILURE:
						// TODO: handle Faulure
						System.out.println("=> " + mex.getFaultExplanation());
						fail();
						break;
					default:
						fail();
						break;
					}
				}
			} catch ( Throwable e ) {
				e.printStackTrace();
				scheduler.rollback();
				throw e;
			}
			propsFileCnt++;
			testPropsFile = new File(deployDir + "/test" + propsFileCnt
					+ ".properties");
			scheduler.commit();
		}
	}

    private void testResponsePattern(Message response, String responsePattern) {
		String resp = (response == null) ? "null" : DOMUtils
				.domToString(response.getMessage());
		testResponsePattern(resp, responsePattern);
	}

	private void testResponsePattern(String resp, String responsePattern) {
		boolean testValue = Pattern.compile(responsePattern, Pattern.DOTALL)
				.matcher(resp).matches();

		if (!testValue) {
			System.out.println("=> Expected Response Pattern >> "
					+ responsePattern);
			System.out.println("=> Acutal Response >> " + resp);
		}
		assertTrue(testValue);
	}

}
