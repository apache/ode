package com.fs.pxe.bpel.engine;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.fs.pxe.bpel.dao.MessageDAO;
import com.fs.pxe.bpel.iapi.Content;
import com.fs.pxe.bpel.iapi.Message;

public class MessageImpl implements Message {

  MessageDAO _dao;

  public MessageImpl(MessageDAO message) {
    if (message == null)
      throw new NullPointerException("null message!");
    _dao = message;
  }

  public Content getPart(String partName) {
    // TODO Auto-generated method stub
    return null;
  }

  public void setMessagePart(String partName, Content content) {
    // TODO Auto-generated method stub
    
  }

  public void setMessage(Element msg) {
    _dao.setData(msg);
  }

  public Element getMessage() {
    return _dao.getData();
  }

  public QName getType() {
    return _dao.getType();
  }

  public List<String> getParts() {
    // TODO Auto-generated method stub
    return null;
  }

}
