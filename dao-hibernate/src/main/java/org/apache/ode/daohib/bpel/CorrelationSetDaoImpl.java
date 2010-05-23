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
package org.apache.ode.daohib.bpel;

import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HCorrelationSet;
import org.apache.ode.daohib.bpel.hobj.HCorrelationProperty;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.HashMap;

/**
 * Hibernate-based {@link CorrelationSetDAO} implementation.
 */
class CorrelationSetDaoImpl extends HibernateDao
    implements CorrelationSetDAO {

  private HCorrelationSet _correlationSet;

  public CorrelationSetDaoImpl(SessionManager sessionManager, HCorrelationSet correlationSet) {
    super(sessionManager, correlationSet);
      entering("CorrelationSetDaoImpl.CorrelationSetDaoImpl");
    _correlationSet = correlationSet;
  }

  public Long getCorrelationSetId() {
    return _correlationSet.getId();
  }

  public String getName() {
    return _correlationSet.getName();
  }

  public ScopeDAO getScope() {
      entering("CorrelationSetDaoImpl.getScope");
    return new ScopeDaoImpl(_sm, _correlationSet.getScope());
  }

    public void setValue(QName[] names, CorrelationKey values) {
        entering("CorrelationSetDaoImpl.setValue");
        _correlationSet.setValue(values.toCanonicalString());
        if (names != null) {
            if (_correlationSet.getProperties() == null || _correlationSet.getProperties().size() == 0) {
                for (int m = 0; m < names.length; m++) {
                    HCorrelationProperty prop =
                            new HCorrelationProperty(names[m], values.getValues()[m], _correlationSet);
                    getSession().save(prop);
                }
            } else {
                for (int m = 0; m < names.length; m++) {
                    HCorrelationProperty prop = getProperty(names[m]);
                    if (prop == null) prop = new HCorrelationProperty(names[m], values.getValues()[m], _correlationSet);
                    else prop.setValue(values.getValues()[m]);
                    getSession().save(prop);
                }
            }
        }
        getSession().update(_correlationSet);
    }

  public CorrelationKey getValue() {
      entering("CorrelationSetDaoImpl.getValue");
    if (_correlationSet.getValue() != null) return new CorrelationKey(_correlationSet.getValue());
    else return null;
  }

  public Map<QName, String> getProperties() {
      entering("CorrelationSetDaoImpl.getProperties");
    HashMap<QName, String> result = new HashMap<QName, String>();
    for (HCorrelationProperty property : _correlationSet.getProperties()) {
      result.put(property.getQName(), property.getValue());
    }
    return result;
  }

    public ProcessDAO getProcess() {
        return new ProcessDaoImpl(_sm, _correlationSet.getProcess());
    }

    public ProcessInstanceDAO getInstance() {
        return new ProcessInstanceDaoImpl(_sm, _correlationSet.getInstance());
    }

    private HCorrelationProperty getProperty(QName qName) {
      entering("CorrelationSetDaoImpl.getProperty");
    for (HCorrelationProperty property : _correlationSet.getProperties()) {
      if (qName.getLocalPart().equals(property.getName())
              && qName.getNamespaceURI().equals(property.getNamespace()))
        return property;
    }
    return null;
  }

}
