/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.o;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Base class for variable types. 
 */
public abstract class OVarType extends OBase {
	
  public OVarType(OProcess owner) {
    super(owner);
  }
  
  /**
   * Create a new instance of this variable.
   * @return a "skeleton" representation of this variable
   */
  public abstract Node newInstance(Document doc);  
  
}
