/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.sfwk;

import com.fs.pxe.daohib.sfwk.hobj.HSfwkMessage;
import com.fs.pxe.daohib.sfwk.hobj.HMessagePart;
import com.fs.pxe.daohib.hobj.HLargeData;
import com.fs.pxe.sfwk.bapi.dao.MessageDAO;
import com.fs.pxe.sfwk.bapi.dao.MessageExchangeDAO;
import com.fs.utils.DOMUtils;

import java.io.IOException;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Hibernate-based {@link MessageDAO} implementation.
 */
final class MessageDaoImpl implements MessageDAO {
  Session _session;
  HSfwkMessage _hself;
  MessageExchangeDaoImpl _mexDao;

  MessageDaoImpl(MessageExchangeDaoImpl mexDao, HSfwkMessage hself){
    _mexDao = mexDao;
    _session = mexDao._session;
    _hself = hself;
  }

  public Element getPart(String partName) {
    try {
      Query qry = _session.createQuery("from com.fs.pxe.daohib.sfwk.hobj.HMessagePart as mp where mp.part = ? and mp.message.id=?");
      qry.setString(0,partName);
      qry.setLong(1,_hself.getId());
      List results =  qry.list();
      if (results.isEmpty())
        return null;

      HLargeData data = ((HMessagePart) results.get(0)).getData();
      if (data == null) return null;
      return DOMUtils.stringToDOM(data.getText());
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
  }


  public void setPart(String partName, Element value) {
    Query qry = _session.createQuery("from com.fs.pxe.daohib.sfwk.hobj.HMessagePart as mp where mp.part = ? and mp.message.id=?");
    qry.setString(0,partName);
    qry.setLong(1,_hself.getId());
    List results =  qry.list();
    if (!results.isEmpty())
      _session.delete(results.get(0));

    HMessagePart thePart = new HMessagePart(_hself, partName);
    HLargeData ld = new HLargeData(DOMUtils.domToString(value));
    thePart.setData(ld);
    _hself.getParts().add(thePart);
    _session.save(ld);
    _session.save(thePart);

  }

  public MessageExchangeDAO getMessageExchange() {
    return _mexDao;
  }
}
