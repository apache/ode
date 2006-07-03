/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.elang.xpath10.runtime;

import org.jaxen.Navigator;
import org.jaxen.dom.DocumentNavigator;
import org.w3c.dom.Node;

class BpelDocumentNavigator extends DocumentNavigator {

  private Node _documentRoot;
  BpelDocumentNavigator(Node docRoot) {
    _documentRoot = docRoot;
  }

  public Object getDocumentNode(Object contextNode) {
	  return _documentRoot;
  }
}
