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

package org.apache.ode.il.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Configuration object used for configuring the intergration layer. The propereties are those likely to be common to all layers.
 * 
 * @author mszefler
 */
public class OdeConfigProperties {

    private static final long serialVersionUID = 1L;

    private static final Log __log = LogFactory.getLog(OdeConfigProperties.class);

    private static final String PROP_DB_MODE = "db.mode";

    private static final String PROP_DB_EXTERNAL_DS = "db.ext.dataSource";

    private static final String PROP_DB_EMBEDDED_NAME = "db.emb.name";

    private static final String PROP_DB_INTERNAL_URL = "db.int.jdbcurl";

    private static final String PROP_DB_INTERNAL_DRIVER = "db.int.driver";

    private static final String PROP_DB_LOGGING = "db.logging";

    private static final String PROP_DB_DAO = "db.dao";

    private static final String PROP_TX_FACTORY_CLASS = "tx.factory.class";

    private static final String PROP_POOL_MAX = "db.pool.max";

    private static final String PROP_POOL_MIN = "db.pool.min";

    private static final String PROP_CONNECTOR_PORT = "jca.port";

    private static final String PROP_CONNECTOR_NAME = "jca.name";

    private static final String PROP_WORKING_DIR = "working.dir";

    private static final String PROP_REPLICATE_EMPTYNS = "message.replicate.emptyns";

    private static final String PROP_EVENT_LISTENERS = "event.listeners";

    private static final String PROP_PROCESS_DEHYDRATION = "process.dehydration";

    private File _cfgFile;

    private String _prefix;

    private Properties _props;

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

    public enum DaoType {
        JPA, HIBERNATE
    }

    public OdeConfigProperties(File cfgFile, String prefix) {
        _cfgFile = cfgFile;
        _prefix = prefix;
        _props = new Properties();
    }

    public File getFile() {
        return _cfgFile;
    }
    
    public void load() throws IOException {
        if (_cfgFile.exists()) {
            __log.debug("config file exists: " + _cfgFile);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(_cfgFile);
                _props.load(fis);
            } finally {
                if (fis != null)
                    try {
                        fis.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
            }
        } else {
            __log.debug("config file does not exists: " + _cfgFile);
            throw new FileNotFoundException("" + _cfgFile);
        }
    }

    /**
     * Should the internal database be used, or are the datasources provided?
     * 
     * @return db mode
     */
    public DatabaseMode getDbMode() {
        return DatabaseMode.valueOf(getProperty(OdeConfigProperties.PROP_DB_MODE, DatabaseMode.EMBEDDED.toString()).trim()
                .toUpperCase());
    }

    public String getDbDataSource() {
        return getProperty(OdeConfigProperties.PROP_DB_EXTERNAL_DS, "java:comp/env/jdbc/ode-ds");
    }

    public String getDbIntenralJdbcUrl() {
        return getProperty(OdeConfigProperties.PROP_DB_INTERNAL_URL, "jdbc:derby://localhost/ode");
    }

    /**
     * JDBC driver class (for use in INTERNAL mode).
     * 
     * @return
     */
    public String getDbInternalJdbcDriverClass() {
        return getProperty(OdeConfigProperties.PROP_DB_INTERNAL_DRIVER, "org.apache.derby.jdbc.ClientDriver");
    }

    public int getPoolMaxSize() {
        return Integer.valueOf(getProperty(OdeConfigProperties.PROP_POOL_MAX, "10"));
    }

    public int getPoolMinSize() {
        return Integer.valueOf(getProperty(OdeConfigProperties.PROP_POOL_MIN, "1"));
    }

    public int getConnectorPort() {
        return Integer.valueOf(getProperty(OdeConfigProperties.PROP_CONNECTOR_PORT, "2099"));
    }
    
    public String getConnectorName() {
        return getProperty(OdeConfigProperties.PROP_CONNECTOR_NAME, "ode");
    }

    public String getWorkingDir() {
        return getProperty(OdeConfigProperties.PROP_WORKING_DIR);
    }

    public String getTxFactoryClass() {
        return getProperty(OdeConfigProperties.PROP_TX_FACTORY_CLASS, "org.apache.ode.axis2.util.JotmFactory");
    }

    public boolean isReplicateEmptyNS() {
        return Boolean.valueOf(getProperty(OdeConfigProperties.PROP_REPLICATE_EMPTYNS, "true"));
    }

    public String getEventListeners() {
        return getProperty(PROP_EVENT_LISTENERS);
    }

    public boolean isDehydrationEnabled() {
        return Boolean.valueOf(getProperty(OdeConfigProperties.PROP_PROCESS_DEHYDRATION, "false"));
    }

    public boolean isDbLoggingEnabled() {
        return Boolean.valueOf(getProperty(OdeConfigProperties.PROP_DB_LOGGING, "false"));
    }

    public DaoType getDbDaoImpl() {
        try {
            String persistenceType = System.getProperty("ode.persistence");
            if (persistenceType != null) {
                return DaoType.valueOf(persistenceType.toUpperCase());
            }
        } catch (Throwable t) {
            __log.debug("error reading system property override for DAO type.", t);
            // fall through, and use the default mechanism
        }
       
        return DaoType.valueOf(_props.getProperty(PROP_DB_DAO, DaoType.JPA.toString()));
    }

    protected String getProperty(String pname) {
        return _props.getProperty(_prefix + pname);
    }

    protected String getProperty(String key, String dflt) {
        return _props.getProperty(_prefix + key, dflt);
    }

}
