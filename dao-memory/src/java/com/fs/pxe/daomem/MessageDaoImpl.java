/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daomem;

import com.fs.pxe.sfwk.bapi.dao.MessageDAO;
import com.fs.pxe.sfwk.bapi.dao.MessageExchangeDAO;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;


/**
 * In-memory {@link MessageDAO} implementation.
 */
class MessageDaoImpl implements MessageDAO {
  private Map<String, Element> _parts = new HashMap<String, Element>();
  private MessageExchangeDAO _mexDao;

  MessageDaoImpl(MessageExchangeDAO mexDao) {
    _mexDao = mexDao;
  }

  public MessageExchangeDAO getMessageExchange() {
    return _mexDao;
  }

  public void setPart(String partName, Element e) {
    _parts.put(partName, e);
  }

  public Element getPart(String partName) {
    return _parts.get(partName);
  }
}
