/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.mngmt;

import com.fs.pxe.sfwk.mngmt.DomainAdminMBean;
import com.fs.pxe.sfwk.mngmt.ServiceAdminMBean;
import com.fs.pxe.sfwk.mngmt.SystemAdminMBean;
import com.fs.pxe.tools.Command;
import com.fs.pxe.tools.ExecutionException;
import com.fs.utils.jmx.JMXConstants;
import com.fs.utils.msg.MessageBundle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class JmxCommand implements Command {

  /** System property containing the default JMX URL. */
  private static final String PROP_JMXURL = "pxe.jmxurl";
  
  /** System property containg the JMX username. */
  private static final String PROP_JMXUSERNAME = "pxe.jmxusername";
  
  /** System property containing the JMX password. */
  private static final String PROP_JMXPASSWORD = "pxe.jmxpassword";

  private String _domainUuid;
  private String _systemName;
  private String _jmxUrl;
  private String _jmxUsername;
  private String _jmxPassword;

  private MBeanServerConnection _connection;
  
  private static final Log __log = LogFactory.getLog(JmxCommand.class);
  
  protected static final MngmtMessages __msgs = MessageBundle.getMessages(MngmtMessages.class);
  
  private DomainAdminMBean _domain;
  
  private SystemAdminMBean _system;

  
  public JmxCommand() {
    _jmxUrl = System.getProperty(PROP_JMXURL);
    if(null == _jmxUrl) 
      __log.warn(__msgs.msgPxeJmxUrlNotSet());

    _jmxUsername = System.getProperty(PROP_JMXUSERNAME);
    _jmxPassword = System.getProperty(PROP_JMXPASSWORD);
  }
  
  public String getDomainUuid() {
    return _domainUuid;
  }

  public void setDomainUuid(String domainUuid) {
    _domainUuid = domainUuid;
  }
  
  public String getSystemName() {
    return _systemName;
  }
  
  public void setSystemName(String systemName) {
    _systemName = systemName;
  }
  
  public String getJmxUrl() {
    if (_jmxUrl == null) {
      setJmxUrl("pxe:default");
    }
    return _jmxUrl;
  }
  
  public String getJmxUsername() {
    return _jmxUsername;
  }
  
  public String getJmxPassword() {
    return _jmxPassword;
  }

  public void setJmxUrl(String jmxUrl) {
    _jmxUrl = jmxUrl;
  }

  public void setJmxUsername(String jmxUsername) {
    _jmxUsername = jmxUsername;
  }

  public void setJmxPassword(String jmxPassword) {
    _jmxPassword = jmxPassword;
  }

  protected MBeanServerConnection getConnection() throws ExecutionException {
    if (!isConnected()) {
      connect();
    }
    return _connection;
  }
  
  protected void connect() throws ExecutionException {
    JMXServiceURL surl = null;
    if (_jmxUrl == null) {
      throw new ExecutionException(__msgs.msgJmxUrlMustBeSet());
    }

    try {
      surl = new JMXServiceURL(_jmxUrl);
    } catch (IOException ioe) {
      throw new ExecutionException(ioe);
    }
    
    JMXConnector jmxc = null;

		Map<String,Object> environment = new HashMap<String,Object>();
		if(getJmxUsername() != null && getJmxUsername().trim().length()>0) {
      // using JMXConnector.CREDENTIALS
      String[] credentials = new String[]
					{ getJmxUsername().trim() , getJmxPassword() };
			environment.put(JMXConnector.CREDENTIALS, credentials);
		}
      
    try {
      jmxc = JMXConnectorFactory.connect(surl, environment);
    } catch (Exception e) {
      throw new ExecutionException(
          "Unable to connect using service URL " + surl + " username=" + getJmxUsername() + ": " + e.getMessage(),
          e);
    }

    try {
      _connection = jmxc.getMBeanServerConnection();
    } catch (IOException ioe) {
      throw new ExecutionException(ioe);
    }
  }
  
  protected boolean isConnected() {
    return _connection != null;
  }
  
  protected DomainAdminMBean getDomain() throws ExecutionException {
    if (_domain != null) {
      return _domain;
    }
    if (!isConnected()) {
      connect();
    }
    ObjectName dn = null;
    if (getDomainUuid() != null) {
      try {
        dn = JMXConstants.createDomainObjectName(getDomainUuid());
      } catch (MalformedObjectNameException re) {
        throw new ExecutionException(re);
      }
      try {
        if (!_connection.isRegistered(dn)) {
          throw new ExecutionException(
            __msgs.msgNoSuchDomain(getDomainUuid()));
        }
      } catch (IOException ioe) {
        throw new ExecutionException(ioe.getMessage(),ioe);
      }      
    } else {
      ObjectName on;
      try {
        on = JMXConstants.createDomainObjectQuery();
      } catch (MalformedObjectNameException mone) {
        // TODO: Better exception.
        throw new ExecutionException(mone);
      }
      Set s;
      try {
        s = _connection.queryMBeans(on,null);
      } catch (IOException ioe) {
        throw new ExecutionException("Unable to query names: " + ioe.getMessage(),ioe);
      }
      if (s.size() > 0) {
        ObjectInstance oi = (ObjectInstance) s.iterator().next();
        dn = oi.getObjectName();
      } else {
        return null;
      }
    }      
    _domain = (DomainAdminMBean) MBeanServerInvocationHandler.newProxyInstance(_connection,
          dn, DomainAdminMBean.class, false);
    if (getDomainUuid() == null) {
      setDomainUuid(_domain.getDomainId());
    }
    return _domain;
  }
  
  protected SystemAdminMBean getSystem() throws ExecutionException {
    assert getSystemName() != null;
    
    if (_system != null) {
      return _system;
    }
    DomainAdminMBean db = getDomain();
    assert db != null;
    
    String key = "system";
    String value =getSystemName();

    ObjectName[] systems = null;
    try {
      systems = db.getSystems();
    } catch (Exception jme) {
      throw new ExecutionException(jme);
    }
    if (systems.length == 0) {
      return null;
    }
    for (int i = 0; i < systems.length; ++i) {
      if (systems[i].getKeyProperty(key).equals(value)) {
        _system = (SystemAdminMBean) MBeanServerInvocationHandler
            .newProxyInstance(_connection, systems[i], SystemAdminMBean.class, false);
        break;
      }
    }
    
    return _system;
  }
  
  protected void clearDomain() {
    _domain = null;
  }
  
  protected void clearSystem() {
    _system = null;
  }
  
  protected ServiceAdminMBean[] getServices(SystemAdminMBean sys) throws ExecutionException {
    ObjectName[] on;
    try {
      on = sys.getServices();
    } catch (Exception jme) {
      throw new ExecutionException(jme.getMessage(),jme);
    }

    ServiceAdminMBean[] svc = new ServiceAdminMBean[on.length];
    for (int i=0; i < on.length; ++i) {
      svc[i] = (ServiceAdminMBean) MBeanServerInvocationHandler
        .newProxyInstance(_connection, on[i],ServiceAdminMBean.class,false);
    }
    return svc;
  }
  
  protected SystemAdminMBean[] getSystems() throws ExecutionException {
    DomainAdminMBean db = getDomain();
    assert db != null;
    
    ObjectName[] systemONames;
    try {
      systemONames = db.getSystems();
    } catch (Exception jme) {
      throw new ExecutionException(jme.getMessage(),jme);
    }
    SystemAdminMBean[] systems = new SystemAdminMBean[systemONames.length];
    for (int i=0; i<systems.length; ++i) {
      systems[i] = (SystemAdminMBean) MBeanServerInvocationHandler
        .newProxyInstance(_connection, systemONames[i], SystemAdminMBean.class, false);
    }
    return systems;
  }
}
