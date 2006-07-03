/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

import java.util.List;

/**
 * BOM representation of the BPEL <code>&lt;assign&gt;</code> activity.
 * The <code>&lt;assign&gt;</code> activity is simply a collection of
 * <code>&lt;copy&gt;</code> entries ({@link Copy}).
 */
public interface AssignActivity extends Activity {

  /**
   * Get the list of <code>&lt;copy&gt;</code> entries for this activity.
   *
   * @return copy entries
   */
  List<Copy> getCopies();

  /**
   * Append a <code>&lt;copy&gt;</code> entry to the list of
   * copy <code>&lt;copy&gt;</code> entries.
   */
  void addCopy(Copy copy);

  /**
   * Add a <code>&lt;copy&gt;</code> entry to the list of
   * copy <code>&lt;copy&gt;</code> entries.
   *
   * @param idx  position of new entry (starting at 0)
   * @param copy new copy entry
   */
  void addCopy(int idx, Copy copy);


}
