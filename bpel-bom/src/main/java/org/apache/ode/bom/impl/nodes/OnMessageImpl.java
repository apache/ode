/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.Correlation;
import org.apache.ode.bom.api.OnMessage;
import org.apache.ode.utils.NSContext;

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

  public org.apache.ode.bom.api.Activity getActivity() {
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
