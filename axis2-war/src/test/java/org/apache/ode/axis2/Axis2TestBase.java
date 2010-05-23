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

package org.apache.ode.axis2;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.engine.AxisServer;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.ode.axis2.hooks.ODEAxisService;
import org.apache.ode.axis2.util.Axis2UriResolver;
import org.apache.ode.axis2.util.Axis2WSDLLocator;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.tools.sendsoap.cline.HttpSoapSender;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;


import javax.servlet.ServletException;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Matthieu Riou <mriou@apache.org>
 */
public abstract class Axis2TestBase {
    private static final int DEFAULT_TEST_PORT_0 = 8888;
    private static final int DEFAULT_TEST_PORT_1 = 7070;
    private static final String DEFAULT_TEST_PORTS = DEFAULT_TEST_PORT_0+","+DEFAULT_TEST_PORT_1;

    private static final Log log = LogFactory.getLog(Axis2TestBase.class);

    protected ODEAxis2Server server;

    protected String config;
    
    protected static final String DO_NOT_OVERRIDE_CONFIG = "<DO_NOT_OVERRIDE_CONFIG>";

    private static String originalOdePersistence = System.getProperty("ode.persistence");
    private static String originalOdeConfigDir = System.getProperty("org.apache.ode.configDir");
    
    static {
        // disable deferred process instance cleanup for faster testing
        System.setProperty(BpelServerImpl.DEFERRED_PROCESS_INSTANCE_CLEANUP_DISABLED_NAME, "true");
        /*
         The property "test.ports" receives a coma-separated list of available ports.
         Base on this list, a set of properties is created:
            test.port.0, test.port.1, test.port.2, ...
         These properties might then be used by test cases using #getTestPort(int) or from endpoint property files using ${system.test.port.0} for instance.
          */
        if(StringUtils.isBlank(System.getProperty("test.ports"))) System.setProperty("test.ports", DEFAULT_TEST_PORTS);
        log.info("test.ports="+System.getProperty("test.ports"));
        String[] ports = System.getProperty("test.ports").split(",");
        for (int i = 0; i < ports.length; i++) {
            String port = ports[i].trim();
            System.setProperty("test.port."+i, port);
        }
    }

    public int getTestPort(int index){
        return Integer.parseInt(System.getProperty("test.port."+index));
    }

    @DataProvider(name = "configs")
    protected Iterator<Object[]> createConfigData() {
        List<String> configDirList = new ArrayList<String>();
        if( !(this instanceof ODEConfigDirAware) || ((ODEConfigDirAware)this).getODEConfigDir().contains("hib")) {
            addToConfigDirList(configDirList, "org.apache.ode.hibdbs");
        }
        if( !(this instanceof ODEConfigDirAware) || !((ODEConfigDirAware)this).getODEConfigDir().contains("hib")) {
            addToConfigDirList(configDirList, "org.apache.ode.jpadbs");
        }

        if( configDirList.isEmpty() ) {
            // if no system property is set, fall back to default
            if( this instanceof ODEConfigDirAware ) {
                configDirList.add(((ODEConfigDirAware)this).getODEConfigDir());
            } else {
                configDirList.add(DO_NOT_OVERRIDE_CONFIG);
            }
        } else {
            System.out.println("Java system properties have been set to override ode configuration: " + configDirList);
        }
        
        final Iterator<String> itr = configDirList.iterator();
        return new Iterator<Object[]>() {
            public boolean hasNext() {
                return itr.hasNext();
            }

            public Object[] next() {
                config = itr.next();
                return new Object[] {};
            }

            public void remove() {
            }
        };
    }

    private void addToConfigDirList(List<String> configDirList, String propertyKey) {
        String dbs = System.getProperty(propertyKey);
        if( dbs != null ) {
            String[] configDirs = dbs.split(",");
            for( String configDir : configDirs ) {
                String trimmed = configDir.trim();
                if( trimmed.length() > 0 ) {
                    configDirList.add(trimmed);
                }
            }
        }
    }
    
