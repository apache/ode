/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.sfwk;

import com.fs.pxe.daohib.sfwk.hobj.HSfwkMessage;
import com.fs.pxe.daohib.sfwk.hobj.HSfwkMessageExchange;
import com.fs.pxe.daohib.hobj.HLargeData;
import com.fs.pxe.sfwk.bapi.dao.MessageDAO;
import com.fs.pxe.sfwk.bapi.dao.MessageExchangeDAO;
import com.fs.utils.DOMUtils;
import com.fs.utils.QNameUtils;
import org.hibernate.Session;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Date;

/**
 * Hibernate-based {@link MessageExchangeDAO} implementation.
 */
final class MessageExchangeDaoImpl implements MessageExchangeDAO {

  Session _session;
  HSfwkMessageExchange _hself;
  Node _sourceNode;
  Node _destNode;

  /**
   * Constructor.
   */
  public MessageExchangeDaoImpl(Session sess, HSfwkMessageExchange ex) {
    _session = sess;
    _hself = ex;
  }

  /**
   * @see com.fs.pxe.sfwk.bapi.dao.MessageExchangeDAO#getChannelName()
   */
  public String getChannelName() {
    return _hself.getChannelName();
  }

  /**
   * @see com.fs.pxe.sfwk.bapi.dao.MessageExchangeDAO#getCreateTime()
   */
  public Date getCreateTime() {
    return _hself.getInsertTime();
  }
  /**
   * @see com.fs.pxe.sfwk.bapi.dao.MessageExchangeDAO#getInputMessage()
   */
  public MessageDAO getInputMessage() {
    return  _hself.getInputMessage() == null ?  null :
            new MessageDaoImpl(this, _hself.getInputMessage());
  }

  /**
   * @see com.fs.pxe.sfwk.bapi.dao.MessageExchangeDAO#getInstanceId()
   */
  public String getInstanceId() {
    return _hself.getInstanceId();
  }

  /**
   * @see com.fs.pxe.sfwk.bapi.dao.MessageExchangeDAO#getOperationName()
   */
  public String getOperationName() {
    return _hself.getOperationName();
  }

  /**
   * @see com.fs.pxe.sfwk.bapi.dao.MessageExchangeDAO#getOutputMessage()
   */
  public MessageDAO getOutputMessage() {
    return  _hself.getOutputMessage() == null ?  null :
            new MessageDaoImpl(this, _hself.getOutputMessage());
  }

  /**
   * @see com.fs.pxe.sfwk.bapi.dao.MessageExchangeDAO#getPortType()
   */
  public QName getPortType() {
    return QNameUtils.toQName(_hself.getPortType());
  }

  /**
   * @see com.fs.pxe.sfwk.bapi.dao.MessageExchangeDAO#setState(int)
   */
  public void setState(int state) {
    _hself.setState(state);
    _session.update(_hself);
  }

  /**
   * @see com.fs.pxe.sfwk.bapi.dao.MessageExchangeDAO#getState()
   */
  public int getState() {
    return _hself.getState();
  }

  /**
   * @see com.fs.pxe.sfwk.bapi.dao.MessageExchangeDAO#getCorrelationId()
   */
  public byte[] getCorrelationId() {
    return _hself.getCorrelationId().getBinary();
  }

  /**
   * @see com.fs.pxe.sfwk.bapi.dao.MessageExchangeDAO#setCorrelationId(byte[])
   */
  public void setCorrelationId(byte[] correlationId) {
    if (_hself.getCorrelationId() != null)
      _session.delete(_hself.getCorrelationId());
    if (correlationId.length > 0) {
      HLargeData ld = new HLargeData(correlationId);
      _hself.setCorrelationId(ld);
      _session.save(ld);
    }
  }

  public Node getSourceEndpoint() {
    if(_sourceNode == null){
      _sourceNode = prepareSource();
    }
    return _sourceNode;
  }

  public void setSourceEndpoint(Node val) {
    if (val == null) return;
    _sourceNode = val;
    _hself.setSourceEndpointSimpleType(!(val instanceof Element));
    if (_hself.getSourceEndpoint() != null)
      _session.delete(_hself.getSourceEndpoint());
    HLargeData ld = new HLargeData();
    if(_hself.isSourceEndpointSimpleType()) {
      ld.setBinary(_sourceNode.getNodeValue().getBytes());
      _hself.setSourceEndpoint(ld);
    } else {
      ld.setBinary(DOMUtils.domToString(_sourceNode).getBytes());
      _hself.setSourceEndpoint(ld);
    }
    _session.save(ld);
    _session.update(_hself);
  }

  public Node getDestinationEndpoint() {
    if(_destNode == null){
      _destNode = prepareDest();
    }
    return _destNode;
  }

  public void setDestinationEndpoint(Node val) {
    if (val == null) return;
    _destNode = val;
    _hself.setDestinationEndpointSimpleType(!(val instanceof Element));
    if (_hself.getDestinationEndpoint() != null)
      _session.delete(_hself.getDestinationEndpoint());
    HLargeData ld = new HLargeData();
    if(_hself.isDestinationEndpointSimpleType()) {
      ld.setBinary(_destNode.getNodeValue().getBytes());
      _hself.setDestinationEndpoint(ld);
    } else {
      ld.setBinary(DOMUtils.domToString(_destNode).getBytes());
      _hself.setDestinationEndpoint(ld);
    }
    _session.save(ld);
    _session.update(_hself);
  }

  public boolean isPinned() {
    return _hself.getPinned();
  }

  public void setPinned(boolean pinned) {
    _hself.setPinned(pinned);
    _session.update(_hself);
  }

  public Serializable getDHandle() {
    throw new UnsupportedOperationException();
  }

  public void addInputMessage(MessageDAO msg) {
    _hself.setInputMessage(((MessageDaoImpl)msg)._hself);
    _session.saveOrUpdate(((MessageDaoImpl)msg)._hself);
    _session.update(_hself);
  }

  public void addOutputMessage(MessageDAO msg) {
    _hself.setOutputMessage(((MessageDaoImpl)msg)._hself);
    _session.saveOrUpdate(((MessageDaoImpl)msg)._hself);
    _session.update(_hself);
  }

  public MessageDAO createMessage() {
    HSfwkMessage newmsg = new HSfwkMessage();
    _session.save(newmsg);
    return new MessageDaoImpl(this, newmsg);
  }

  private Node prepareSource() {
    if(_hself.getSourceEndpoint() == null)
      return null;
    String data = _hself.getSourceEndpoint().getText();
    if(_hself.isSourceEndpointSimpleType()){
      Document d = DOMUtils.newDocument();
      // we create a dummy wrapper element
      // prevents some apps from complaining
      // when text node is not actual child of document
      Element e = d.createElement("foo");
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

  private Node prepareDest() {
    if(_hself.getDestinationEndpoint() == null)
      return null;
    String data = _hself.getDestinationEndpoint().getText();
    if(_hself.isDestinationEndpointSimpleType()){
      Document d = DOMUtils.newDocument();
      // we create a dummy wrapper element
      // prevents some apps from complaining
      // when text node is not actual child of document
      Element e = d.createElement("foo");
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

}
