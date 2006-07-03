/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.Correlation;
import com.fs.pxe.bom.api.OnMessage;
import com.fs.utils.NSContext;

import java.util.List;

import javax.xml.namespace.QName;


/**
 * BPEL object model representation of an <code>onMessage</code> decleration.
 */
public class OnMessageImpl extends ScopeImpl implements OnMessage {

  private static final long serialVersionUID = -1L;

  private String _partnerLink;
  private QName _portType;
  private String _operation;
  private String _variable;
  private CorrelationHelperImpl _correlations = new CorrelationHelperImpl();
  private Activity _activity;
  private Object _declaredIn;
  private String _messageExchange;

  public OnMessageImpl(NSContext nsContext) {
    super(nsContext);
  }

  public OnMessageImpl() {
    super();
  }

  public String getType() {
    return "onEvent";
  }

  public com.fs.pxe.bom.api.Activity getActivity() {
    return _activity;
  }

  public void setActivity(Activity activity) {
    _activity = activity;
  }

  public String getOperation() {
    return _operation;
  }

  public void setOperation(String operation) {
    _operation = operation;
  }

  public String getPartnerLink() {
    return _partnerLink;
  }

  public void setPartnerLink(String partnerLink) {
    _partnerLink = partnerLink;
  }

  public QName getPortType() {
    return _portType;
  }

  public void setPortType(QName portType) {
    _portType = portType;
  }

  public String getVariable() {
    return _variable;
  }

  public void setVariable(String variableName) {
    _variable = variableName;
  }


  public List<Correlation> getCorrelations(short patternMask) {
    return _correlations.getCorrelations(patternMask);
  }
  
  public List<Correlation> getCorrelations() {
    return _correlations.getCorrelations();
  }
  
  public void addCorrelation(Correlation c) {
    _correlations.addCorrelation(c);
  }

  void setDeclaredIn(Object declaredIn) {
    _declaredIn = declaredIn;
  }
  
	public String getMessageExchangeId() {
		return _messageExchange;
	}
  
	public void setMessageExchangeId(String messageExchange) {
		_messageExchange = messageExchange;
	}
}
