/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
  
  public List<Correlation> getCorrelations() {
   return _correlations.getCorrelations();
  }
}

