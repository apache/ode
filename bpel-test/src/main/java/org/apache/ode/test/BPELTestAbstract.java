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
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange.CorrelationStatus;
import org.apache.ode.bpel.iapi.ProcessStore;
import org.apache.ode.bpel.iapi.ProcessStoreEvent;
import org.apache.ode.bpel.iapi.ProcessStoreListener;
import org.apache.ode.bpel.memdao.BpelDAOConnectionFactoryImpl;
import org.apache.ode.dao.jpa.BPELDAOConnectionFactoryImpl;
import org.apache.ode.il.MockScheduler;
import org.apache.ode.store.ProcessStoreImpl;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.w3c.dom.Element;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BPELTestAbstract extends TestCase {

    protected BpelServerImpl _server;

    protected ProcessStore store;

    protected MessageExchangeContextImpl mexContext;

    protected EntityManager em;

    protected EntityManagerFactory emf;

    protected MockScheduler scheduler;

    protected BpelDAOConnectionFactory _cf;

    /** Failures that have been detected. */
    protected List<Failure> _failures;

    /** The things we'd like to deploy. */
    protected List<Deployment> _deployments;

    /** The things we'd like to invoke. */
    protected List<Invocation> _invocations;

    /** What's actually been deployed. */
    private List<Deployment> _deployed;

    @Override
    protected void setUp() throws Exception {
        _failures = new CopyOnWriteArrayList<Failure>();
        _server = new BpelServerImpl();
        mexContext = new MessageExchangeContextImpl();
        _deployments = new ArrayList<Deployment>();
        _invocations = new ArrayList<Invocation>();
        _deployed = new ArrayList<Deployment>();

        if (Boolean.getBoolean("org.apache.ode.test.persistent")) {
            emf = Persistence.createEntityManagerFactory("ode-unit-test-embedded");
            em = emf.createEntityManager();
            _cf = new BPELDAOConnectionFactoryImpl();
            _server.setDaoConnectionFactory(_cf);
            scheduler = new MockScheduler() {
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
            scheduler = new MockScheduler();
            _cf = new BpelDAOConnectionFactoryImpl(scheduler);
            _server.setDaoConnectionFactory(_cf);
        }
        _server.setInMemDaoConnectionFactory(new BpelDAOConnectionFactoryImpl(scheduler));
        _server.setScheduler(scheduler);
        _server.setBindingContext(new BindingContextImpl());
        _server.setMessageExchangeContext(mexContext);
        scheduler.setJobProcessor(_server);
        store = new ProcessStoreImpl(null, "jpa", true);
        store.registerListener(new ProcessStoreListener() {
            public void onProcessStoreEvent(ProcessStoreEvent event) {
                // bounce the process
                _server.unregister(event.pid);
                if (event.type != ProcessStoreEvent.Type.UNDEPLOYED)
                    _server.register(store.getProcessConfiguration(event.pid));
            }
        });
        _server.init();
        _server.start();
    }

    @Override
    protected void tearDown() throws Exception {
        for (Deployment d : _deployed) {
            try {
                store.undeploy(d.deployDir);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("Error undeploying " + d);
            }
        }
        

        if (em != null)
            em.close();
        if (emf != null)
            emf.close();
        
        _server.stop();
        _failures = null;
        _deployed = null;
        _deployments = null;
        _invocations = null;
        
    }

    protected void negative(String deployDir) throws Throwable {
        try {
            go(new File(deployDir));
        } catch (junit.framework.AssertionFailedError ex) {
            return;
        }
        fail("Expecting test to fail");
    }

    protected void go(String deployDir) throws Exception {
        go(makeDeployDir(deployDir));
    }

    protected Deployment addDeployment(String deployDir) {
        return addDeployment(makeDeployDir(deployDir));
    }

    protected Deployment addDeployment(File deployDir) {
        Deployment deployment = new Deployment(deployDir);
        _deployments.add(deployment);
        return deployment;
    }

    protected void go(File deployDir) throws Exception {
        setup(deployDir);
        go();
    }

    protected void setup(File deployDir) throws Exception {
        addDeployment(deployDir);
        int propsFileCnt = 0;
        File testPropsFile = new File(deployDir, "test.properties");
        if (!testPropsFile.exists()) {
            propsFileCnt++;
            testPropsFile = new File(deployDir, "test" + propsFileCnt + ".properties");
            if (!testPropsFile.exists()) {
                System.err.println("can't find " + testPropsFile);
            }
        }

        if (!testPropsFile.exists())
            fail("Test property file not found in " + deployDir);

        while (testPropsFile.exists()) {
            Properties testProps = new Properties();
            InputStream is = new FileInputStream(testPropsFile);
            try {
                testProps.load(is);
            } finally {
                is.close();
            }

            final QName serviceId = new QName(testProps.getProperty("namespace"), testProps.getProperty("service"));
            final String operation = testProps.getProperty("operation");

            for (int i = 1; testProps.getProperty("request" + i) != null; i++) {
                final String in = testProps.getProperty("request" + i);
                final String responsePattern = testProps.getProperty("response" + i);

                addInvoke(testPropsFile + "#" + i, serviceId, operation, in, responsePattern);
            }

            testPropsFile = new File(deployDir, "test" + propsFileCnt + ".properties");
        }
    }

    protected Invocation addInvoke(String id, QName target, String operation, String request, String responsePattern) throws Exception {

        Invocation inv = new Invocation(id);
        inv.target = target;
        inv.operation = operation;
        inv.request = DOMUtils.stringToDOM(request);
        inv.expectedStatus = null;
        if (responsePattern != null) {
            inv.expectedFinalStatus = MessageExchange.Status.RESPONSE;
            inv.expectedResponsePattern = Pattern.compile(responsePattern, Pattern.DOTALL);
        }

        _invocations.add(inv);
        return inv;
    }
    
    protected void go() throws Exception {
        try {
            doDeployments();
            doInvokes();
        } finally {
            checkFailure();
        }
    }

    protected void checkFailure() {
        for (Failure failure : _failures)
            System.err.println(failure);

        assertTrue(_failures.size() == 0);
    }

    
    protected Deployment deploy(String location) {
        Deployment deployment = new Deployment(makeDeployDir(location));
        doDeployment(deployment);
        return deployment;
    }
    
    protected void doDeployments() {
        for (Deployment d : _deployments)
            doDeployment(d);
    }

    /**
     * Do all the registered deployments.
     * 
     * @param d
     */
    protected void doDeployment(Deployment d) {
        Collection<QName> procs;

        try {
            procs = store.deploy(d.deployDir);
            
            _deployed.add(d);
        } catch (Exception ex) {
            if (d.expectedException == null) {
                ex.printStackTrace();
                failure(d, "DEPLOY: Unexpected exception: " + ex, ex);
            } else if (!d.expectedException.isAssignableFrom(ex.getClass())) {
                ex.printStackTrace();
                failure(d, "DEPLOY: Wrong exception; expected " + d.expectedException + " but got " + ex.getClass(), ex);                
            }


            return;
        }

        try {
            for (QName procName : procs)
                _server.register(store.getProcessConfiguration(procName));
        } catch (Exception ex) {
            if (d.expectedException == null)
                failure(d, "REGISTER: Unexpected exception: " + ex, ex);
            else if (!d.expectedException.isAssignableFrom(ex.getClass()))
                failure(d, "REGISTER: Wrong exception; expected " + d.expectedException + " but got " + ex.getClass(), ex);
        }
    }

    protected void doUndeployments() {
        for (Deployment d : _deployments) {
            try {
                undeploy(d);
            } catch (Exception ex) {
                ex.printStackTrace();
                failure(d, "Undeployment failed.", ex);
            }
        }
        
        _deployments.clear();
    }

    protected void undeploy(Deployment d) {
        if (_deployed.contains(d)) {
            _deployed.remove(d);
            store.undeploy(d.deployDir);
        }
    }
    protected void doInvokes() throws Exception {
        ArrayList<Thread> testThreads = new ArrayList<Thread>();
        for (Invocation i : _invocations) {
            InvokerThread t = new InvokerThread(i);
            testThreads.add(t);
        }

        for (Thread testThread : testThreads)
            testThread.start();

        for (Thread testThread : testThreads)
            testThread.join();

    }

    private void failure(Object where) {
        failure(where, "Failure", null);
    }

    private void failure(Object where, String message, Exception ex) {
        Failure f = new Failure(where, message, ex);
        _failures.add(f);
        fail(f.toString());
    }

    private void failure(Object where, String message, Object expected, Object actual) {
        Failure f = new Failure(where, message, expected, actual, null);
        _failures.add(f);
        fail(f.toString());
    }

    protected boolean isFailed() {
        return !_failures.isEmpty();
    }

    protected File makeDeployDir(String deployDir) {
        String deployxml = deployDir + "/deploy.xml";
        URL deployxmlurl = getClass().getResource(deployxml);
        if (deployxmlurl == null) {
            fail("Resource not found: " + deployxml);
        }
        return new File(deployxmlurl.getPath()).getParentFile();
    }

    protected static class Failure {
        Object where;

        String msg;

        Object expected;

        Object actual;

        Exception ex;

        public Failure(Object where, String msg, Exception ex) {
            this(where, msg, null, null, ex);
        }

        public Failure(Object where, String msg, Object expected, Object actual, Exception ex) {
            this.actual = actual;
            this.expected = expected;
            this.where = where;
            this.msg = msg;
            this.ex = ex;
        }

        public String toString() {
            StringBuffer sbuf = new StringBuffer(where + ": " + msg);
            if (actual != null)
                sbuf.append("; got " + actual + ", expected " + expected);
            return sbuf.toString();
        }
    }

    /**
     * Represents a test deployement.
     * 
     * @author mszefler
     * 
     */
    public static class Deployment {
        /** The directory containing the deploy.xml and artefacts. */
        public File deployDir;

        /** If non-null the type of exception we expect to get when we deploy. */
        public Class expectedException = null;

        public Deployment(File deployDir) {
            this.deployDir = deployDir;
        }

        public String toString() {
            return "Deployment#" + deployDir;
        }
    }

    /**
     * Represents an test invocation of the BPEL engine.
     * 
     * @author mszefler
     */
    public static class Invocation {
        /** Identifier (for reporting). */
        public String id;

        /** Name of the operation to invoke. */
        public String operation;

        /** Name of service to invoke. */
        public QName target;

        /** Expected RegExp pattern for the response, or null */
        public Pattern expectedResponsePattern;

        /** The request message that should be sent to the server */
        public Element request;

        /** Number of ms to wait (relative to other invokes) before invoking. */
        public long invokeDelayMs = 0L;

        /** If non-null, expect an exception of this class (or subclass) on invoke. */
        public Class expectedInvokeException = null;

        /** If non-null, expecte this status right after invoke. */
        public MessageExchange.Status expectedStatus = null;

        /** If non-null, expect this status after response received. */
        public MessageExchange.Status expectedFinalStatus = MessageExchange.Status.COMPLETED_OK;

        /** If non-null, expect this correlation status right after invoke. */
        public CorrelationStatus expectedCorrelationStatus = null;

        /** If non-null, expect this correlation after response received. */
        public CorrelationStatus expectedFinalCorrelationStatus = null;

        /** Maximum number of ms to wait for a response. */
        public long maximumWaitMs = 60 * 1000;

        /** If non-null, minimum number of ms before a response should be available. */
        public Long minimumWaitMs = null;

        long invokeTime;

        Exception invokeException;

        QName requestType;

        public Invocation(String id) {
            this.id = id;
        }

        public String toString() {
            return "Invocation#" + id;
        }

    }

    class InvokerThread extends Thread {
        Invocation _invocation;

        InvokerThread(Invocation invocation) {
            _invocation = invocation;
        }

        public void run() {
            final MyRoleMessageExchange mex;
            final Future<MessageExchange.Status> running;

            // Wait for it....
            try {
                Thread.sleep(_invocation.invokeDelayMs);
            } catch (Exception ex) {
            }

            scheduler.begin();
            try {
                mex = _server.getEngine().createMessageExchange(new GUID().toString(), _invocation.target, _invocation.operation);
                mexContext.clearCurrentResponse();

                Message request = mex.createMessage(_invocation.requestType);
                request.setMessage(_invocation.request);
                _invocation.invokeTime = System.currentTimeMillis();
                running = mex.invoke(request);

                Status status = mex.getStatus();
                CorrelationStatus cstatus = mex.getCorrelationStatus();
                if (_invocation.expectedStatus != null && !status.equals(_invocation.expectedStatus))
                    failure(_invocation, "Unexpected message exchange status", _invocation.expectedStatus, status);

                if (_invocation.expectedCorrelationStatus != null && !cstatus.equals(_invocation.expectedCorrelationStatus))
                    failure(_invocation, "Unexpected correlation status", _invocation.expectedCorrelationStatus, cstatus);

            } catch (Exception ex) {
                if (_invocation.expectedInvokeException == null)
                    failure(_invocation, "Unexpected invocation exception.", ex);
                else if (_invocation.expectedInvokeException.isAssignableFrom(ex.getClass()))
                    failure(_invocation, "Unexpected invocation exception.", _invocation.expectedInvokeException, ex.getClass());

                return;
            } finally {
                scheduler.commit();
            }

            if (isFailed())
                return;

            try {
                running.get(_invocation.maximumWaitMs, TimeUnit.MILLISECONDS);
            } catch (Exception ex) {
                failure(_invocation, "Exception on future object.", ex);
                return;
            }

            long ctime = System.currentTimeMillis();
            long itime = ctime - _invocation.invokeTime;
            if (_invocation.minimumWaitMs != null && _invocation.minimumWaitMs >= itime)
                failure(_invocation, "Response received too soon.", _invocation.minimumWaitMs, itime);

            if (_invocation.maximumWaitMs <= itime)
                failure(_invocation, "Response took too long.", _invocation.maximumWaitMs, itime);

            if (isFailed())
                return;

            scheduler.begin();
            try {
                Status finalstat = mex.getStatus();
                if (_invocation.expectedFinalStatus != null && !_invocation.expectedFinalStatus.equals(finalstat))
                    failure(_invocation, "Unexpected final message exchange status", _invocation.expectedFinalStatus, finalstat);

                if (_invocation.expectedFinalCorrelationStatus != null
                        && !_invocation.expectedFinalCorrelationStatus.equals(mex.getCorrelationStatus())) {
                    failure(_invocation, "Unexpected final correlation status", _invocation.expectedFinalCorrelationStatus, mex
                            .getCorrelationStatus());
                }
                if (_invocation.expectedResponsePattern != null) {
                    if (mex.getResponse() == null)
                        failure(_invocation, "Expected response, but got none.", null);
                    String responseStr = DOMUtils.domToString(mex.getResponse().getMessage());
                    Matcher matcher = _invocation.expectedResponsePattern.matcher(responseStr);
                    if (!matcher.matches())
                        failure(_invocation, "Response does not match expected pattern", _invocation.expectedResponsePattern, responseStr);
                }
            } finally {
                scheduler.commit();
            }
        }
    }
}
