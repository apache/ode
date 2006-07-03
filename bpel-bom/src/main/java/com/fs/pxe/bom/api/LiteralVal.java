/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

import org.w3c.dom.Element;

/**
 * BOM representation of an XML-literal assignment R-value.
 * Corresponds to the literal from-spec.
 */
public interface LiteralVal extends From {
  /**
   * Get the literal XML value.
   * @return an XML {@link Element}
   */
  Element getLiteral();

  /**
   * Set the literal XML value.
   * @param literalNode an XML {@link Element}
   */
  void setLiteral(Element literalNode);
}
