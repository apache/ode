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


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.activation.DataHandler;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.ode.axis2.OdeFault;
import org.apache.ode.axis2.deploy.DeploymentPoller;
import org.apache.ode.axis2.hooks.ODEAxisService;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessStore;
import org.apache.ode.il.OMUtils;
import org.apache.ode.utils.fs.FileUtils;
import org.apache.ode.utils.Namespaces;

/**
 * Axis wrapper for process deployment.
 */
public class DeploymentWebService {

    private static final Log __log = LogFactory.getLog(DeploymentWebService.class);

    private final OMNamespace _pmapi;
    private final OMNamespace _deployapi;

    private File _deployPath;
    private DeploymentPoller _poller;
    private ProcessStore _store;
   
    
    public DeploymentWebService() {
        _pmapi = OMAbstractFactory.getOMFactory().createOMNamespace("http://www.apache.org/ode/pmapi","pmapi");
        _deployapi = OMAbstractFactory.getOMFactory().createOMNamespace("http://www.apache.org/ode/deployapi","deployapi");
    }

    public void enableService(AxisConfiguration axisConfig, ProcessStore store,
                              DeploymentPoller poller, String rootpath, String workPath) throws AxisFault, WSDLException {
        _deployPath = new File(workPath, "processes");
        _store = store;
        _poller = poller;

        Definition def;
        WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
        wsdlReader.setFeature("javax.wsdl.verbose", false);

        File wsdlFile = new File(rootpath + "/deploy.wsdl");
        def = wsdlReader.readWSDL(wsdlFile.toURI().toString());
        AxisService deployService = ODEAxisService.createService(
                axisConfig, new QName("http://www.apache.org/ode/deployapi", "DeploymentService"),
                "DeploymentPort", "DeploymentService", def, new DeploymentMessageReceiver());
        axisConfig.addService(deployService);
    }

    class DeploymentMessageReceiver extends AbstractMessageReceiver {

        public void invokeBusinessLogic(MessageContext messageContext) throws AxisFault {
            String operation = messageContext.getAxisOperation().getName().getLocalPart();
            SOAPFactory factory = getSOAPFactory(messageContext);
            boolean unknown = false;

            try {
                if (operation.equals("deploy")) {
                    OMElement deployElement = messageContext.getEnvelope().getBody().getFirstElement();
                    OMElement namePart = deployElement.getFirstChildWithName(new QName(null, "name"));
                    OMElement packagePart = deployElement.getFirstChildWithName(new QName(null, "package"));

                    // "be liberal in what you accept from others"
                    if (packagePart == null) {
                        packagePart = OMUtils.getFirstChildWithName(deployElement, "package");
                        if (packagePart != null && __log.isWarnEnabled()) {
                            __log.warn("Invalid incoming request detected for operation " + messageContext.getAxisOperation().getName() + ". Package part should have no namespace but has " + packagePart.getQName().getNamespaceURI());
                        }
                    }

                    OMElement zip = null;
                    if (packagePart != null) {
                        zip = packagePart.getFirstChildWithName(new QName(Namespaces.ODE_DEPLOYAPI_NS, "zip"));
                        // "be liberal in what you accept from others"
                        if (zip == null) {
                            zip = OMUtils.getFirstChildWithName(packagePart, "zip");
                            if (zip != null && __log.isWarnEnabled()) {
                                String ns = zip.getQName().getNamespaceURI() == null || zip.getQName().getNamespaceURI().length() == 0 ? "empty" : zip.getQName().getNamespaceURI();
                                __log.warn("Invalid incoming request detected for operation " + messageContext.getAxisOperation().getName() + ". <zip/> element namespace should be " + Namespaces.ODE_DEPLOYAPI_NS + " but was " + ns);
                            }
                        }
                    }

                    if (zip == null || packagePart == null)
                        throw new OdeFault("Your message should contain an element named 'package' with a 'zip' element"); 

                    String bundleName = namePart.getText().trim();
                    if (!validBundleName(namePart.getText()))
                        throw new OdeFault("Invalid bundle name, only non empty alpha-numerics and _ strings are allowed.");

                    OMText binaryNode = (OMText) zip.getFirstOMChild();
                    if (binaryNode == null) {
                        throw new OdeFault("Empty binary node under <zip> element");
                    }
                    binaryNode.setOptimize(true);
                    try {
                        // We're going to create a directory under the deployment root and put
                        // files in there. The poller shouldn't pick them up so we're asking
                        // it to hold on for a while.
                        _poller.hold();

                        File dest = new File(_deployPath, bundleName + "-" + _store.getCurrentVersion());
                        dest.mkdir();
                        unzip(dest, (DataHandler) binaryNode.getDataHandler());

                        // Check that we have a deploy.xml
                        File deployXml = new File(dest, "deploy.xml");
                        if (!deployXml.exists())
                            throw new OdeFault("The deployment doesn't appear to contain a deployment " +
                                    "descriptor in its root directory named deploy.xml, aborting.");

                        Collection<QName> deployed = _store.deploy(dest);

                        File deployedMarker = new File(_deployPath, dest.getName() + ".deployed");
                        deployedMarker.createNewFile();

                        // Telling the poller what we deployed so that it doesn't try to deploy it again
                        _poller.markAsDeployed(dest);
                        __log.info("Deployment of artifact " + dest.getName() + " successful.");

                        OMElement response = factory.createOMElement("response", null);

                        if (__log.isDebugEnabled()) __log.debug("Deployed package: "+dest.getName());
                        OMElement d = factory.createOMElement("name", _deployapi);
                        d.setText(dest.getName());
                        response.addChild(d);

                        for (QName pid : deployed) {
                            if (__log.isDebugEnabled()) __log.debug("Deployed PID: "+pid);
                            d = factory.createOMElement("id", _deployapi);
                            d.setText(pid);
                            response.addChild(d);
                        }
                        sendResponse(factory, messageContext, "deployResponse", response);
                    } finally {
                        _poller.release();
                    }
                } else if (operation.equals("undeploy")) {
                    OMElement part = messageContext.getEnvelope().getBody().getFirstElement().getFirstElement();
                    if (part == null) throw new OdeFault("Missing bundle name in undeploy message.");

                    String pkg = part.getText().trim();
                    if (!validBundleName(pkg)) {
                        throw new OdeFault("Invalid bundle name, only non empty alpha-numerics and _ strings are allowed.");
                    }

                    File deploymentDir = new File(_deployPath, pkg);
                    if (!deploymentDir.exists())
                        throw new OdeFault("Couldn't find deployment package " + pkg + " in directory " + _deployPath);

                    try {
                        // We're going to delete files & directories under the deployment root.
                        // Put the poller on hold to avoid undesired side effects
                        _poller.hold();

                        Collection<QName> undeployed = _store.undeploy(deploymentDir);

                        File deployedMarker = new File(deploymentDir + ".deployed");
                        deployedMarker.delete();
                        FileUtils.deepDelete(deploymentDir);

                        OMElement response = factory.createOMElement("response", null);
                        response.setText("" + (undeployed.size() > 0));
                        sendResponse(factory, messageContext, "undeployResponse", response);
                        _poller.markAsUndeployed(deploymentDir);
                    } finally {
                        _poller.release();
                    }
                } else if (operation.equals("listDeployedPackages")) {
                    Collection<String> packageNames = _store.getPackages();
                    OMElement response = factory.createOMElement("deployedPackages", null);
                    for (String name : packageNames) {
                        OMElement nameElmt = factory.createOMElement("name", _deployapi);
                        nameElmt.setText(name);
                        response.addChild(nameElmt);
                    }
                    sendResponse(factory, messageContext, "listDeployedPackagesResponse", response);
                } else if (operation.equals("listProcesses")) {
                    OMElement namePart = messageContext.getEnvelope().getBody().getFirstElement().getFirstElement();
                    List<QName> processIds = _store.listProcesses(namePart.getText());
                    OMElement response = factory.createOMElement("processIds", null);
                    for (QName qname : processIds) {
                        OMElement nameElmt = factory.createOMElement("id", _deployapi);
                        nameElmt.setText(qname);
                        response.addChild(nameElmt);
                    }
                    sendResponse(factory, messageContext, "listProcessesResponse", response);
                } else if (operation.equals("getProcessPackage")) {
                    OMElement qnamePart = messageContext.getEnvelope().getBody().getFirstElement().getFirstElement();
                    ProcessConf process = _store.getProcessConfiguration(OMUtils.getTextAsQName(qnamePart));
                    if (process == null) {
                        throw new OdeFault("Could not find process: " + qnamePart.getTextAsQName());
                    }
                    String packageName = _store.getProcessConfiguration(OMUtils.getTextAsQName(qnamePart)).getPackage();
                    OMElement response = factory.createOMElement("packageName", null);
                    response.setText(packageName);
                    sendResponse(factory, messageContext, "getProcessPackageResponse", response);
                } else unknown = true;
            } catch (Throwable t) {
                // Trying to extract a meaningful message
                Throwable source = t;
                while (source.getCause() != null && source.getCause() != source) source = source.getCause();
                __log.warn("Invocation of operation " + operation + " failed", t);
                throw new OdeFault("Invocation of operation " + operation + " failed: " + source.toString(), t);
            }
            if (unknown) throw new OdeFault("Unknown operation: '"
                    + messageContext.getAxisOperation().getName() + "'");
        }

