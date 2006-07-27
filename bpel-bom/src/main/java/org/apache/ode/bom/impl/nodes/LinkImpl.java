/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.FlowActivity;
import org.apache.ode.bom.api.Link;

/**
 * BPEL object model representation of a link between two activities.
 */
public class LinkImpl extends BpelObjectImpl implements Link {
  private static final long serialVersionUID = -1L;

  /**
   * Name of the link.
   */
  private String _name;

  private FlowActivity _declaredIn;
  
  /**
   * Constructor.
   */
  public LinkImpl() {
  }

  public FlowActivity getDeclaredIn() {
    return _declaredIn;
  }

  public String getLinkName() {
    return _name;
  }

  public void setLinkName(String name) {
    _name = name;
  }

  void setDeclaredIn(FlowActivity declaredId) {
    _declaredIn = declaredId;
  }
}
