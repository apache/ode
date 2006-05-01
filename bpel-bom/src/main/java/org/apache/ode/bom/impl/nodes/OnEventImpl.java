/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.OnEvent;
import org.apache.ode.utils.NSContext;

import javax.xml.namespace.QName;

public class OnEventImpl extends OnMessageImpl
  implements OnEvent
{
  private static final long serialVersionUID = -1L;
  
  private QName _element;
  private QName _messageType;
  
  public OnEventImpl(NSContext nsContext) {
    super(nsContext);
  }

  public OnEventImpl() {
    super();
  }

  public QName getElement() {
    return _element;
  }

  public QName getMessageType() {
    return _messageType;
  }

  public void setElement(QName q) {
    _element = q;
  }

  public void setMessageType(QName q) {
    _messageType = q;
  }
  
}
