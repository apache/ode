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
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.ProcessStore;
import org.apache.ode.bpel.iapi.ProcessStoreEvent;
import org.apache.ode.bpel.iapi.ProcessStoreListener;
import org.apache.ode.bpel.memdao.BpelDAOConnectionFactoryImpl;
import org.apache.ode.dao.jpa.BPELDAOConnectionFactoryImpl;
import org.apache.ode.store.ProcessStoreImpl;
import org.apache.ode.test.scheduler.TestScheduler;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.w3c.dom.Element;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public abstract class BPELTestAbstract extends TestCase {

    protected BpelServerImpl server;
    protected ProcessStore store;
    protected MessageExchangeContextImpl mexContext;
    protected EntityManager em;
    protected EntityManagerFactory emf;
    protected TestScheduler scheduler;
    protected BpelDAOConnectionFactory _cf;

    protected ArrayList<Failure> failures;

    @Override
    protected void setUp() throws Exception {
        failures = new ArrayList<Failure>();
        server = new BpelServerImpl();
        mexContext = new MessageExchangeContextImpl();

        if ( Boolean.getBoolean("org.apache.ode.test.persistent")) {
            emf = Persistence.createEntityManagerFactory("ode-unit-test-embedded");
            em = emf.createEntityManager();
            String pr = Persistence.PERSISTENCE_PROVIDER;
            _cf = new BPELDAOConnectionFactoryImpl();
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
        scheduler.setJobProcessor(server);
        store = new ProcessStoreImpl(null, "jpa", true);
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
        failures = null;
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
            testResponsePattern("init", bpelE.getMessage(), responsePattern);
            return;
        } catch ( Exception e ) {
            e.printStackTrace();
            fail();
        }
        scheduler.commit();
        
        ArrayList<Thread> testThreads = new ArrayList<Thread>();

        while (testPropsFile.exists()) {

            final Properties testProps = new Properties();
            testProps.load(testPropsFile.toURL().openStream());
            final QName serviceId = new QName(testProps.getProperty("namespace"),
                    testProps.getProperty("service"));
            final String operation = testProps.getProperty("operation");

            // Running tests in separate threads to allow concurrent invocation
            // (otherwise the first receive/reply invocation is going to block
            // everybody).
            Thread testRun = new Thread(new Runnable() {
                public void run() {
                    doInvoke(testProps, serviceId, operation);
                }
            });
            
            testThreads.add(testRun);
            testRun.start();

            Thread.sleep(200);
            propsFileCnt++;
            testPropsFile = new File(deployDir + "/test" + propsFileCnt
                    + ".properties");
        }

        // Waiting for all the test threads to finish.
        for (Thread testThread : testThreads) {
            testThread.join();
        }

        // Displaying result
        for (Failure failure : failures) {
            System.out.println("A test failure occured in message exchange request " + failure.requestName);
            System.out.println("=> Expected Response Pattern >> " + failure.expected);
            System.out.println("=> Actual Response >> " + failure.actual);            
        }
        assertTrue(failures.size() == 0);
    }

    private void testResponsePattern(String requestName, Message response, String responsePattern) {
        String resp = (response == null) ? "null" : DOMUtils
                .domToString(response.getMessage());
        testResponsePattern(requestName, resp, responsePattern);
    }

    private void testResponsePattern(String requestName, String resp, String responsePattern) {
        boolean testValue = Pattern.compile(responsePattern, Pattern.DOTALL)
                .matcher(resp).matches();
        if (!testValue) {
            failures.add(new Failure(requestName, resp, responsePattern));
        }
    }

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
    private void doInvoke(Properties testProps, QName serviceId, String operation) {
        for (int i = 1; testProps.getProperty("request" + i) != null; i++) {
            MyRoleMessageExchange mex = null;
            Future running = null;
            String responsePattern = null;
            try {
                scheduler.begin();

                mex = server.getEngine().createMessageExchange(new GUID().toString(), serviceId, operation);

                String in = testProps.getProperty("request" + i);
                responsePattern = testProps.getProperty("response" + i);

                mexContext.clearCurrentResponse();

                Message request = mex.createMessage(null);

                Element elem = DOMUtils.stringToDOM(in);
                request.setMessage(elem);


                running = mex.invoke(request);
                scheduler.commit();
            } catch ( Throwable e ) {
                e.printStackTrace();
                scheduler.rollback();
                fail();
            }

            if (!responsePattern.equals("ASYNC")) {
                try {
                    running.get(200000, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    System.out.println("TIMEOUT!");
                    fail();
                }

                switch (mex.getStatus()) {
                    case RESPONSE:
                        testResponsePattern("request" + i, mex.getResponse(), responsePattern);
                        // TODO: test for response fault
                        break;
                    case ASYNC:

                        switch (mex.getMessageExchangePattern()) {
                            case REQUEST_ONLY:
                                if (!responsePattern.equals("ASYNC"))
                                    fail();
                                break;
                            case REQUEST_RESPONSE:
                                testResponsePattern("request" + i, mexContext.getCurrentResponse(),
                                        responsePattern);
                            default:
                                break;
                        }

                        break;
                    case COMPLETED_OK:
                        if (!responsePattern.equals("COMPLETED_OK"))
                            testResponsePattern("request" + i, mexContext.getCurrentResponse(),
                                    responsePattern);
                        break;
                    case FAULT:
                        // TODO: handle Fault
                        System.out.println("=> " + mex.getFault() + " " + mex.getFaultExplanation());
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
        }
    }

    protected static class Failure {
        String requestName;
        String expected;
        String actual;

        public Failure(String requestName, String actual, String expected) {
            this.actual = actual;
            this.expected = expected;
            this.requestName = requestName;
        }
    }

}
