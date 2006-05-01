/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.api;

/**
 * BOM representation of &lt;flow&gt;'s link declaration.
 */
public interface Link extends BpelObject {

  /**
   * The flow activity in which the link is declared.
   *
   * @return declaring &lt;flow&gt;
   */
  FlowActivity getDeclaredIn();

  /**
   * Get this link's name.
   *
   * @return name.
   */
  String getLinkName();

}
