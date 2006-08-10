/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.memdao;

import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.w3c.dom.Node;

import java.util.Properties;


/**
 * A very simple, in-memory implementation of the {@link XmlDataDAO} interface.
 */
class XmlDataDaoImpl implements XmlDataDAO {

  private Node _data;
  private Properties _properties = new Properties();
  private ScopeDaoImpl _scope;
  private String _name;
  
  XmlDataDaoImpl(ScopeDaoImpl scope,String varname){
  	_scope = scope;
    _name = varname;
  }
  
  /**
   * @see XmlDataDAO#isNull()
   */
  public boolean isNull() {
    return _data == null;
  }

  /**
   * @see XmlDataDAO#get()
   */
  public Node get() {
    return _data;
  }

  /**
   * @see XmlDataDAO#remove()
   */
  public void remove() {
    _data = null;
  }

  /**
   * @see XmlDataDAO#set(org.w3c.dom.Node)
   */
  public void set(Node val) {
    _data = val;
  }

	/**
	 * @see org.apache.ode.bpel.dao.XmlDataDAO#getProperty(java.lang.String)
	 */
	public String getProperty(String propertyName) {
		return _properties.getProperty(propertyName);
	}

	/**
	 * @see org.apache.ode.bpel.dao.XmlDataDAO#setProperty(java.lang.String, java.lang.String)
	 */
	public void setProperty(String pname, String pvalue) {
		_properties.setProperty(pname, pvalue);
	}

	public Properties getProperties() {
		return _properties;
	}

	/**
	 * @see org.apache.ode.bpel.dao.XmlDataDAO#getScopeDAO()
	 */
	public ScopeDAO getScopeDAO() {
		return _scope;
	}

  public String getName() {
    return _name;
  }

}
