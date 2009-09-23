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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.xml.namespace.QName;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.evtproc.DebugBpelEventListener;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.bpel.iapi.MessageExchange.AckType;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange.CorrelationStatus;
import org.apache.ode.bpel.memdao.BpelDAOConnectionFactoryImpl;
import org.apache.ode.bpel.rtrep.common.extension.AbstractExtensionBundle;
import org.apache.ode.dao.jpa.BPELDAOConnectionFactoryImpl;
import org.apache.ode.il.MockScheduler;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.store.ProcessConfImpl;
import org.apache.ode.store.ProcessStoreImpl;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.w3c.dom.Element;

public abstract class BPELTestAbstract {
    private static final String SHOW_EVENTS_ON_CONSOLE = "no";

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

    private MockTransactionManager _txm;

    @Before
    public void setUp() throws Exception {
        _failures = new CopyOnWriteArrayList<Failure>();
        _server = new BpelServerImpl();
        mexContext = new MessageExchangeContextImpl();
        _deployments = new ArrayList<Deployment>();
        _invocations = new ArrayList<Invocation>();
        _deployed = new ArrayList<Deployment>();

        if (Boolean.getBoolean("org.apache.ode.test.persistent")) {

            _server.setDaoConnectionFactory(_cf);
            _txm = new MockTransactionManager();

            BPELDAOConnectionFactoryImpl cf = new BPELDAOConnectionFactoryImpl();
            cf.setTransactionManager(_txm);
            // cf.setDataSource(datasource);
            scheduler = new MockScheduler(_txm);
        } else {
            _txm = new MockTransactionManager();
            scheduler = new MockScheduler(_txm);
            _cf = new BpelDAOConnectionFactoryImpl(_txm);
            _server.setDaoConnectionFactory(_cf);
        }
        _server.setScheduler(scheduler);
        _server.setBindingContext(new BindingContextImpl());
        _server.setMessageExchangeContext(mexContext);
        _server.setTransactionManager(_txm);
        scheduler.setJobProcessor(_server);
        final EndpointReferenceContext eprContext = new EndpointReferenceContext() {
            public EndpointReference resolveEndpointReference(Element epr) {
                return null;
            }

            public EndpointReference convertEndpoint(QName targetType, Element sourceEndpoint) {
                return null;
            }

            public Map getConfigLookup(EndpointReference epr) {
                return Collections.EMPTY_MAP;
            }
        };
        store = new ProcessStoreImpl(eprContext, null, "jpa", new OdeConfigProperties(new Properties(), ""), true);
        // not needed: we do eclipcitly in doDeployment
//        store.registerListener(new ProcessStoreListener() {
//            public void onProcessStoreEvent(ProcessStoreEvent event) {
//                // bounce the process
//                _server.unregister(event.pid);
//                if (event.type != ProcessStoreEvent.Type.UNDEPLOYED) {
//                    ProcessConfImpl conf = (ProcessConfImpl) store.getProcessConfiguration(event.pid);
//                    // Test processes always run with in-mem DAOs
//                    conf.setTransient(true);
//                    _server.register(conf);
//                }
//            }
//        });
        _server.setConfigProperties(new OdeConfigProperties(getConfigProperties(), ""));
        _server.registerBpelEventListener(new DebugBpelEventListener());
        _server.init();
        _server.start();
    }

    @After
    public void tearDown() throws Exception {
        for (Deployment d : _deployed) {
            try {
                store.undeploy(d.deployDir);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("Error undeploying " + d);
            }
        }

        if (em != null) em.close();
        if (emf != null) emf.close();

        _server.stop();
        _failures = null;
        _deployed = null;
        _deployments = null;
        _invocations = null;
    }

    public void registerExtensionBundle(AbstractExtensionBundle bundle) {
    	_server.registerExtensionBundle(bundle);
    }
    
    public void unregisterExtensionBundle(AbstractExtensionBundle bundle) {
    	_server.unregisterExtensionBundle(bundle.getNamespaceURI());
    }

    public void unregisterExtensionBundle(String nsURI) {
    	_server.unregisterExtensionBundle(nsURI);
    }

