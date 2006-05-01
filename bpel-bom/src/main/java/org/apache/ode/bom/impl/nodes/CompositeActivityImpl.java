/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.CompositeActivity;
import org.apache.ode.utils.NSContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Base class for all BPEL structured activities such as <code>flow</code>,
 * <code>sequence</code>, and <code>while</code>. This class provides
 * facilities for keeping track of child activities.
 */
public abstract class CompositeActivityImpl extends ActivityImpl implements CompositeActivity {

  private final ArrayList<Activity> _orderedChildren = new ArrayList<Activity>();

  protected CompositeActivityImpl() {
    super();
  }

  /**
   * Constructor.
   *
   * @param nsContext
   */
  protected CompositeActivityImpl(NSContext nsContext) {
    super(nsContext);
  }


  public List<Activity> getChildren() {
    return Collections.unmodifiableList(_orderedChildren);
  }

  public void removeChild(Activity childToRemove) {
    _orderedChildren.remove(childToRemove);
  }

  public void addChild(Activity childToAdd) {
    _orderedChildren.add(childToAdd);
  }

  public void addChild(int idx, Activity childToAdd) {
    _orderedChildren.add(idx, childToAdd);
  }

}
