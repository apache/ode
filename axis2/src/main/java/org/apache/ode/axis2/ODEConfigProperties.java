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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.net.URISyntaxException;
import java.net.URL;

public class ODEConfigProperties extends Properties {

    private static final long serialVersionUID = 1L;
    private static final Log __log = LogFactory.getLog(ODEConfigProperties.class);
    private static final Messages __msgs = Messages.getMessages(Messages.class);
    private static final String CONFIG_FILE_NAME = "ode-axis2.properties";
    private static final String PROP_NAMESPACE = "ode-axis2.pidNamespace";
    private static final String PROP_ALLOW_INCOMPLETE_DEPLOYMENT = "ode-axis2.allowIncompleteDeployment";
    private static final String PROP_DB_MODE = "ode-axis2.db.mode";
    private static final String PROP_DB_EXTERNAL_DS = "ode-axis2.db.ext.dataSource";
    private static final String PROP_DB_EMBEDDED_NAME = "ode-axis2.db.emb.name";
    private static final String PROP_POOL_MAX = "ode-axis2.db.pool.max";
    private static final String PROP_POOL_MIN = "ode-axis2.db.pool.min";
    private static final String PROP_CONNECTOR_PORT = "ode-axis2.jca.port";
    private static final String PROP_WORKING_DIR = "ode-axis2.working.dir";

    private File _installDir;

    public ODEConfigProperties(File installRoot) {
        _installDir = new File(installRoot, "conf");
    }

    public void load() throws ServletException {
        boolean found = true;
        File configFile = new File(_installDir, ODEConfigProperties.CONFIG_FILE_NAME);
        if (!configFile.exists()) {
            try {
                URL resource = getClass().getClassLoader().getResource(ODEConfigProperties.CONFIG_FILE_NAME);
                if (resource != null) configFile = new File(resource.toURI());
                else found = false;
            } catch (URISyntaxException e) {
                // Reported below as log msg
            }
        }

        if (found) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(configFile);
                load(fis);
            } catch (Exception ex) {
                String errmsg = ODEConfigProperties.__msgs.msgOdeInstallErrorCfgReadError(configFile);
                ODEConfigProperties.__log.warn(errmsg,ex);
            } finally {
                if (fis != null)
                    try {
                        fis.close();
                    } catch (Exception ex) { ex.printStackTrace(); }
            }
        } else {
            String errmsg = ODEConfigProperties.__msgs.msgOdeInstallErrorCfgNotFound(configFile);
            ODEConfigProperties.__log.warn(errmsg);
        }
    }

    /**
     * Should the internal database be used, or are the datasources provided?
     * @return db mode
     */
    public DatabaseMode getDbMode() {
        return DatabaseMode.valueOf(getProperty(ODEConfigProperties.PROP_DB_MODE, DatabaseMode.EMBEDDED.toString()).trim().toUpperCase());
    }

    public String getDbDataSource() {
        return getProperty(ODEConfigProperties.PROP_DB_EXTERNAL_DS, "java:comp/env/jdbc/ode-ds");
    }

    public String getDbEmbeddedName(){
        return getProperty(ODEConfigProperties.PROP_DB_EMBEDDED_NAME, "data");
    }

    /**
     * Possible database modes.
     */
    public enum DatabaseMode {
        /** External data-source (managed by app server) */
        EXTERNAL,

        /** Internal data-source (managed by us--Minerva) */
        INTERNAL,

        /** Embedded database (managed by us--Minerva) */
        EMBEDDED
    }

    public int getPoolMaxSize() {
        return Integer.valueOf(getProperty(ODEConfigProperties.PROP_POOL_MAX, "10"));
    }

    public int getPoolMinSize() {
        return Integer.valueOf(getProperty(ODEConfigProperties.PROP_POOL_MIN, "1"));
    }

    public int getConnectorPort() {
        return Integer.valueOf(getProperty(ODEConfigProperties.PROP_CONNECTOR_PORT,"2099"));
    }

    public String getWorkingDir() {
        return getProperty(ODEConfigProperties.PROP_WORKING_DIR);
    }

}
