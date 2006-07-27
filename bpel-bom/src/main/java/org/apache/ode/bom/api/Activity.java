/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.api;

import java.util.Set;

/**
 * Interface common to all BPEL activities. This interface provides methods for manipulating the
 * so-called "common attributes" such as source and target links, activity name, and supress join
 * failure flag.
 */
public interface Activity extends BpelObject, JoinFailureSuppressor {

  /**
   * Set the join condition.
   *
   * @param joinCondition the join condition
   */
  void setJoinCondition(Expression joinCondition);

  /**
   * Get the join condition.
   *
   * @return the join condition
   */
  Expression getJoinCondition();

  /**
   * Get the {@link LinkSource}s for this activity.
   *
   * @return set of {@link LinkSource}s
   */
  Set<LinkSource> getLinkSources();

  /**
   * Get the {@link LinkTarget}s for this activity.
   *
   * @return set of {@link LinkTarget}s
   */
  Set<LinkTarget> getLinkTargets();

  /**
   * The type of activity (e.g. "while", "sequence", "etc")
   *
   * @return activity type string
   */
  String getType();

  /**
   * Add a link source to this activity.
   *
   * @param linkSource link source to add
   */
  void addSource(LinkSource linkSource);

  /**
   * Add a link target to this activity.
   *
   * @param linkTarget link target to add
   */
  void addTarget(LinkTarget linkTarget);

  /**
   * Set (or clear) the user-defined name of this activity.
   *
   * @param name user-defined name or <code>null</code> to clear
   */
  void setName(String name);

  /**
   * Get the (optional) user-defined name for this activity.
   *
   * @return user-defined name or <code>null</code>
   */
  String getName();


}