    public void startServer() throws Exception {
        startServer("webapp/WEB-INF", "webapp/WEB-INF/conf/axis2.xml");
    }

    public void startServer(String axis2RepoDir, String axis2ConfLocation) throws Exception {
        String odeRootAbsolutePath = getClass().getClassLoader().getResource("webapp/WEB-INF").getFile();
        String axis2RepoAbsolutePath = getClass().getClassLoader().getResource(axis2RepoDir).getFile();
        String axis2ConfAbsolutePath = axis2ConfLocation == null ? null : getClass().getClassLoader().getResource(axis2ConfLocation).getFile();
        server = new ODEAxis2Server(odeRootAbsolutePath, axis2RepoAbsolutePath, axis2ConfAbsolutePath);
        server.start();
    }

    public void stopServer() throws AxisFault {
        server.stop();
    }

    @BeforeMethod
    protected void setUp() throws Exception {
        log.debug("##### Running "+getClass().getName());
        /**
         * 1. If no settings are given from buildr, the test runs with the default config directory.
         * 2. If no settings are given from buildr and if the test implements ODEConfigDirAware, the test runs with
         * the config directory from the interface.
         * 3. If settings are given from buildr and if it's derby and openJPA, test falls back to the above 1 or 2.
         * 4. If settings are given from buildr and if it's derby and hibernate, test falls back to the above 2 or
         * uses -Dode.persistence=hibernate.
         */
        if( config == null || DO_NOT_OVERRIDE_CONFIG.equals(config) ) {
            System.out.println("Test config: default.");
        } else if("<jpa>".equals(config)) {
            if( this instanceof ODEConfigDirAware ) {
                config = ((ODEConfigDirAware)this).getODEConfigDir();
                System.out.println("Test config: " + config + ".");
                System.setProperty("org.apache.ode.configDir", config);
            } else {
                System.out.println("Test config: default.");
            }
        } else if("<hib>".equals(config)) {
            if( this instanceof ODEConfigDirAware ) {
                config = ((ODEConfigDirAware)this).getODEConfigDir();
                System.out.println("Test config: " + config + ".");
                System.setProperty("org.apache.ode.configDir", config);
            } else {
                // why does this not work?
//              System.out.println("Test config: -Dode.persistence=hibernate");
//              System.setProperty("ode.persistence", "hibernate");
                config = getClass().getClassLoader().getResource("webapp").getFile() + "/WEB-INF/conf.hib-derby";
                System.out.println("Test config: " + config + ".");
                System.setProperty("org.apache.ode.configDir", config);
            }
        } else {
            System.out.println("Test config: " + config + ".");
            System.setProperty("org.apache.ode.configDir", config);
        }

        startServer();
    }

    @AfterMethod
    protected void tearDown() throws Exception {
        stopServer();

        if( originalOdeConfigDir != null ) {
            System.setProperty("org.apache.ode.configDir", originalOdeConfigDir);
        } else {
            System.clearProperty("org.apache.ode.configDir");
        }
        if( originalOdeConfigDir != null ) {
            System.setProperty("ode.persistence", originalOdePersistence);      
        } else {
            System.clearProperty("ode.persistence");
        }
    }

    protected class ODEAxis2Server extends AxisServer {

        ODEServer _ode;
        String odeRootDir;

        protected ODEAxis2Server(String odeRootDir, String axis2RepoDir, String axis2ConfLocation) throws Exception {
            super(false);
            this.odeRootDir = odeRootDir;
            if (log.isInfoEnabled()) {
                log.info("Ode Root Dir: " + odeRootDir);
                log.info("Axis2 Conf file: " + axis2ConfLocation);
                log.info("Axis2 Repo dir: " + axis2RepoDir);
            }

            configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(axis2RepoDir, axis2ConfLocation);
            // do not use 8080 for tests, and make sure to pass a string, not an int
            configContext.getAxisConfiguration().getTransportIn("http").addParameter(new Parameter("port", ""+getTestPort(0)));
        }

