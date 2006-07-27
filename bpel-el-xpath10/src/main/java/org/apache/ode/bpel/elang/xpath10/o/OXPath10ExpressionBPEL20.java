/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.elang.xpath10.o;

import org.apache.ode.bpel.o.OProcess;

import java.io.Serializable;

import javax.xml.namespace.QName;


/**
 * Jaxen-based compiled-xpath representation for XPATH 1.0 expression language. 
 */
public class OXPath10ExpressionBPEL20 extends OXPath10Expression
        implements Serializable {
	private static final long serialVersionUID = -1L;

  /** QName of the <code>bpws:getVariableData</code> function. */
  public final QName qname_doXslTransform;

  /** Flags this expression as a joinCondition */
  public final boolean isJoinExpression;

  public OXPath10ExpressionBPEL20(OProcess owner,
      QName qname_getVariableData,
      QName qname_getVariableProperty,
      QName qname_getLinkStatus,
      QName qname_doXslTransform,
      boolean isJoinExpression) {
  	super(owner, qname_getVariableData, qname_getVariableProperty, qname_getLinkStatus);
    this.qname_doXslTransform = qname_doXslTransform;
    this.isJoinExpression = isJoinExpression;
  }

  public String toString() {
    return "{OXPath10Expression " + xpath + "}";
  }
}

