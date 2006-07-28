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
import java.io.FileInputStream;
import java.util.Properties;

import javax.jbi.JBIException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.ode.jbi.msgmap.JbiWsdl11WrapperMapper;

public class OdeConfigProperties extends Properties {
  private static final long serialVersionUID = 1L;
  private static final Log __log = LogFactory.getLog(OdeConfigProperties.class);
  private static final Messages __msgs = Messages.getMessages(Messages.class);
  private static final String CONFIG_FILE_NAME = "ode-jbi.properties";
  private static final String PROP_NAMESPACE = "ode-jbi.pidNamespace";
  private static final String PROP_ALLOW_INCOMPLETE_DEPLOYMENT = "ode-jbi.allowIncompleteDeployment";
  private static final String PROP_DB_MODE = "ode-jbi.db.mode";
  private static final String PROP_DB_EXTERNAL_DS = "ode-jbi.db.ext.dataSource";
  private static final String PROP_DB_EMBEDDED_NAME = "ode-jbi.db.emb.name";
  private static final String PROP_POOL_MAX = "ode-jbi.db.pool.max";
  private static final String PROP_POOL_MIN = "ode-jbi.db.pool.min";
  private static final String PROP_MSGMAPPER = "ode-jbi.messageMapper";
  private static final String PROP_CONNECTOR_PORT = "ode-jbi.connector.registryPort";
  private static final String PROP_CONNECTOR_NAME = "ode-jbi.connector.registryName";
  
  
  private String _installDir;
  
  public OdeConfigProperties(String installRoot) {
    _installDir = installRoot;
  }

  public void load() throws JBIException {
    File configFile = new File(_installDir ,CONFIG_FILE_NAME);
    if (!configFile.exists()) {
      String errmsg = __msgs.msgOdeInstallErrorCfgNotFound(configFile);
      __log.error(errmsg);
      throw new JBIException(errmsg);
    }
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(configFile);
      load(fis);
    } catch (Exception ex) {
      String errmsg = __msgs.msgOdeInstallErrorCfgReadError(configFile);
      __log.error(errmsg,ex);
      throw new JBIException(errmsg);
    } finally {
      if (fis != null) 
        try {
          fis.close();
        } catch (Exception ex) {} 
    }
    
  }
  
  /**
   * Get the namespace that should be used to generate process identifiers
   * (PIDs). The local part of the PID will be the service unit id.
   * @return
   */
  public String getPidNamespace() {
    return getProperty(PROP_NAMESPACE, null);
  }
  
  /**
   * Should the internal database be used, or are the datasources provided?
   * @return 
   */
  public DatabaseMode getDbMode() {
    return DatabaseMode.valueOf(getProperty(PROP_DB_MODE, DatabaseMode.EMBEDDED.toString()).toUpperCase());
  }
  
  public String getDbDataSource() {
    return getProperty(PROP_DB_EXTERNAL_DS, "java:comp/jdbc/ode-ds");
  }
  
  public String getDbEmbeddedName(){
    return getProperty(PROP_DB_EMBEDDED_NAME, "data");
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

  public boolean getAllowIncompleteDeployment() {
    return Boolean.valueOf(getProperty(PROP_ALLOW_INCOMPLETE_DEPLOYMENT,Boolean.FALSE.toString()));
  }

  /**
   * Get the mapper to use for converting message to/from NMS format.
   * @return
   */
  public String getMessageMapper() {
    return getProperty(PROP_MSGMAPPER, JbiWsdl11WrapperMapper.class.getName());
  }
  public int getPoolMaxSize() {
    return Integer.valueOf(getProperty(PROP_POOL_MAX, "10"));
  }
  
  public int getPoolMinSize() {
    return Integer.valueOf(getProperty(PROP_POOL_MIN, "1"));
  }

  public int getConnectorPort() {
    return Integer.valueOf(getProperty(PROP_CONNECTOR_PORT, "1099"));
  }

  public String getConnectorName() {
    return getProperty(PROP_CONNECTOR_NAME,"ode");
  }
}
