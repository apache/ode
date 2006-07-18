/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.o;

public abstract class OLValueExpression extends OExpression {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param owner
	 */
	public OLValueExpression(OProcess owner) {
		super(owner);
	}
	
	public abstract OScope.Variable getVariable();

}
