/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

import org.apache.ode.bpel.o.OScope;

import java.io.Serializable;

/**
 * Variable instance identifier.
 */
public class VariableInstance implements Serializable {
	private static final long serialVersionUID = 1L;

	public final OScope.Variable declaration;
  public final Long scopeInstance;

  VariableInstance(Long scopeInstance, OScope.Variable variable) {
    this.scopeInstance = scopeInstance;
    this.declaration = variable;
  }

  public boolean equals(Object obj) {
    VariableInstance other = (VariableInstance) obj;
    return other.declaration.equals(declaration) && other.scopeInstance.equals(scopeInstance);
  }

}
