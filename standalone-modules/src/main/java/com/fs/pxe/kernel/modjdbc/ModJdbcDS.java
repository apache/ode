/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modjdbc;

import com.fs.pxe.kernel.PxeKernelModException;
import com.fs.utils.jmx.SimpleMBean;
import com.fs.utils.msg.MessageBundle;

import java.sql.Driver;
import java.sql.DriverManager;

import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.Reference;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentools.minerva.MinervaPool;
import org.opentools.minerva.MinervaPool.PoolType;

public class ModJdbcDS extends SimpleMBean implements ModJdbcDSMBean {
  private static final Log __log = LogFactory.getLog(ModJdbcDS.class);
  private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

  private String _transactionManagerName = "TransactionManager";
  private String _dataSourceName = null;
  private String _driver = "ClassNameNotSet";
  private MinervaPool _pool = new MinervaPool();

  public ModJdbcDS() throws NotCompliantMBeanException {
    super(ModJdbcDSMBean.class);
  }

  public String getDataSourceName() {
    return _dataSourceName;
  }

  public void setDataSourceName(String dataSourceName) {
    _dataSourceName = dataSourceName;
  }

  public String getDriver() {
    return _driver;
  }

  public void setDriver(String driver) {
    _driver = driver;
  }

  public String getPassword() {
    return _pool.getConnectionFactory().getPassword();
  }

  public void setPassword(String password) {
    _pool.getConnectionFactory().setPassword(password);
  }

  public int getPoolMax() {
    return _pool.getPoolParams().maxSize;
  }

  public void setPoolMax(int poolMax) {
    _pool.getPoolParams().maxSize = poolMax;
  }

  public int getPoolMin() {
    return _pool.getPoolParams().minSize;
  }

  public void setPoolMin(int poolMin) {
    _pool.getPoolParams().minSize = poolMin;
  }

  public String getTransactionManagerName() {
    return _transactionManagerName;
  }

  public void setTransactionManagerName(String transactionManagerName) {
    _transactionManagerName = transactionManagerName;
  }

  public String getUrl() {
    return _pool.getConnectionFactory().getConnectionURL();
  }

  public void setUrl(String url) {
    _pool.getConnectionFactory().setConnectionURL(url);
  }

  public String getUsername() {
    return _pool.getConnectionFactory().getUserName();
  }

  public void setUsername(String username) {
    _pool.getConnectionFactory().setUserName(username);
  }

  public void start() throws PxeKernelModException {
    
    // Register the driver.
    try {
      Driver driver = (Driver) Class.forName(_driver).newInstance();
      DriverManager.registerDriver(driver);
    } catch (Exception ex) {
      String errmsg = __msgs.msgErrorRegisteringJdbcDriver(_driver);
      __log.error(errmsg, ex);
      throw new PxeKernelModException(errmsg,ex);
    }
    

    TransactionManager tm;
    // Find the transaction manager
    try {
      InitialContext ctx = new InitialContext();
      tm = (TransactionManager) ctx.lookup(_transactionManagerName);
      ctx.close();
    } catch (Exception ex) {
      String errmsg = __msgs.msgErrorTxManagerNotFound(_transactionManagerName);
      __log.error(errmsg,ex);
      throw new PxeKernelModException(errmsg,ex);
    }
    
    _pool.setTransactionManager(tm);
    try {
      _pool.start();
    } catch (Exception ex) {
      String errmsg = __msgs.msgErrorStartingPool();
      __log.error(errmsg,ex);
      throw new PxeKernelModException(errmsg,ex);
    }
    
    Reference dsRef = _pool.createDataSourceReference();
    if (_dataSourceName != null)
      try {
        // bind datasource and transaction manager
        InitialContext ctx = new InitialContext();
        ctx.rebind(_dataSourceName, dsRef);
        ctx.close();
      } catch (Exception ex) {
        final String errmsg = __msgs.msgErrorBindingDataSource(_dataSourceName);
        __log.error(errmsg,ex);
        throw new PxeKernelModException(errmsg, ex);
      }

    __log.info(__msgs.msgStartedJDBCPool(_driver, getUrl(),  _dataSourceName));

  }

  public void stop() throws PxeKernelModException {
    if (_dataSourceName != null)
      try {
        InitialContext ctx = new InitialContext();
        ctx.unbind(_dataSourceName);
        ctx.close();
      } catch (Exception ex) {
        __log.warn(__msgs.msgErrorUnbindingDataSource(_dataSourceName),ex);
      }
      
    _pool.stop();
  }

  protected ObjectName createObjectName() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public String getType() {
    return _pool.getType().toString();
  }

  public void setType(String type) {
    _pool.setType(PoolType.valueOf(type));
    
  }
}
