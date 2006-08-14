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

package org.apache.ode.axis2.service;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.util.Utils;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.DeploymentUnit;
import org.apache.ode.axis2.hooks.ODEAxisService;
import org.apache.ode.axis2.deploy.DeploymentPoller;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.activation.DataHandler;
import java.io.IOException;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.Iterator;

/**
 * Axis wrapper for process deployment.
 */
public class DeploymentService {

    private static final Log __log = LogFactory.getLog(DeploymentService.class);

    private BpelServer _server;
    private File _deployPath;
    private DeploymentPoller _poller;

    public void enableService(AxisConfiguration axisConfig, BpelServer server,
                              DeploymentPoller poller, String rootpath) {
        _server = server;
        _deployPath = new File(rootpath, "processes");

        Definition def;
        try {
            WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
            def = wsdlReader.readWSDL(rootpath + "/deploy.wsdl");
            AxisService deployService = ODEAxisService.createService(
                    axisConfig, new QName("http://www.apache.org/ode/deployapi", "DeploymentService"),
                    "DeploymentPort", "DeploymentService", def, new DeploymentMessageReceiver());
            axisConfig.addService(deployService);
            _poller = poller;
        } catch (WSDLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class DeploymentMessageReceiver extends AbstractMessageReceiver {
        public void receive(MessageContext messageContext) throws AxisFault {
            if (messageContext.getAxisOperation().getName().getLocalPart().equals("deploy")) {
                OMElement namePart = messageContext.getEnvelope().getBody().getFirstElement().getFirstElement();
                OMElement zipPart = (OMElement) namePart.getNextOMSibling();
                OMElement zip = zipPart.getFirstElement();
                if (!zipPart.getQName().getLocalPart().equals("package") ||
                        !zip.getQName().getLocalPart().equals("zip"))
                    throw new AxisFault("Your message should contain a part named 'package' with a zip element");

                OMText binaryNode = (OMText) zip.getFirstOMChild();
                binaryNode.setOptimize(true);
                try {
                    // We're going to create a directory under the deployment root and put
                    // files in there. The poller shouldn't pick them up so we're asking
                    // it to hold on for a while.
                    _poller.hold();
                    File dest = new File(_deployPath, namePart.getText());
                    dest.mkdir();
                    unzip(dest, (DataHandler) binaryNode.getDataHandler());

                    _server.deploy(dest);
                    // Telling the poller what we deployed so that it doesn't try to deploy it again
                    _poller.markAsDeployed(dest);
                    __log.info("Deployment of artifact " + dest.getName() + " successful.");
                    sendResponse(messageContext, "deployResponse", true);
                } finally {
                    _poller.release();
                }
            } else if (messageContext.getAxisOperation().getName().getLocalPart().equals("undeploy")) {
                OMElement part = messageContext.getEnvelope().getBody().getFirstElement().getFirstElement();

                String elmtStr = part.getText();
                File deploymentDir = new File(_deployPath, elmtStr);
                if (!deploymentDir.exists())
                    throw new AxisFault("Couldn't find deployment package " + elmtStr + " in directory " + _deployPath);

                try {
                    // We're going to create a directory under the deployment root and put
                    // files in there. The poller shouldn't pick them up so we're asking
                    // it to hold on for a while.
                    _poller.hold();
                    boolean result = _server.undeploy(deploymentDir);
                    sendResponse(messageContext, "undeployResponse", result);
                    _poller.markAsUndeployed(deploymentDir);
                } finally {
                    _poller.release();
                }
            } else
                throw new AxisFault("Unknown operation: '" + messageContext.getAxisOperation().getName() + "'");
        }

        private void unzip(File dest, DataHandler dataHandler) throws AxisFault {
            try {
                ZipInputStream zis = new ZipInputStream(dataHandler.getDataSource().getInputStream());
                ZipEntry entry;
                // Processing the package
                while((entry = zis.getNextEntry()) != null) {
                    if(entry.isDirectory()) {
                        __log.debug("Extracting directory: " + entry.getName());
                        new File(_deployPath, entry.getName()).mkdir();
                        continue;
                    }
                    __log.debug("Extracting file: " + entry.getName());
                    copyInputStream(zis, new BufferedOutputStream(
                            new FileOutputStream(new File(dest, entry.getName()))));
                }
                zis.close();
            } catch (IOException e) {
                throw new AxisFault("Couldn't open attached deployment package (should be a zip containing the " +
                        "deployment directory).", e);
            }
        }

        private void sendResponse(MessageContext messageContext, String op, boolean result) throws AxisFault {
            MessageContext outMsgContext = Utils.createOutMessageContext(messageContext);
            outMsgContext.getOperationContext().addMessageContext(outMsgContext);

            SOAPFactory factory = getSOAPFactory(messageContext);
            SOAPEnvelope envelope = factory.getDefaultEnvelope();
            outMsgContext.setEnvelope(envelope);

            OMNamespace depns = factory.createOMNamespace("http://www.apache.org/ode/pmapi","deployapi");
            OMElement response = factory.createOMElement("deployResponse", depns);
            OMElement respPart = factory.createOMElement("response", null);
            respPart.setText("true");
            response.addChild(respPart);
            envelope.getBody().addChild(response);
            AxisEngine engine = new AxisEngine(
                    messageContext.getOperationContext().getServiceContext().getConfigurationContext());
            engine.send(outMsgContext);
        }
    }

    private static void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);
        out.close();
    }

}
