package com.fs.pxe.axis2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class PXEConfigProperties extends Properties {

  private static final long serialVersionUID = 1L;
  private static final Log __log = LogFactory.getLog(PXEConfigProperties.class);
  private static final Messages __msgs = Messages.getMessages(Messages.class);
  private static final String CONFIG_FILE_NAME = "pxe-axis2.properties";
  private static final String PROP_NAMESPACE = "pxe-axis2.pidNamespace";
  private static final String PROP_ALLOW_INCOMPLETE_DEPLOYMENT = "pxe-axis2.allowIncompleteDeployment";
  private static final String PROP_DB_MODE = "pxe-axis2.db.mode";
  private static final String PROP_DB_EXTERNAL_DS = "pxe-axis2.db.ext.dataSource";
  private static final String PROP_DB_EMBEDDED_NAME = "pxe-axis2.db.emb.name";
  private static final String PROP_POOL_MAX = "pxe-axis2.db.pool.max";
  private static final String PROP_POOL_MIN = "pxe-axis2.db.pool.min";

  private File _installDir;

  public PXEConfigProperties(File installRoot) {
    _installDir = new File(installRoot, "conf");
  }

  public void load() throws ServletException {
    File configFile = new File(_installDir, PXEConfigProperties.CONFIG_FILE_NAME);
    if (!configFile.exists()) {
      String errmsg = PXEConfigProperties.__msgs.msgPxeInstallErrorCfgNotFound(configFile);
      PXEConfigProperties.__log.warn(errmsg);
    } else {
      FileInputStream fis = null;
      try {
        fis = new FileInputStream(configFile);
        load(fis);
      } catch (Exception ex) {
        String errmsg = PXEConfigProperties.__msgs.msgPxeInstallErrorCfgReadError(configFile);
        PXEConfigProperties.__log.warn(errmsg,ex);
      } finally {
        if (fis != null)
          try {
            fis.close();
          } catch (Exception ex) { ex.printStackTrace(); }
      }
    }
  }

  /**
   * Should the internal database be used, or are the datasources provided?
   * @return db mode
   */
  public DatabaseMode getDbMode() {
    return DatabaseMode.valueOf(getProperty(PXEConfigProperties.PROP_DB_MODE, DatabaseMode.EMBEDDED.toString()).trim().toUpperCase());
  }

  public String getDbDataSource() {
    return getProperty(PXEConfigProperties.PROP_DB_EXTERNAL_DS, "java:comp/jdbc/pxe-ds");
  }

  public String getDbEmbeddedName(){
    return getProperty(PXEConfigProperties.PROP_DB_EMBEDDED_NAME, "data");
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
    return Integer.valueOf(getProperty(PXEConfigProperties.PROP_POOL_MAX, "10"));
  }

  public int getPoolMinSize() {
    return Integer.valueOf(getProperty(PXEConfigProperties.PROP_POOL_MIN, "1"));
  }

}
