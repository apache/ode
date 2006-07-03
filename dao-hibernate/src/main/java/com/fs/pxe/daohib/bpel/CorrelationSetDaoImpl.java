/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.bpel;

import com.fs.pxe.bpel.dao.CorrelationSetDAO;
import com.fs.pxe.bpel.dao.ScopeDAO;
import com.fs.pxe.bpel.common.CorrelationKey;
import com.fs.pxe.daohib.SessionManager;
import com.fs.pxe.daohib.bpel.hobj.HCorrelationSet;
import com.fs.pxe.daohib.bpel.hobj.HCorrelationProperty;

import javax.xml.namespace.QName;
import java.util.Set;
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
    _correlationSet = correlationSet;
  }

  public Long getCorrelationSetId() {
    return _correlationSet.getId();
  }

  public String getName() {
    return _correlationSet.getName();
  }

  public ScopeDAO getScope() {
    return new ScopeDaoImpl(_sm, _correlationSet.getScope());
  }

  public void setValue(QName[] names, CorrelationKey values) {
    _correlationSet.setValue(CorrelationKeySerializer.toCanonicalString(values));
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
    getSession().update(_correlationSet);
  }

  public CorrelationKey getValue() {
    if (_correlationSet.getValue() != null) return new CorrelationKey(_correlationSet.getValue());
    else return null;
  }

  public Map<QName, String> getProperties() {
    HashMap<QName, String> result = new HashMap<QName, String>();
    for (HCorrelationProperty property : _correlationSet.getProperties()) {
      result.put(property.getQName(), property.getValue());
    }
    return result;
  }

  private HCorrelationProperty getProperty(QName qName) {
    for (HCorrelationProperty property : _correlationSet.getProperties()) {
      if (qName.getLocalPart().equals(property.getName())
              && qName.getNamespaceURI().equals(property.getNamespace()))
        return property;
    }
    return null;
  }

}
