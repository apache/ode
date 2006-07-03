/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.o;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for active BPEL agents.
 */
public class OAgent extends OBase {
  /** Links entering this agent. */
  public final Set<OLink> incomingLinks = new HashSet<OLink>();

  /** Links exiting this agent. */
  public final Set<OLink> outgoingLinks = new HashSet<OLink>();

  /** Variables read from. */
  public final Set<OScope.Variable> variableRd = new HashSet<OScope.Variable>();

  /** Variables written to. */
  public final Set<OScope.Variable> variableWr = new HashSet<OScope.Variable>();

  /** The children of this agent. */
  public final Set<OAgent> nested = new HashSet<OAgent>();

  public OAgent(OProcess owner) {
    super(owner);
  }
}
