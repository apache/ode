/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.bpel;

import com.fs.pxe.bpel.dao.ScopeDAO;
import com.fs.pxe.bpel.dao.XmlDataDAO;
import com.fs.pxe.daohib.SessionManager;
import com.fs.pxe.daohib.hobj.HLargeData;
import com.fs.pxe.daohib.bpel.hobj.HVariableProperty;
import com.fs.pxe.daohib.bpel.hobj.HXmlData;
import com.fs.utils.DOMUtils;

import java.util.Iterator;

import org.hibernate.Query;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Hibernate-based {@link XmlDataDAO} implementation.
 */
class XmlDataDaoImpl extends HibernateDao implements XmlDataDAO {
  private static final String QUERY_PROPERTY =
          "from " + HVariableProperty.class.getName() +
          " as p where p.xmlData.id = ? and p.name = ?";

	private HXmlData _data;
  private Node _node;

  /**
	 * @param hobj
	 */
	public XmlDataDaoImpl(SessionManager sm, HXmlData hobj) {
		super(sm, hobj);
    _data = hobj;
	}
	/**
	 * @see com.fs.pxe.bpel.dao.XmlDataDAO#isNull()
	 */
	public boolean isNull() {
		return _data.getData() == null;
	}
  
	/**
	 * @see com.fs.pxe.bpel.dao.XmlDataDAO#get()
	 */
	public Node get() {
		if(_node == null){
			_node = prepare();
    }
    return _node;
	}
	/**
	 * @see com.fs.pxe.bpel.dao.XmlDataDAO#remove()
	 */
	public void remove() {
		
	}
	/**
	 * @see com.fs.pxe.bpel.dao.XmlDataDAO#set(org.w3c.dom.Node)
	 */
	public void set(Node val) {
		_node = val;
    _data.setSimpleType(!(val instanceof Element));
    if (_data.getData() != null)
      _sm.getSession().delete(_data.getData());
    HLargeData ld = new HLargeData();
    if(_data.isSimpleType()) {
      ld.setBinary(_node.getNodeValue().getBytes());
      _data.setData(ld);
    } else {
      ld.setBinary(DOMUtils.domToString(_node).getBytes());
      _data.setData(ld);
    }
    getSession().save(ld);
    getSession().update(_data);
  }
	/**
	 * @see com.fs.pxe.bpel.dao.XmlDataDAO#getProperty(java.lang.String)
	 */
	public String getProperty(String propertyName) {
		
    HVariableProperty p = _getProperty(propertyName);
    return p == null
      ? null
      : p.getValue();
	}
	
	/**
	 * @see com.fs.pxe.bpel.dao.XmlDataDAO#setProperty(java.lang.String, java.lang.String)
	 */
	public void setProperty(String pname, String pvalue) {
		HVariableProperty p = _getProperty(pname);
    if(p == null){
    	p = new HVariableProperty(_data, pname, pvalue);
      getSession().save(p);
      _data.getProperties().add(p);
    }else{
      p.setValue(pvalue);
      getSession().update(p);
    }
  }
  
  /**
   * @see com.fs.pxe.bpel.dao.XmlDataDAO#getScopeDAO()
   */
  public ScopeDAO getScopeDAO() {
    return new ScopeDaoImpl(_sm,_data.getScope());
  }
  
  private HVariableProperty _getProperty(String propertyName){
    Iterator iter;
    Query qry = getSession().createQuery(QUERY_PROPERTY);
    qry.setLong(0, _data.getId());
    qry.setString(1, propertyName);
    iter = qry.iterate();
    return iter.hasNext()
            ? (HVariableProperty)iter.next()
            : null;
  }
  
  private Node prepare(){
    if(_data.getData() == null)
      return null;
    String data = _data.getData().getText();
    if(_data.isSimpleType()){
      Document d = DOMUtils.newDocument();
      // we create a dummy wrapper element
      // prevents some apps from complaining
      // when text node is not actual child of document
      Element e = d.createElement("text-node-wrapper");
      Text tnode = d.createTextNode(data);
      d.appendChild(e);
      e.appendChild(tnode);
      return tnode;
    }else{
      try{
        return DOMUtils.stringToDOM(data);
      }catch(Exception e){
        throw new RuntimeException(e);
      }
    }
  }

  public String getName() {
    return _data.getName();
  }
	
}