    protected void negative(String deployDir) throws Throwable {
        try {
            go(new File(deployDir));
        } catch (junit.framework.AssertionFailedError ex) {
            return;
        }
        Assert.fail("Expecting test to fail");
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

        if (!testPropsFile.exists()) {
            Assert.fail("Test property file not found in " + deployDir);
        }

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

            Boolean sequential = Boolean.parseBoolean(testProps.getProperty("sequential", "false"));
            
            Invocation last = null;
            for (int i = 1; testProps.getProperty("request" + i) != null; i++) {
                final String in = testProps.getProperty("request" + i);
                final String responsePattern = testProps.getProperty("response" + i);

                last = addInvoke(testPropsFile + "#" + i, serviceId, operation, in, responsePattern, sequential ? last : null);
            }
            propsFileCnt++;
            testPropsFile = new File(deployDir, "test" + propsFileCnt + ".properties");
        }
    }

    protected Invocation addInvoke(String id, QName target, String operation, String request, String responsePattern) throws Exception {
        return addInvoke(id, target, operation, request, responsePattern, null);
    }
    
    protected Invocation addInvoke(String id, QName target, String operation, String request, String responsePattern, Invocation synchronizeWith)
            throws Exception {

        Invocation inv = new Invocation(id, synchronizeWith);
        inv.target = target;
        inv.operation = operation;
        inv.request = DOMUtils.stringToDOM(request);
        if (responsePattern != null) {
            if ("ONEWAY".equals(responsePattern)) {
            	inv.expectedFinalStatus = AckType.ONEWAY;
            	inv.expectedResponsePattern = null;
            } else if ("FAULT".equals(responsePattern)) {
            	inv.expectedFinalStatus = AckType.FAULT;
            	inv.expectedResponsePattern = null;
            } else if ("FAILURE".equals(responsePattern)) {
            	inv.expectedFinalStatus = AckType.FAILURE;
            	inv.expectedResponsePattern = null;
            } else {
                inv.expectedResponsePattern = Pattern.compile(responsePattern, Pattern.DOTALL);
                inv.expectedFinalStatus = AckType.RESPONSE;
            }
        } else
            inv.expectedFinalStatus = AckType.ONEWAY;

        _invocations.add(inv);
        return inv;
    }

    public void go() throws Exception {
        try {
            doDeployments();
            doInvokes();
        } finally {
            checkFailure();
        }
    }

    protected void checkFailure() {
        StringBuffer sb = new StringBuffer("Failure report:\n");
        for (Failure failure : _failures) {
            sb.append(failure);
            sb.append('\n');
        }
        if (_failures.size() != 0) {
            System.err.println(sb.toString());
            Assert.fail(sb.toString());
        }
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
            for (QName procName : procs) {
                ProcessConfImpl conf = (ProcessConfImpl) store.getProcessConfiguration(procName);
                // We want to use our own cf
                conf.setTransient(false);
                _server.register(conf);
            }
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
    
    protected int getMaximumWaitInMillis() {
    	return 60 * 1000;
    }

    protected int getMinimumWaitInMillis() {
    	return -1;
    }
    
    protected void doInvokes() throws Exception {
        ArrayList<Thread> testThreads = new ArrayList<Thread>();
        for (Invocation i : _invocations) {
            InvokerThread t = new InvokerThread(i);
            testThreads.add(t);
        }

        for (Thread testThread : testThreads) {
            testThread.start();
            if (testThreads.size() > 0) Thread.sleep(2000);
        }

        for (Thread testThread : testThreads)
            testThread.join();

    }

    private void failure(Object where) {
        failure(where, "Failure", null);
    }

    private void failure(Object where, String message, Exception ex) {
        Failure f = new Failure(where, message, ex);
        _failures.add(f);
        ex.printStackTrace();
        Assert.fail(f.toString());
    }

    private void failure(Object where, String message, Object expected, Object actual) {
        Failure f = new Failure(where, message, expected, actual, null);
        _failures.add(f);
        Assert.fail(f.toString());
    }

    protected boolean isFailed() {
        return !_failures.isEmpty();
    }

    protected File makeDeployDir(String deployDir) {
        String deployxml = deployDir + "/deploy.xml";
        URL deployxmlurl = getClass().getResource(deployxml);
        if (deployxmlurl == null) {
            Assert.fail("Resource not found: " + deployxml);
        }
        try {
            return new File(deployxmlurl.toURI().getPath()).getParentFile();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
            return null;
        }
    }

    /**
     * Override this to provide configuration properties for Ode extensions like BpelEventListeners.
     * 
     * @return
     */
    protected Properties getConfigProperties() {
        // could also return null, returning an empty properties
        // object is more fail-safe.
        Properties p = new Properties();
        p.setProperty("debugeventlistener.dumpToStdOut", SHOW_EVENTS_ON_CONSOLE);        
        return p;
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
            if (ex != null) {
            	sbuf.append("; got exception:\n");
            	sbuf.append(ExceptionUtils.getFullStackTrace(ex));
            }
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

        /** for sync invocations */
        public Invocation synchronizeWith;
        
        /** checking completion */
        public boolean done = false;
        
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

        /** If non-null, expect this status after response received. */
        public AckType expectedFinalStatus = AckType.RESPONSE;

        /** If non-null, expect this correlation status right after invoke. */
        public CorrelationStatus expectedCorrelationStatus = null;

        /** If non-null, expect this correlation after response received. */
        public CorrelationStatus expectedFinalCorrelationStatus = null;

        /** Maximum number of ms to wait for a response. */
        public long maximumWaitMs = 60 * 1000;

        /** If non-null, minimum number of ms before a response should be available. */
        public long minimumWaitMs = -1;

        long invokeTime;

        Exception invokeException;

        QName requestType;

        public Invocation(String id, Invocation synchronizeWith) {
            this.id = id;
            this.synchronizeWith = synchronizeWith;
        }

        public String toString() {
            return "Invocation#" + id;
        }
        
    }

    class InvokerThread extends Thread {
        Invocation _invocation;

        InvokerThread(Invocation invocation) {
            _invocation = invocation;
            _invocation.maximumWaitMs = getMaximumWaitInMillis();
            _invocation.minimumWaitMs = getMinimumWaitInMillis();
        }

        public void run() {
            try {
                run2();
            } finally {
                synchronized (_invocation) {
                    _invocation.done = true;
                    _invocation.notify();
                }
            }
        }
        
        public void run2() {
            final MyRoleMessageExchange mex;
            final Future<MessageExchange.Status> running;

            // Wait for it....
            try {
                Thread.sleep(_invocation.invokeDelayMs * 1000);
            } catch (Exception ex) {
            }
            
            if (_invocation.synchronizeWith != null) {
                synchronized (_invocation.synchronizeWith) {
                    while (!_invocation.synchronizeWith.done) {
                        try {
                            _invocation.synchronizeWith.wait(_invocation.maximumWaitMs);
                        } catch (InterruptedException e) {
                            failure(_invocation, "timed out waiting in sequence", e);
                            return;
                        }
                    }
                }
            }

            try {
                mex = _server.createMessageExchange(InvocationStyle.UNRELIABLE, _invocation.target, _invocation.operation,
                        new GUID().toString());

                Message request = mex.createMessage(_invocation.requestType);
                request.setMessage(_invocation.request);
                _invocation.invokeTime = System.currentTimeMillis();
                mex.setRequest(request);
                mex.invokeBlocking();

                CorrelationStatus cstatus = mex.getCorrelationStatus();
                if (_invocation.expectedCorrelationStatus != null && !cstatus.equals(_invocation.expectedCorrelationStatus))
                    failure(_invocation, "Unexpected correlation status", _invocation.expectedCorrelationStatus, cstatus);

            } catch (Exception ex) {
                if (_invocation.expectedInvokeException == null)
                    failure(_invocation, "Unexpected invocation exception.", ex);
                else if (_invocation.expectedInvokeException.isAssignableFrom(ex.getClass()))
                    failure(_invocation, "Unexpected invocation exception.", _invocation.expectedInvokeException, ex.getClass());

                return;
            }

            if (mex.getStatus() != Status.ACK)
                failure(_invocation, "No ACK status", Status.ACK.toString(), mex.getStatus().toString());

            if (isFailed())
                return;

            long ctime = System.currentTimeMillis();
            long itime = ctime - _invocation.invokeTime;
            if (_invocation.minimumWaitMs != -1 && _invocation.minimumWaitMs >= itime)
                failure(_invocation, "Response received too soon.", _invocation.minimumWaitMs, itime);

            if (_invocation.maximumWaitMs <= itime)
                failure(_invocation, "Response took too long.", _invocation.maximumWaitMs, itime);

            if (isFailed())
                return;

            AckType finalstat = mex.getAckType();
            if (_invocation.expectedFinalStatus != null && _invocation.expectedFinalStatus != finalstat) {
                if (finalstat.equals(AckType.FAULT)) {
                    failure(_invocation, "Unexpected final message exchange status", _invocation.expectedFinalStatus, "FAULT: "
                            + mex.getFault() + " | " + mex.getFaultExplanation());
                } else {
                    failure(_invocation, "Unexpected final message exchange status", _invocation.expectedFinalStatus, finalstat);
                }
            }

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
            		failure(_invocation, "Response does not match expected pattern", _invocation.expectedResponsePattern,
            				responseStr);
            }
        }
    }
}
