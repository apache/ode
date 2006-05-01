/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Correlation;
import org.apache.ode.bom.api.ReceiveActivity;
import org.apache.ode.utils.NSContext;

import java.util.List;

import javax.xml.namespace.QName;

/**
 * Factory for BPEL <code>receive</code> activities.
 */
public class ReceiveActivityImpl extends ActivityImpl implements ReceiveActivity {

  private static final long serialVersionUID = -1L;

  private String _partnerLink;
  private QName _portType;
  private String _operation;
  private String _variable;
  private String _messageExchange;
	
  private boolean _createInstance;
  private CorrelationHelperImpl _correlations = new CorrelationHelperImpl();

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public ReceiveActivityImpl(NSContext nsContext) {
    super(nsContext);
  }

  public ReceiveActivityImpl() {
    super();
  }


  public void setOperation(String operation) {
    _operation = operation;
  }

  public String getOperation() {
    return _operation;
  }

  public void setPartnerLink(String partnerLink) {
    _partnerLink = partnerLink;
  }

  public String getPartnerLink() {
    return _partnerLink;
  }

  public void setPortType(QName type) {
    _portType = type;
  }

  public QName getPortType() {
    return _portType;
  }
  
  public String getMessageExchangeId() {
    return _messageExchange;
  }
  
  public void setMessageExchangeId(String messageExchange) {
    _messageExchange = messageExchange;
  }

  public String getType() {
    return "receive";
  }

  public void setVariable(String variable) {
    _variable = variable;
  }

  public String getVariable() {
    return _variable;
  }

  public void setCreateInstance(boolean createInstance) {
    _createInstance = createInstance;
  }

  public boolean getCreateInstance() {
    return _createInstance;
  }

  public void addCorrelation(Correlation correlation) {
    _correlations.addCorrelation(correlation);
  }

  public List<Correlation> getCorrelations(short patternMask) {
    return _correlations.getCorrelations(patternMask);
  }
}
