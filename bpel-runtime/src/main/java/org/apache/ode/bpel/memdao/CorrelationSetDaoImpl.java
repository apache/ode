/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.memdao;

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.ScopeDAO;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;


/**
 * A very simple, in-memory implementation of the {@link CorrelationSetDAO} interface.
 */
class CorrelationSetDaoImpl
  implements CorrelationSetDAO {
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
    _corrValues = new HashMap<QName, String>();
    for (int m = 0; m < names.length; m++) {
      _corrValues.put(names[m], values.getValues()[m]);
    }
  }


  public CorrelationKey getValue() {
    return _key;
  }

  public Map<QName, String> getProperties() {
    return _corrValues;
  }
}
