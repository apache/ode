package org.apache.ode.daohib.bpel;


import javax.xml.namespace.QName;

import org.hibernate.Session;
import org.w3c.dom.Element;

import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HMessage;
import org.apache.ode.daohib.hobj.HLargeData;
import org.apache.ode.utils.DOMUtils;

public class MessageDaoImpl extends HibernateDao implements MessageDAO {

  private HMessage _hself;
  private Session _session;

  protected MessageDaoImpl(SessionManager sessionManager, HMessage hobj) {
    super(sessionManager, hobj);
    _hself = hobj;
    _session = sessionManager.getSession();
  }

  public void setType(QName type) {
    _hself.setType(type == null ? null : type.toString());
  }

  public QName getType() {
    return _hself.getType() == null ? null : QName.valueOf(_hself.getType()); 
  }

  public void setData(Element value) {
    if (_hself.getMessageData() != null)
      _session.delete(_hself.getMessageData());
    HLargeData newdata = new HLargeData(DOMUtils.domToString(value));
    _session.save(newdata);
    _hself.setMessageData(newdata);
  }

  public Element getData() {
    if (_hself.getMessageData() == null)
      return null;
    try {
      return DOMUtils.stringToDOM(_hself.getMessageData().getText());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
  }

  public MessageExchangeDAO getMessageExchange() {
    return new MessageExchangeDaoImpl(_sm,_hself.getMessageExchange());
  }

  
}
