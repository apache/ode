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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.engine.AxisServer;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.hooks.ODEAxisService;
import org.apache.ode.axis2.util.Axis2UriResolver;
import org.apache.ode.axis2.util.Axis2WSDLLocator;
import org.apache.axis2.transport.http.SimpleHTTPServer;

public class ODEAxis2Server extends AxisServer {
    private static final Log log = LogFactory.getLog(ODEAxis2Server.class);

        ODEServer _ode;
        String odeRootDir;
        ODEConfigProperties config;
        public Runnable txMgrCreatedCallback = null;

        public ODEAxis2Server(String odeRootDir, String axis2RepoDir, String axis2ConfLocation, int port, ODEConfigProperties config) throws Exception {
            super(false);
            this.config = config;
            this.odeRootDir = odeRootDir;
            if (log.isInfoEnabled()) {
                log.info("Ode Root Dir: " + odeRootDir);
                log.info("Axis2 Conf file: " + axis2ConfLocation);
                log.info("Axis2 Repo dir: " + axis2RepoDir);
            }

            configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(axis2RepoDir, axis2ConfLocation);
            SimpleHTTPServer receiver = new SimpleHTTPServer(configContext, port);
            TransportInDescription trsIn = configContext.getAxisConfiguration().getTransportIn(Constants.TRANSPORT_HTTP);
            trsIn.setReceiver(receiver);
        }

        public ODEAxis2Server(String odeRootDir, URL axis2xml, URL axis2repository, int port, ODEConfigProperties config) throws Exception {
            super(false);
            this.config = config;
            this.odeRootDir = odeRootDir;
            if (log.isInfoEnabled()) {
                log.info("Ode Root Dir: " + odeRootDir);
                log.info("Axis2 Conf file: " + axis2xml);
                log.info("Axis2 Repo dir: " + axis2repository);
            }

            configContext = ConfigurationContextFactory.createConfigurationContextFromURIs(axis2xml, axis2repository);
            SimpleHTTPServer receiver = new SimpleHTTPServer(configContext, port);
            TransportInDescription trsIn = configContext.getAxisConfiguration().getTransportIn(Constants.TRANSPORT_HTTP);
            trsIn.setReceiver(receiver);
        }

        public void start() throws AxisFault {
            super.start();
            _ode = new ODEServer();
            _ode.txMgrCreatedCallback = txMgrCreatedCallback;
            try {
                _ode.init(odeRootDir, new ConfigurationContext(configContext.getAxisConfiguration()), config);
            } catch (ServletException e) {
                throw new RuntimeException(e.getRootCause());
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
            serviceBuilder.setCustomWSDLResolver(new Axis2WSDLLocator(wsdlUri));
            serviceBuilder.setServerSide(true);

            AxisService axisService = serviceBuilder.populateService();
            axisService.setName(serviceName.getLocalPart());
            axisService.setWsdlFound(true);
            axisService.setCustomWsdl(true);
            axisService.setClassLoader(getConfigurationContext().getAxisConfiguration().getServiceClassLoader());

            Iterator<AxisOperation> operations = axisService.getOperations();
            while (operations.hasNext()) {
                AxisOperation operation = operations.next();
                if (operation.getMessageReceiver() == null) {
                    operation.setMessageReceiver(receiver);
                }
            }
            getConfigurationContext().getAxisConfiguration().addService(axisService);
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
