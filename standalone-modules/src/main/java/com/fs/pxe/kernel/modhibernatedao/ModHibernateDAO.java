/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modhibernatedao;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Environment;

import com.fs.pxe.bpel.dao.BpelDAOConnectionFactory;
import com.fs.pxe.daohib.DataSourceConnectionProvider;
import com.fs.pxe.daohib.SessionManager;
import com.fs.pxe.daohib.bpel.BpelDAOConnectionFactoryImpl;
import com.fs.pxe.daohib.sfwk.DAOConnectionFactoryImpl;
import com.fs.pxe.kernel.PxeKernelModException;
import com.fs.pxe.sfwk.bapi.dao.DAOConnectionFactory;
import com.fs.utils.jmx.SimpleMBean;
import com.fs.utils.msg.MessageBundle;

/**
 * PXE Kernel Mod for starting the Hibernate DAO layer.
 */
public class ModHibernateDAO extends SimpleMBean implements ModHibernateDAOMBean {
  private static final Messages __msgs = MessageBundle.getMessages(Messages.class);
  private static final Log __log = LogFactory.getLog(ModHibernateDAO.class);

  private String _bpelSSCF = null;
  private String _sscf = null;
  private String _dsname;
  private String _transactionManager = "TransactionManager";

  DAOConnectionFactoryImpl _sscfImpl;
  BpelDAOConnectionFactoryImpl _bpelSscfImpl;

  private static final Map<String, ModHibernateDAO> __instances = Collections.synchronizedMap(new HashMap<String,ModHibernateDAO>());

  private String _hibernateProperties = null;
  private String _dialect = null;

  public ModHibernateDAO() throws NotCompliantMBeanException {
    super(ModHibernateDAOMBean.class);
  }

  public String getBpelStateStoreConnectionFactory() {
    return _bpelSSCF;
  }

  public String getStateStoreConnectionFactory() {
    return _sscf;
  }

  public String getHibernateProperties() {
    return _hibernateProperties;
  }

  
  public void setHibernateProperties(String hibernateProperties) {
    _hibernateProperties = hibernateProperties;
  }

  public String getTransactionManager() {
    return _transactionManager;
  }

  public void setBpelStateStoreConnectionFactory(String bpelSscf) {
    _bpelSSCF = bpelSscf;
  }

  public void setStateStoreConnectionFactory(String sscf) {
    _sscf = sscf;
  }

  public void setTransactionManager(String transactionManager) {
    _transactionManager = transactionManager;
  }

  public String getDataSource() {
    return _dsname;
  }

  public void setDataSource(String dsname) {
    _dsname = dsname;
  }

  public String getDialect() {
    return _dialect;
  }

  public void setDialect(String dialect) {
    _dialect = dialect;
  }

  public void start() throws PxeKernelModException {
    Properties properties = new Properties();

    properties.put(Environment.CONNECTION_PROVIDER, DataSourceConnectionProvider.class.getName());
    
    if (_dialect != null)
      properties.put("hibernate.dialect", _dialect);

    if (_hibernateProperties != null) {
      URL hibernatePropertiesURL;
      try {
        hibernatePropertiesURL = new URL(_hibernateProperties);
      } catch (MalformedURLException e) {
        String errmsg = __msgs.msgInvalidHibernatePropertiesURL(_hibernateProperties);
        __log.error(errmsg,e);
        throw new PxeKernelModException(errmsg, e);
      }


      try {
        properties.load(hibernatePropertiesURL.openStream());
      } catch (IOException e) {
        String errmsg = __msgs.msgErrorReadingHibernateProperties(_hibernateProperties);
        __log.error(errmsg, e);
        throw new PxeKernelModException(errmsg, e);
      }
    }


    TransactionManager txManager;
    try {
      InitialContext ctx = new InitialContext();
      txManager = (TransactionManager) ctx.lookup(_transactionManager);
      ctx.close();
    } catch (Exception e) {
      String errmsg = __msgs.msgErrorLookingUpTransactionManager(_transactionManager);
      __log.error(errmsg, e);
      throw new PxeKernelModException(errmsg, e);
    }
    
    DataSource datasource;
    try {
      InitialContext ctx = new InitialContext();
      datasource= (DataSource) ctx.lookup(_dsname);
      ctx.close();
    } catch (Exception e) {
      String errmsg = __msgs.msgErrorLookingUpDataSource(_dsname);
      __log.error(errmsg, e);
      throw new PxeKernelModException(errmsg, e);
    }

    SessionManager sm = null;
    try {
      sm = new SessionManager(properties, datasource, txManager);
    } catch (Exception e) {
      String errmsg = __msgs.msgErrorStartingHibernate();
      __log.error(errmsg ,e);
      throw new PxeKernelModException(errmsg,e);
    }

    _sscfImpl = new DAOConnectionFactoryImpl(sm);
    _bpelSscfImpl = new BpelDAOConnectionFactoryImpl(sm);
    __instances.put(getObjectName().toString(), this);

    Reference sscfRef = new Reference(DAOConnectionFactory.class.getName(),
            HibernateDaoObjectFactory.class.getName(), null);
    sscfRef.add(new StringRefAddr("oname", getObjectName().toString()));

    Reference bpelSscfRef = new Reference(BpelDAOConnectionFactory.class.getName(),
            HibernateDaoObjectFactory.class.getName(), null);
    bpelSscfRef.add(new StringRefAddr("oname", getObjectName().toString()));

    try {
      InitialContext ctx = new InitialContext();
      try {
        if (_bpelSSCF  != null)
          ctx.rebind(_bpelSSCF, bpelSscfRef);

        if (_sscf != null )
          ctx.rebind(_sscf, sscfRef);
      } finally {
        ctx.close();
      }
    } catch (Exception ex) {
      String errmsg = __msgs.msgErrorBindingReferences();
      __log.error(errmsg, ex);
      throw new PxeKernelModException(errmsg,ex);
    }
    __log.info(__msgs.msgStartedHibernate());
  }

  public void stop() throws PxeKernelModException {
  }


  protected ObjectName createObjectName() {
    try {
      return new ObjectName("com.fivesight.pxe:name=ModHibernateDAO");
    } catch (MalformedObjectNameException e) {
      throw new RuntimeException(e);
    }
  }

  static ModHibernateDAO getInstance(Reference ref) {
    String oname = ((StringRefAddr)ref.get("oname")).getContent().toString();
    return __instances.get(oname);
  }


}


