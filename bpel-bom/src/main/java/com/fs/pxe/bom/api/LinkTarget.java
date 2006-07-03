/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

/**
 * A representation of a BPEL link target. A link target is a tuple that joins
 * a link decleration (by reference) and an activity (by context).
 */
public interface LinkTarget extends BpelObject {
  /**
   * Get the activity that owns this link target.
   *
   * @return owner {@link Activity} object
   */
  Activity getActivity();

  /**
   * Get the name of the refernced link.
   *
   * @return link name
   */
  String getLinkName();

  /**
   * Set the name of the referenced link.
   *
   * @param linkName link name
   */
  void setLinkName(String linkName);

}
