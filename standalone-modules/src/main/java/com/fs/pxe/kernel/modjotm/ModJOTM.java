/*
 * File:      $Id: ModJOTM.java 1467 2006-06-12 04:27:49Z mbs $
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modjotm;

import com.fs.utils.jmx.SimpleMBean;
import com.fs.utils.msg.MessageBundle;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.objectweb.jotm.Jotm;

/**
 * JONAS Transaction Manager module for the PXE kernel.
 */
public class ModJOTM extends SimpleMBean implements ModJOTMMBean {
  private static final Log __log = LogFactory.getLog(ModJOTM.class);
  private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

  private static final Map<String, Jotm> __jotmMap = new ConcurrentHashMap<String,Jotm>();

  private Jotm _jotm;

  private int _timeout = 30;
  private String _tmJndiName = "TransactionManager";
  private String _utJndiName = "UserTransaction";
  
  public ModJOTM() throws NotCompliantMBeanException {
    super(ModJOTMMBean.class);
  }

  public int getTxTimeout() {
    return _timeout;
  }

  public String getTransactionManagerName() {
    return _tmJndiName;
  }

  public void setTransactionManagerName(String txManagerName) {
    _tmJndiName = txManagerName;
  }
  
  public String getUserTransactionManagerName() {
    return _utJndiName;
  }

  public void setUserTransactionManagerName(String utxname) {
    _utJndiName = utxname;
  }

  public void setTxTimeout(int txTimeout) {
    _timeout = txTimeout;
  }

  protected ObjectName createObjectName() {
    return null;
  }

  public void start() {
    // JOTM transaction manager
    try {
      Properties props = System.getProperties();
      _jotm = new Jotm(true, false);
      __jotmMap.put(getObjectName().toString(), _jotm);
      _jotm.getTransactionManager().setTransactionTimeout(_timeout);
      System.setProperties(props);

      Reference txm = new Reference("javax.transaction.TransactionManager",
              JotmTransactionManagerFactory.class.getName(), null);
      txm.add(new StringRefAddr("oname", getObjectName().toString()));
      Reference utxm = new Reference("javax.transaction.UserTransaction",
          JotmTransactionManagerFactory.class.getName(), null);
      utxm.add(new StringRefAddr("oname", getObjectName().toString()));

      InitialContext ctx = new InitialContext();
      ctx.rebind(_tmJndiName, txm);
      ctx.rebind(_utJndiName, utxm);
      
      ctx.close();
      __log.info(__msgs.msgStartedJOTM(_tmJndiName));
    } catch (Exception ex) {
      __log.error(__msgs.msgErrorStartingJOTM(), ex);
    }
  }

  public void stop() {
    __jotmMap.remove(getObjectName());
    _jotm.stop();
    _jotm = null;
  }

  public static TransactionManager getTransactionManager(Reference ref) {
    String oname = (String)ref.get("oname").getContent();
    return __jotmMap.get(oname).getTransactionManager();
  }

  public static UserTransaction getUserTransaction(Reference ref) {
    String oname = (String)ref.get("oname").getContent();
    return __jotmMap.get(oname).getUserTransaction();
  }
}
