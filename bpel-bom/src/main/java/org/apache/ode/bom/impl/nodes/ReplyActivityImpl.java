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

import org.apache.ode.bom.api.Correlation;
import org.apache.ode.bom.api.ReplyActivity;
import org.apache.ode.utils.NSContext;

import java.util.List;

import javax.xml.namespace.QName;


/**
 * BPEL reply activity.  A reply activity is used to send a response to a
 * request previously accepted through a receive activity. Such responses are
 * only meaningful for synchronous interactions.
 */
public class ReplyActivityImpl extends ActivityImpl implements ReplyActivity {

  private static final long serialVersionUID = -1L;

  private String _partnerLink;
  private QName _portType;
  private String _operation;
  private String _variable;
  private QName _faultName;
  private String _messageExchange;
  private CorrelationHelperImpl _correlations = new CorrelationHelperImpl();

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public ReplyActivityImpl(NSContext nsContext) {
    super(nsContext);
  }

  public ReplyActivityImpl() {
    super();
  }

  public void setFaultName(QName name) {
    _faultName = name;
  }

  public QName getFaultName() {
    return _faultName;
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
    return "reply";
  }

  public void setVariable(String variable) {
    _variable = variable;
  }

  public String getVariable() {
    return _variable;
  }

  public void addCorrelation(Correlation cor) {
    _correlations.addCorrelation(cor);
  }

  public List<Correlation> getCorrelations(short patternMask) {
    return _correlations.getCorrelations(patternMask);
  }
}
