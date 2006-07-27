/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.api;

import org.apache.ode.utils.NSContext;

/**
 * Common interface to all BPEL object model (BOM) objects. Provides for location
 * information (i.e. line numbers) and namespace context (XML namespace prefix maps).
 */
public interface BpelObject {

  /**
   * Get the line number in the BPEL source document where this object is defined.
   *
   * @return line number
   */
  int getLineNo();

  /**
   * Set the line number in the BPEL source document where this object is defined.
   *
   * @param lineNo line number
   */
  void setLineNo(int lineNo);

  /**
   * Get the namespace context for this BPEL object (i.e. prefix-to-namespace mapping).
   *
   * @return namespace context
   */
  NSContext getNamespaceContext();

  /**
   * Set the namespace context for this BPEL object (i.e. prefix-to-namespace mapping).
   *
   * @param ctx namespace context
   */
  void setNamespaceContext(NSContext ctx);

  /**
   * Returns the human-readable description of this object.
   * 
   * @return the description
   */
  String getDescription();
  
  /**
   * Set a description of this model element.
   * @param description human-readable description
   */
  void setDescription(String description);

}
