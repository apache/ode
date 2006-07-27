/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.o;

import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;

import java.util.HashSet;
import java.util.Set;


/**
 */
public class OFlow extends OActivity {
  static final long serialVersionUID = -1L  ;

  /** Links delcared within this activity. */
  public final Set<OLink> localLinks = new HashSet<OLink>();

  public final Set<OActivity> parallelActivities = new HashSet<OActivity>();

  public OFlow(OProcess owner) {
    super(owner);
  }

  public OLink getLocalLink(final String linkName) {
    return CollectionsX.find_if(localLinks, new MemberOfFunction<OLink>() {
      public boolean isMember(OLink o) {
        return o.name.equals(linkName);
      }
    });
  }

}
