/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.o;

import java.util.List;
import java.util.ArrayList;


/**
 * Compiled representation of the BPEL <code>&lt;sequence&gt;</code> activity.
 */
public class OSequence extends OActivity {
  static final long serialVersionUID = -1L  ;

  public final List<OActivity> sequence = new ArrayList<OActivity>();

  public OSequence(OProcess owner) {
    super(owner);
  }
}
