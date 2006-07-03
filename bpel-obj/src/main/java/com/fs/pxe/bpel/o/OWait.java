/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.o;

/**
 * Wait object
 */
public class OWait extends OActivity {
  
  static final long serialVersionUID = -1L  ;

  public OExpression forExpression;
  public OExpression untilExpression;

  public OWait(OProcess owner) {
    super(owner);
  }

  /**
	 * Is wait a duration?
	 * @return
	 */
	public boolean hasFor() { return forExpression != null; }
	
	/**
	 * Is wait an until? 
	 * @return
	 */
	public boolean hasUntil() { return untilExpression != null; }

}
