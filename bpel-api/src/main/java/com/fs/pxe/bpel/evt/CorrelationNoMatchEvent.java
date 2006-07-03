/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.evt;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import com.fs.pxe.bpel.common.CorrelationKey;

/**
 * Message arrived and matched neither (a) createInstance or (b) correlation match
 */
public class CorrelationNoMatchEvent extends CorrelationEvent {
  private static final long serialVersionUID = 1L;
  private final HashSet<CorrelationKey> _keys = new HashSet<CorrelationKey>();
  
  public CorrelationNoMatchEvent(QName qName, String opName, String mexId, CorrelationKey[] keys) {
    super(qName, opName, mexId);
    for (CorrelationKey key:keys)
      _keys.add(key);
  }

  public Set<CorrelationKey> getKeys() {
    return _keys;
  }
  
  public void setKeys(Set<CorrelationKey> keys) {
    _keys.clear();
    _keys.addAll(keys);
  }

}
