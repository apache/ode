package org.apache.ode.bpel.dao;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * Fault data access object. Used to access information about a
 * fault that affected a process execution (causing the failure of
 * an instance for example).
 */
public interface FaultDAO {

  QName getName();

  Element getData();

  String getExplanation();

  int getLineNo();

  int getActivityId();
}
