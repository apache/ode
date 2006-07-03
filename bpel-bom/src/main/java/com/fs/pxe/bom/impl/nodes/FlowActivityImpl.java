/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.FlowActivity;
import com.fs.pxe.bom.api.Link;
import com.fs.utils.NSContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * BPEL object model representation of a  <code>&lt;flow&gt;</code> activity.
 *
 * @see CompositeActivityImpl
 */
public class FlowActivityImpl extends CompositeActivityImpl implements FlowActivity {

  private static final long serialVersionUID = 1L;
	private HashSet<Link> _links = new HashSet<Link>();

  /**
   * Constructor.
   *
   * @param nsContext namespace context for this activity
   */
  public FlowActivityImpl(NSContext nsContext) {
    super(nsContext);

  }

  public FlowActivityImpl() {
    super();
  }

  public String getType() {
    return "flow";
  }

  public void addLink(Link link) {
    _links.add(link);
  }

  public Set<Link> getLinks() {
    return Collections.unmodifiableSet(_links);
  }

}