        private File buildUnusedDir(File deployPath, String dirName) {
            int v = 1;
            while (new File(deployPath, dirName + "-" + v).exists()) v++;
            return new File(deployPath, dirName + "-" + v);
        }

        private void unzip(File dest, DataHandler dataHandler) throws AxisFault {
            try {
                ZipInputStream zis = new ZipInputStream(dataHandler.getDataSource().getInputStream());
                ZipEntry entry;
                // Processing the package
                while((entry = zis.getNextEntry()) != null) {
                    if(entry.isDirectory()) {
                        __log.debug("Extracting directory: " + entry.getName());
                        new File(dest, entry.getName()).mkdir();
                        continue;
                    }
                    __log.debug("Extracting file: " + entry.getName());
                    File destFile = new File(dest, entry.getName());
                    if (!destFile.getParentFile().exists()) destFile.getParentFile().mkdirs();
                    copyInputStream(zis, new BufferedOutputStream(
                            new FileOutputStream(destFile)));
                }
                zis.close();
            } catch (IOException e) {
                throw new OdeFault("An error occured on deployment.", e);
            }
        }

        private void sendResponse(SOAPFactory factory, MessageContext messageContext, String op,
                                  OMElement response) throws AxisFault {
            MessageContext outMsgContext = Utils.createOutMessageContext(messageContext);
            outMsgContext.getOperationContext().addMessageContext(outMsgContext);

            SOAPEnvelope envelope = factory.getDefaultEnvelope();
            outMsgContext.setEnvelope(envelope);

            OMElement responseOp = factory.createOMElement(op, _pmapi);
            responseOp.addChild(response);
            envelope.getBody().addChild(responseOp);
            AxisEngine.send(outMsgContext);
        }

        private boolean validBundleName(String bundle) {
            boolean valid;
            if (StringUtils.isBlank(bundle)) valid = false;
            else valid = bundle.matches("[\\p{L}0-9_\\-]*");
            if (__log.isDebugEnabled()) {
                __log.debug("Validating bundle " + bundle + " valid: " + valid);
            }
            return valid;
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
