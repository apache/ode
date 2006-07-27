package org.apache.ode.bpel.dao;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

// TODO: Abstract out the data representation.
public interface MessageDAO {

  void setType(QName type);
  
  QName getType();
  
  void setData(Element value);
  
  Element getData();

  MessageExchangeDAO getMessageExchange();
}
