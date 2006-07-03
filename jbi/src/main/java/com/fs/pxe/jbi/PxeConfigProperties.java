package com.fs.pxe.jbi;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.jbi.JBIException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fs.pxe.jbi.msgmap.JbiWsdl11WrapperMapper;

public class PxeConfigProperties extends Properties {
  private static final long serialVersionUID = 1L;
  private static final Log __log = LogFactory.getLog(PxeConfigProperties.class);
  private static final Messages __msgs = Messages.getMessages(Messages.class);
  private static final String CONFIG_FILE_NAME = "pxe-jbi.properties";
  private static final String PROP_NAMESPACE = "pxe-jbi.pidNamespace";
  private static final String PROP_ALLOW_INCOMPLETE_DEPLOYMENT = "pxe-jbi.allowIncompleteDeployment";
  private static final String PROP_DB_MODE = "pxe-jbi.db.mode";
  private static final String PROP_DB_EXTERNAL_DS = "pxe-jbi.db.ext.dataSource";
  private static final String PROP_DB_EMBEDDED_NAME = "pxe-jbi.db.emb.name";
  private static final String PROP_POOL_MAX = "pxe-jbi.db.pool.max";
  private static final String PROP_POOL_MIN = "pxe-jbi.db.pool.min";
  private static final String PROP_MSGMAPPER = "pxe-jbi.messageMapper";
  
  private String _installDir;
  
  public PxeConfigProperties(String installRoot) {
    _installDir = installRoot;
  }

  public void load() throws JBIException {
    File configFile = new File(_installDir ,CONFIG_FILE_NAME);
    if (!configFile.exists()) {
      String errmsg = __msgs.msgPxeInstallErrorCfgNotFound(configFile);
      __log.error(errmsg);
      throw new JBIException(errmsg);
    }
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(configFile);
      load(fis);
    } catch (Exception ex) {
      String errmsg = __msgs.msgPxeInstallErrorCfgReadError(configFile);
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
    return getProperty(PROP_DB_EXTERNAL_DS, "java:comp/jdbc/pxe-ds");
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

}
