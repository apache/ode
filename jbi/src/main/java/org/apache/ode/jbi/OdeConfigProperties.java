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

package org.apache.ode.jbi;

import java.io.File;
import java.util.Properties;

import org.apache.ode.jbi.msgmap.JbiWsdl11WrapperMapper;

public class OdeConfigProperties extends org.apache.ode.il.config.OdeConfigProperties {
    private static final long serialVersionUID = 1L;

    public static final String CONFIG_FILE_NAME = "ode-jbi.properties";

    private static final String PROP_NAMESPACE = "pidNamespace";

    private static final String PROP_ALLOW_INCOMPLETE_DEPLOYMENT = "allowIncompleteDeployment";
   
    private static final String PROP_MSGMAPPER = "messageMapper";

    public OdeConfigProperties(File cfgFile) {
        super(cfgFile, "ode-jbi.");
    }

    public OdeConfigProperties(Properties properties) {
        super(properties, "ode-jbi.");
    }


    /**
     * Get the namespace that should be used to generate process identifiers
     * (PIDs). The local part of the PID will be the service unit id.
     * 
     * @return
     */
    public String getPidNamespace() {
        return getProperty(PROP_NAMESPACE, null);
    }

    public boolean getAllowIncompleteDeployment() {
        return Boolean.valueOf(getProperty(PROP_ALLOW_INCOMPLETE_DEPLOYMENT, Boolean.FALSE.toString()));
    }

    /**
     * Get the mapper to use for converting message to/from NMS format.
     * 
     * @return
     */
    public String[] getMessageMappers() {
        return getProperty(PROP_MSGMAPPER, JbiWsdl11WrapperMapper.class.getName()).split("[ ,]");
    }

 }
