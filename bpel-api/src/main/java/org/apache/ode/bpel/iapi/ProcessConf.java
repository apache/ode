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
package org.apache.ode.bpel.iapi;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.evt.BpelEvent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Deployed process configuration. IMPORTANT: Implementations of this class <em>MUST BE IMMUTABLE</em>,
 * otherwise the engine will get confused.  
 * 
 * @author mriou <mriou at apache dot org>
 */
public interface ProcessConf {

    /**
     * Get the process id, generally the same as the type.
     * @return process id.
     */
    QName getProcessId();
    
    /**
     * Get the process type (BPEL definition name).
     * @return
     */
    QName getType();

    /**
     * Get the process version.
     * @return version
     */
    long getVersion();

    /**
     * Is this a <em>transient</em> process? Transient processes are not persisted in the store.
     * @return <code>true</code> if this is a transient process.
     */
    boolean isTransient();

    /**
     * Indicates whether this process implements REST-style resources.
     */
    boolean isRestful();

    /**
     * Get the CBP stream. 
     * @return new stream to the CBP file.
     */
    InputStream getCBPInputStream();
   
    /**
     * Get the path of the BPEL document, relative to its deployment unit 
     * @return Relative path of BPEL document
     */
    String getBpelDocument();
   
    /**
     * Get the base URL for resolving resources.  
     */
    URI getBaseURI();
    
    /**
     * Get the date of deployment.
     * @return
     */
    Date getDeployDate();

    /**
     * Get the state of the process. 
     * @return process state.
     */
    ProcessState getState();
    
    /**
     * Get the files associated with the deployment.
     * @return
     */
    List<File> getFiles();

    /**
     * Get the process properties. 
     * @return
     */
    Map<QName, Node> getProcessProperties();

    /**
     * Gets the name of the package into which the process is deployed.
     * @return package name
     */
    String getPackage();
    
    /**
     * Gets the WSDL definition used in a process into which a service is defined.
     * @param serviceName
     * @return definition
     */
    Definition getDefinitionForService(QName serviceName);

    /**
     * Gets the list of endpoints a process should provide.
     * @return map of partner link names and associated enpoints
     */
    Map<String, Endpoint> getProvideEndpoints();

    /**
     * Gets the list of endpoints a process invokes.
     * @return map of partner link names and associated enpoints
     */
    Map<String, Endpoint> getInvokeEndpoints();
    
    /**
     * Tells if the service is shareable
     * @param serviceName
     * @return true if the given service can be shared by processes
     */
    boolean isSharedService(QName serviceName);
    
    /**
     * Generic facility to get additional stuff out of the process descriptor. 
     * @param qname name of the extension element.
     * @return list of extension elements 
     */
    List<Element> getExtensionElement(QName qname);

    boolean isEventEnabled(List<String> scopeNames, BpelEvent.TYPE type);

    /**
     * Returns a list of properties associtated to this endpoint.
     * @param epr
     * @return map of property/value pairs
     */
    public Map<String, String> getEndpointProperties(EndpointReference epr);

}
