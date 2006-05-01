/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.LiteralVal;
import org.apache.ode.utils.NSContext;

import org.w3c.dom.Element;

public class LiteralValImpl extends BpelObjectImpl implements LiteralVal {
  private static final long serialVersionUID = 1L;
	private Element _literalNode;

  public LiteralValImpl(NSContext nsctx) {
    super(nsctx);
  }

  public Element getLiteral() {
    return _literalNode;
  }

  public void setLiteral(Element literalNode) {
    _literalNode = literalNode;
  }
}
