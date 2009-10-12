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

package org.apache.ode.axis2.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.deployment.ServiceBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.PolicyInclude;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.Policy;
import org.apache.rampart.RampartMessageData;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.net.URL;
import java.net.URI;

/**
 *
 */
public class AxisUtils {

    private static final Log log = LogFactory.getLog(AxisUtils.class);

    public static void configureService(AxisService axisService, URL service_file) throws IOException, XMLStreamException, AxisFault {
        configureService(new ConfigurationContext(axisService.getAxisConfiguration()), axisService, service_file);
    }

    /**
     * Configure a service instance woth the specified service.xml document.
     * If modules are mentioned in the document, <code>this</code> method will make sure they are properly engaged and engage them if necessary.
     * The modules have to be available in the module repository otherwise an AxisFault will be thrown.
     *
     * @param axisService  the service to configure
     * @param service_file the service.xm document to configure the service with
     * @throws IOException
     * @throws XMLStreamException
     * @throws org.apache.axis2.AxisFault if a module listed in the service.xml is not available in the module repository
     */
    public static void configureService(ConfigurationContext configCtx, AxisService axisService, URL service_file) throws IOException, XMLStreamException, AxisFault {
        InputStream ais = service_file.openStream();
        log.debug("Looking for Axis2 service configuration file: " + service_file);
        if (ais != null) {
            log.debug("Configuring service " + axisService.getName() + " using: " + service_file);
            try {
                if (configCtx == null)
                    configCtx = new ConfigurationContext(axisService.getAxisConfiguration());
                ServiceBuilder builder = new ServiceBuilder(ais, configCtx, axisService);
                builder.populateService(builder.buildOM());
            } finally {
                ais.close();
            }
            // the service builder only updates the module list but do not engage them
            // modules have to be engaged manually,
            for (int i = 0; i < axisService.getModules().size(); i++) {
                String moduleRef = (String) axisService.getModules().get(i);
                AxisModule module = axisService.getAxisConfiguration().getModule(moduleRef);
                if (module != null) {
                    axisService.engageModule(module);
                } else {
                    throw new AxisFault("Unable to engage module: " + moduleRef);
                }
            }
        }
    }

    public static void applySecurityPolicy(AxisService service, String policy_file) throws IllegalArgumentException {
        URI policyUri = new File(policy_file).toURI();
        if (log.isDebugEnabled()) log.debug("Applying security policy: " + policyUri);
        try {
            InputStream policyStream = policyUri.toURL().openStream();
            try {
                Policy policyDoc = PolicyEngine.getPolicy(policyStream);
                // Neethi parser is really dumb.
                // In case of parsing error, the exception is printed out and swallowed. Null is returned.
                if(policyDoc == null){
                    String msg = "Failed to parse policy: "+policy_file+". Due to Neethi limitations the reason can't be provided. See stacktraces in standard output (not logs)";
                    log.error(msg);
                    throw new IllegalArgumentException(msg);
                }
                service.getPolicyInclude().addPolicyElement(PolicyInclude.AXIS_SERVICE_POLICY, policyDoc);
                // make sure the proper modules are engaged, if they are available
                engageModules(service, "rampart", "rahas");
            } finally {
                policyStream.close();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Exception while parsing policy: " + policyUri, e);
        }
    }

    public static void engageModules(AxisDescription description, String... modules) throws AxisFault {
        for (String m : modules) {
            if (description.getAxisConfiguration().getModule(m) != null) {
                if (!description.getAxisConfiguration().isEngaged(m) && !description.isEngaged(m)) {
                    description.engageModule(description.getAxisConfiguration().getModule(m));
                }
            } else {
                if (log.isDebugEnabled()) log.debug("Module " + m + " is not available.");
            }
        }
    }
}
