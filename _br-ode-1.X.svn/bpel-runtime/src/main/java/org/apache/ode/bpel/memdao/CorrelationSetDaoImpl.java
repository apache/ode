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

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;


/**
 * A very simple, in-memory implementation of the {@link CorrelationSetDAO} interface.
 */
class CorrelationSetDaoImpl extends DaoBaseImpl implements CorrelationSetDAO {
  private Long _csetId;
  private ScopeDAO _scope;
  private String _name;
  private CorrelationKey _key;
  private HashMap<QName,String> _corrValues;

  /**
   * Constructor.
   * @param name correlation set name
   * @param scope the scope for which the correlation set is relevant
   */
  public CorrelationSetDaoImpl(String name, org.apache.ode.bpel.dao.ScopeDAO scope) {
    _name = name;
    _scope = scope;
    _csetId = IdGen.newCorrelationSetId();
  }

  public Long getCorrelationSetId() {
    return _csetId;
  }

  /**
   * @see org.apache.ode.bpel.dao.CorrelationSetDAO#getName()
   */
  public String getName() {
    return _name;
  }

  /**
   * @see org.apache.ode.bpel.dao.CorrelationSetDAO#getScope()
   */
  public ScopeDAO getScope() {
    return _scope;
  }

    public void setValue(QName[] names, CorrelationKey values) {
        _key = values;
        if (names != null) {
            _corrValues = new HashMap<QName, String>();
            for (int m = 0; m < names.length; m++) {
                _corrValues.put(names[m], values.getValues()[m]);
            }
        }
    }

  public CorrelationKey getValue() {
    return _key;
  }

  public Map<QName, String> getProperties() {
    return _corrValues;
  }

    public ProcessDAO getProcess() {
        return getScope().getProcessInstance().getProcess();
    }

    public ProcessInstanceDAO getInstance() {
        return getScope().getProcessInstance();
    }
}
