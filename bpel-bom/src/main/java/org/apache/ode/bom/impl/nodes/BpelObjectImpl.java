/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.BpelObject;
import org.apache.ode.utils.NSContext;

import java.io.Serializable;

/**
 * Base class for all objects in the BPEL object model.
 */
public abstract class BpelObjectImpl implements Serializable, BpelObject {

  private int _lineNo = -1;
  private NSContext _namespaceCtx;
  private String _description;

  protected BpelObjectImpl() {
  }

  protected BpelObjectImpl(NSContext nsctx) {
    _namespaceCtx = nsctx;
  }

  public int getLineNo() {
    return _lineNo;
  }

  public void setLineNo(int lineNo) {
    _lineNo = lineNo;
  }

  public NSContext getNamespaceContext() {
    return _namespaceCtx;
  }

  public void setNamespaceContext(NSContext ctx) {
    assert ctx != null;
    _namespaceCtx = ctx;
  }

  public String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

}
