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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.SystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration object used for configuring the intergration layer. The propereties are those likely to be common to all layers.
 *
 * @author mszefler
 */
public class OdeConfigProperties {

    private static final long serialVersionUID = 1L;

    private static final Log __log = LogFactory.getLog(OdeConfigProperties.class);

    public static final String PROP_DB_MODE = "db.mode";

    public static final String PROP_DB_EXTERNAL_DS = "db.ext.dataSource";

    public static final String PROP_DB_EMBEDDED_NAME = "db.emb.name";

    public static final String PROP_DB_EMBEDDED_CREATE = "db.emb.create";

    public static final String PROP_DB_INTERNAL_URL = "db.int.jdbcurl";

    public static final String PROP_DB_INTERNAL_DRIVER = "db.int.driver";

    public static final String PROP_DB_INTERNAL_PASSWORD = "db.int.password";

    public static final String PROP_DB_INTERNAL_USER = "db.int.username";

    public static final String PROP_DB_LOGGING = "db.logging";

    public static final String PROP_TX_FACTORY_CLASS = "tx.factory.class";

    public static final String PROP_POOL_MAX = "db.pool.max";

    public static final String PROP_POOL_MIN = "db.pool.min";

    public static final String PROP_DB_POOL_BLOCKING = "db.pool.blocking";

    public static final String PROP_THREAD_POOL_SIZE = "threads.pool.size";

    public static final String PROP_CONNECTOR_PORT = "jca.port";

    public static final String PROP_CONNECTOR_NAME = "jca.name";

    public static final String PROP_WORKING_DIR = "working.dir";

    public static final String PROP_EVENT_LISTENERS = "event.listeners";

    public static final String PROP_MEX_INTERCEPTORS = "mex.interceptors";

    public static final String PROP_PROCESS_DEHYDRATION = "process.dehydration";

    public static final String PROP_DAOCF = "dao.factory";
    
    public static final String PROP_EXTENSION_BUNDLES_RT = "extension.bundles.runtime";
    public static final String PROP_EXTENSION_BUNDLES_VAL = "extension.bundles.validation";

    private File _cfgFile;

    private String _prefix;

    private Properties _props;

    /** Default defaults for the database embedded name and dao connection factory class. */
    public static String DEFAULT_DB_EMB_NAME = "jpadb";
    private static String __daoCfClass = "org.apache.ode.dao.jpa.BPELDAOConnectionFactoryImpl";

    static {
        String odep = System.getProperty("ode.persistence");
        if (odep != null &&
                "hibernate".equalsIgnoreCase(odep)) {
            __log.debug("Using HIBERNATE due to system property override!");
            DEFAULT_DB_EMB_NAME = "hibdb";
            __daoCfClass = "org.apache.ode.daohib.bpel.BpelDAOConnectionFactoryImpl";

        }
    }
    /**
     * Possible database modes.
     */
    public enum DatabaseMode {
        /** External data-source (managed by app server) */
        EXTERNAL,

        /** Internal data-source (User provides database info, Ode provides connection pool) */
        INTERNAL,

        /** Embedded database (Ode provides default embedded database with connection pool) */
        EMBEDDED
    }

    public OdeConfigProperties(File cfgFile, String prefix) {
        _cfgFile = cfgFile;
        _prefix = prefix;
        _props = new Properties();
    }

    public OdeConfigProperties(Properties props, String prefix) {
        _cfgFile = null;
        _prefix = prefix;
        _props = props;
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
        for (Object key : _props.keySet()) {
        	String value = (String) _props.get(key);
        	value = SystemUtils.replaceSystemProperties(value);
        	_props.put(key, value);
        }
    }

    /**
     * Should the internal database be used, or are the datasources provided?
     *
     * @return db mode
     */
    public String getDbEmbeddedName() {
        return getProperty(OdeConfigProperties.PROP_DB_EMBEDDED_NAME, DEFAULT_DB_EMB_NAME);
    }

    public boolean isDbEmbeddedCreate() {
        return Boolean.valueOf(getProperty(OdeConfigProperties.PROP_DB_EMBEDDED_CREATE, "false"));
    }

    public DatabaseMode getDbMode() {
        return DatabaseMode.valueOf(getProperty(OdeConfigProperties.PROP_DB_MODE, DatabaseMode.EMBEDDED.toString()).trim()
                .toUpperCase());
    }

    public String getDAOConnectionFactory() {
        return getProperty(PROP_DAOCF, __daoCfClass);
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

    public boolean getPoolBlocking() {
        return Boolean.valueOf(getProperty(PROP_DB_POOL_BLOCKING,"false"));
    }

    public int getThreadPoolMaxSize() {
        return Integer.valueOf(getProperty(OdeConfigProperties.PROP_THREAD_POOL_SIZE, "0"));
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
        return getProperty(OdeConfigProperties.PROP_TX_FACTORY_CLASS, "org.apache.ode.il.EmbeddedGeronimoFactory");
    }

    public String getEventListeners() {
        return getProperty(PROP_EVENT_LISTENERS);
    }

    public String getMessageExchangeInterceptors() {
        return getProperty(PROP_MEX_INTERCEPTORS);
    }

    public boolean isDehydrationEnabled() {
        return Boolean.valueOf(getProperty(OdeConfigProperties.PROP_PROCESS_DEHYDRATION, "false"));
    }

    public boolean isDbLoggingEnabled() {
        return Boolean.valueOf(getProperty(OdeConfigProperties.PROP_DB_LOGGING, "false"));
    }


    protected String getProperty(String pname) {
        return _props.getProperty(_prefix + pname);
    }

    protected String getProperty(String key, String dflt) {
        return _props.getProperty(_prefix + key, dflt);
    }

    public Properties getProperties() {
        return _props;
    }

    public String getDbInternalUserName() {
        return getProperty(PROP_DB_INTERNAL_USER);
    }

    public String getDbInternalPassword() {
        return getProperty(PROP_DB_INTERNAL_PASSWORD);
    }

    public String getExtensionActivityBundlesRT() {
    	return getProperty(PROP_EXTENSION_BUNDLES_RT);
    }

    public String getExtensionActivityBundlesValidation() {
    	return getProperty(PROP_EXTENSION_BUNDLES_VAL);
    }

}
