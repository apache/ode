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

import org.apache.ode.bpel.dd.DeployDocument;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Provides information about a deployment package.
 */
public interface DeploymentUnit {

    /**
     * Get the deployment directory location.
     * 
     * @return deployment directory location
     */
    File getDeployDir();

    /**
     * Get the deployment descriptor for this deployment unit.
     * 
     * @return parsed deployment descriptor
     */
    DeployDocument getDeploymentDescriptor();

    /**
     * Get a WSDL definition from the deployment directory for the given
     * namespace.
     * 
     * @param name
     *            WSDL targetNamespace
     * @return corresponding WSDL document
     */
    Definition getDefinitionForService(QName name);

    /**
     * Get a collection of all the WSDL definitions in the deployment directory.
     * 
     * @return collection of WSDL documents
     */
    Collection<Definition> getDefinitions();

    /**
     * Names of the processes included in the deployment unit.
     * 
     * @return
     */
    Set<QName> getProcessNames();

    List<File> allFiles();

}
