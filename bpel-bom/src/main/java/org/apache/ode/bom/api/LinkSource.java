/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.api;

/**
 * BOM representation of a "link source" (i.e. a <code>&lt;source...&gt;</code> element) declaration.
 * A {@link LinkSource} is a triple joining a link declaration (by reference), an activity
 * declaration (by context) and a transition condition (by containment).
 */
public interface LinkSource extends BpelObject {

  /**
   * Get the activity that declares this link source.
   *
   * @return declaring {@link Activity} object
   */
  Activity getActivity();

  /**
   * Get the refernced link.
   *
   * @return name of referenced link
   */
  String getLinkName();

  /**
   * Set the referenced link.
   *
   * @param linkName name of referenced link
   */
  void setLinkName(String linkName);


  /**
   * Get the link transition condition.
   *
   * @return transition condition {@link Expression}
   */
  Expression getTransitionCondition();

  /**
   * Set the link transition condition.
   *
   * @param transitionCondition transition condition {@link Expression}
   */
  void setTransitionCondition(Expression transitionCondition);

}
