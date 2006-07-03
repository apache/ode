/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

import org.w3c.dom.Node;

/**
 * BOM representation of a BPEL expression language expression.
 */
public interface Expression extends BpelObject {

  /**
   * Get the expression language for this expression.
   * @return expression langauge URI or <code>null</code> if none specified
   */
  String getExpressionLanguage();

  /**
   * Set the expression "text" (i.e. the program listing).
   * @todo rename this method to setText()
   */
  void setXPathString(String xpathString);

  /**
   * Get the expression "text" (i.e. the program listing).
   * @todo rename this method to getText()
   * @return expression text
   */
  String getXPathString();
  
  /**
   * The expression as a DOM node.
   * @return
   */
  Node getNode();
  
  /**
   * Set the expression as a DOM node.
   * @param node
   */
  void setNode(Node node);

}
