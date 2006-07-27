/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.api;

import java.util.Set;


/**
 * BOM representation of the BPEL <code>&lt;flow&gt;</code> activity.
 * See {@link CompositeActivity} for methods used to manipulate child activities.
 */
public interface FlowActivity extends CompositeActivity {
  /**
   * Add a link declaration to this &lt;flow&gt;.
   * @param link link declaration
   */
  void addLink(Link link);

  /**
   * Get the set of links declared in this &lt;flow&gt; activity.
   * @return {@link Set} of {@link Link} declared in this &lt;flow&gt;
   */
  Set<Link> getLinks();
}
