/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

import java.util.List;

/**
 * Base interface for BPEL composite activities <code>&lt;flow&gt;</code> and <code>&lt;sequence&gt;</code>.
 * This interface provides methods for manipulating an ordered list of child activities.
 */
public interface CompositeActivity extends Activity {

  /**
   * Get the (ordered) list of child activities.
   *
   * @return immutable list of child {@link Activity} objects
   */
  List<Activity> getChildren();

  /**
   * Remove a child from the list of child activities.
   *
   * @param childToRemove child {@link Activity} to remove
   */
  void removeChild(Activity childToRemove);

  /**
   * Add (append) a child to the list of child activities.
   *
   * @param childToAdd {@link Activity} to add to end of list
   */
  void addChild(Activity childToAdd);

  /**
   * Add a child to the list of child activities.
   *
   * @param idx        position (index)
   * @param childToAdd {@link Activity} to add
   */
  void addChild(int idx, Activity childToAdd);

}
