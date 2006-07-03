/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.Expression;
import com.fs.pxe.bom.api.LinkSource;
import com.fs.pxe.bom.api.LinkTarget;
import com.fs.utils.NSContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Base class extended by all BPEL activities. This class provides data and
 * logic applicable to all BPEL activities, including links, join conditions,
 * transition conditions, and implicit scope handling.
 */
public abstract class ActivityImpl extends BpelObjectImpl implements com.fs.pxe.bom.api.Activity {

  private static final long serialVersionUID = -1L;

  /**
   * Name of the activity.
   */
  private String _name;

  /**
   * Join condition (activation condition) .
   */
  private Expression _joinCondition;

  /**
   * Should join failures be suppressed?
   */
  private short _suppressJoinFailure;

  /**
   * All the {@link LinkImpl}s where this activity is the source (indexed by
   * link name).
   */
  private final HashSet<LinkSource> _sourceLinks = new HashSet<LinkSource>();

  /**
   * All the {@link LinkImpl}s where this activity is the target (indexed by
   * link name).
   */
  private final HashSet<LinkTarget> _targetLinks = new HashSet<LinkTarget>();

  /**
   * A user-readable description of the activity (digest of original BPEL XML)
   */
  private String _description;

  protected ActivityImpl(NSContext nsContext) {
    super(nsContext);
  }

  protected ActivityImpl() {
    super();
  }

  /**
   * Description of the activity.
   *
   * @param description Description of the activity.
   */
  public void setDescription(String description) {
    _description = description;
  }

  /**
   * Description of the activity.
   *
   * @return Description of the activity.
   */
  public String getDescription() {
    return _description;
  }

  /**
   * A join condition is used to specify requirements about concurrent paths
   * reaching at an activity. See {@link Expression}
   *
   * @param joinCondition
   */
  public void setJoinCondition(Expression joinCondition) {
    _joinCondition = joinCondition;
  }

  /**
   * Returns the <code>JoinConditionExpr</code>
   *
   * @return the join expression
   */
  public Expression getJoinCondition() {
    return _joinCondition;
  }

  /**
   * Sets name of activity
   *
   * @param name
   */
  public void setName(String name) {
    _name = name;
  }

  /**
   * Returns name of activity
   *
   * @return activity name
   */
  public String getName() {
    return _name;
  }

  /**
   * This attribute determines whether the joinFailure fault will be
   * suppressed for all activities in the process. The default for this
   * attribute is "no".
   *
   * @param suppressJoinFailure
   */
  public void setSuppressJoinFailure(short suppressJoinFailure) {
    _suppressJoinFailure = suppressJoinFailure;
  }

  /**
   * Returns suppressJoinFailure state
   *
   * @return <code>true</code> if join failures are suppressed
   */
  public short getSuppressJoinFailure() {
    return _suppressJoinFailure;
  }

  /**
   * Return the names of the links for which this node is a target.
   */
  public Set<LinkTarget> getLinkTargets() {
    return Collections.unmodifiableSet(_targetLinks);
  }

  /**
   * Returns the names of the links for which this activity is the source.
   *
   * @return links
   */
  public Set<LinkSource> getLinkSources() {
    return Collections.unmodifiableSet(_sourceLinks);
  }


  public void addSource(LinkSource linkSource) {
    ((LinkSourceImpl) linkSource).setActivity(this);
    _sourceLinks.add(linkSource);

  }

  public void addTarget(LinkTarget linkTarget) {
    ((LinkTargetImpl) linkTarget).setActivity(this);
    _targetLinks.add(linkTarget);
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "{" + getType() + " activity \"" + getName()+ "\"}";
  }

}