        protected void start() throws AxisFault {
            super.start();
            _ode = new ODEServer();
            try {
                _ode.init(odeRootDir, configContext.getAxisConfiguration());
            } catch (ServletException e) {
                e.printStackTrace();
            }
        }

        public void stop() throws AxisFault {
            _ode.shutDown();
            super.stop();
        }

        public Collection<QName> deployProcess(String bundleName) {
            return _ode.getProcessStore().deploy(new File(getResource(bundleName)));
        }

        public void undeployProcess(String bundleName) {
            _ode.getProcessStore().undeploy(new File(getResource(bundleName)));
        }

        public boolean isDeployed(String bundleName) {
            return _ode.getProcessStore().getPackages().contains(bundleName);
        }

        /**
         * Creates and deploys an Axis service based on a provided MessageReceiver. The receiver
         * will be invoked for all invocations of that service.
         */
        protected void deployService(String bundleName, String defFile, QName serviceName, String port,
                                     MessageReceiver receiver) throws WSDLException, IOException, URISyntaxException {
            URI wsdlUri = new File(getResource(bundleName) + "/" + defFile).toURI();

            InputStream is = wsdlUri.toURL().openStream();
            WSDL11ToAxisServiceBuilder serviceBuilder = new ODEAxisService.WSDL11ToAxisPatchedBuilder(is, serviceName, port);
            serviceBuilder.setBaseUri(wsdlUri.toString());
            serviceBuilder.setCustomResolver(new Axis2UriResolver());
            serviceBuilder.setCustomWSLD4JResolver(new Axis2WSDLLocator(wsdlUri));
            serviceBuilder.setServerSide(true);

            AxisService axisService = serviceBuilder.populateService();
            axisService.setName(serviceName.getLocalPart());
            axisService.setWsdlFound(true);
            axisService.setCustomWsdl(true);
            axisService.setClassLoader(getConfigurationContext().getAxisConfiguration().getServiceClassLoader());

            Iterator operations = axisService.getOperations();
            while (operations.hasNext()) {
                AxisOperation operation = (AxisOperation) operations.next();
                if (operation.getMessageReceiver() == null) {
                    operation.setMessageReceiver(receiver);
                }
            }
            getConfigurationContext().getAxisConfiguration().addService(axisService);
        }

        public String sendRequestFile(String endpoint, String bundleName, String filename) {
            return sendRequestFile(endpoint, bundleName + "/" + filename);
        }
        
        public String sendRequestFile(String endpoint, String filename) {
            try {
                URL url = new URL(endpoint);
                // override the port if necessary but only if the given port is the default one
                if(url.getPort()==DEFAULT_TEST_PORT_0 && url.getPort()!=getTestPort(0)){
                    url=  new URL(url.getProtocol()+"://"+url.getHost()+":"+getTestPort(0)+url.getPath()+(url.getQuery()!=null?"?"+url.getQuery():""));
                }
                return HttpSoapSender.doSend(url,
                        new FileInputStream(getResource(filename)), null, 0, null, null, null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        protected String getResource(String bundleName) {
            return getClass().getClassLoader().getResource(bundleName).getFile();
        }

        /**
         * Convenient methods to generate a WSDL for an Axis2 service. Often nice, but also often
         * generates crappy WSDL that aren't even valid (especially when faults are involved) so
         * use with care.
         *
         * @param serviceName
         * @param fileName
         * @throws AxisFault
         */
        protected void generateWSDL(String serviceName, String fileName) throws AxisFault {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(fileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            configContext.getAxisConfiguration().getService(serviceName).printWSDL(fos);
        }

        public ODEServer getODEServer() {
            return _ode;
        }
    }
}
