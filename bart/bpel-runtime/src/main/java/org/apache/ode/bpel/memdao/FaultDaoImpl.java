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

package org.apache.ode.bpel.memdao;

import org.apache.ode.bpel.dao.FaultDAO;
import org.apache.ode.utils.QNameUtils;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * In-memory implementation of {@link FaultDAO}.
 */
public class FaultDaoImpl extends DaoBaseImpl implements FaultDAO {

  private String _name;
  private String _explanation;
  private Element _data;
  private int _lineNo;
  private int _activityId;

  public FaultDaoImpl(String name, String explanation, Element data, int lineNo, int activityId) {
    _name = name;
    _explanation = explanation;
    _data = data;
    _lineNo = lineNo;
    _activityId = activityId;
  }

  public QName getName() {
    return QNameUtils.toQName(_name);
  }

  public void setName(QName name) {
    _name = QNameUtils.fromQName(name);
  }

  public String getExplanation() {
    return _explanation;
  }

  public void setExplanation(String explanation) {
    _explanation = explanation;
  }

  public Element getData() {
    return _data;
  }

  public void setData(Element data) {
    _data = data;
  }

  public int getLineNo() {
    return _lineNo;
  }

  public void setLineNo(int lineNo) {
    _lineNo = lineNo;
  }

  public int getActivityId() {
    return _activityId;
  }

  public void setActivityId(int activityId) {
    _activityId = activityId;
  }

}
