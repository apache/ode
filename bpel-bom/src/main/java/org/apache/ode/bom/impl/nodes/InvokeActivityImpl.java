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
import org.apache.ode.bom.api.InvokeActivity;
import org.apache.ode.utils.NSContext;

import java.util.List;

import javax.xml.namespace.QName;


/**
 * BPEL object model representation of an <code>&lt;invoke&gt;</code> activity.
 */
public class InvokeActivityImpl extends ScopeImpl implements InvokeActivity {

  private static final long serialVersionUID = -1L;

  private String _partnerLink;
  private String _inputVar;
  private String _outputVar;
  private QName _portType;
  private String _operation;
  private CorrelationHelperImpl _correlations = new CorrelationHelperImpl();

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public InvokeActivityImpl(NSContext nsContext) {
    super(nsContext);
  }

  public InvokeActivityImpl() {
    super();
  }

  public void setInputVar(String variable) {
    _inputVar = variable;
  }

  public String getInputVar() {
    return _inputVar;
  }

  public void setOperation(String operation) {
    _operation = operation;
  }

  public String getOperation() {
    return _operation;
  }

  public void setOutputVar(String variable) {
    _outputVar = variable;
  }

  public String getOutputVar() {
    return _outputVar;
  }

  public void setPartnerLink(String partnerLink) {
    _partnerLink = partnerLink;
  }

  public String getPartnerLink() {
    return _partnerLink;
  }

  public void setPortType(QName portType) {
    _portType = portType;
  }

  public QName getPortType() {
    return _portType;
  }

  /**
   * @see org.apache.ode.bom.impl.nodes.ActivityImpl#getType()
   */
  public String getType() {
    return "invoke";
  }

  public List<Correlation> getCorrelations(short patternMask) {
    return _correlations.getCorrelations(patternMask);
  }

  
  public void addCorrelation(Correlation correlation) {
    _correlations.addCorrelation(correlation);
  }


  public void setNamespaceContext(NSContext ctx) {
    super.setNamespaceContext(ctx);
  }

  public List<Correlation> getCorrelations() {
    return _correlations.getCorrelations();
  }
}
