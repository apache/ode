package com.fs.pxe.bpel.engine;

import java.util.List;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import com.fs.pxe.bpel.dao.MessageDAO;
import com.fs.pxe.bpel.iapi.Message;

public class MessageImpl implements Message {

  MessageDAO _dao;

  public MessageImpl(MessageDAO message) {
    if (message == null)
      throw new NullPointerException("null message!");
    _dao = message;
  }

  public Element getPart(String partName) {
    Element message = getMessage();
    NodeList eltList = message.getElementsByTagName(partName);
    if (eltList.getLength() == 0) return null;
    else return (Element) eltList.item(0);
  }

  public void setMessagePart(String partName, Element content) {
    Element message = getMessage();
    message.appendChild(message.getOwnerDocument().importNode(content, true));
    setMessage(message);
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
    ArrayList<String> parts = new ArrayList<String>();
    Element message = getMessage();
    NodeList nodeList = message.getChildNodes();
    for (int m = 0; m < nodeList.getLength(); m++) {
      Node node = nodeList.item(m);
      if (node.getNodeType() == Node.ELEMENT_NODE)
        parts.add(node.getLocalName());
    }
    return parts;
  }

}
