/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.AssignActivity;
import com.fs.pxe.bom.api.Copy;
import com.fs.utils.NSContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Normalized representation of the BPEL <code>assign</code> activity.
 * Maintains a list of {@link CopyImpl} objects that describe the copy actions to
 * be performed as part of the assign.
 */
public class AssignActivityImpl extends ActivityImpl implements AssignActivity {

  private static final long serialVersionUID = -1L;

  private ArrayList<Copy> _copies = new ArrayList<Copy>();

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public AssignActivityImpl(NSContext nsContext) {
    super(nsContext);

  }

  public AssignActivityImpl() {
    super();
  }

  public List<Copy> getCopies() {
    return Collections.unmodifiableList(_copies);
  }

  /**
   * @see com.fs.pxe.bom.impl.nodes.ActivityImpl#getType()
   */
  public String getType() {
    return "assign";
  }

  public void addCopy(Copy copy) {
    _copies.add(copy);
  }

  public void addCopy(int idx, Copy copy) {
    _copies.add(idx, copy);
  }
}
