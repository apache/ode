/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Correlation;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for managing corrations.
 */
class CorrelationHelperImpl {
  private final ArrayList<Correlation> _correlations = new ArrayList<Correlation>();

  public void addCorrelation(Correlation correlation) {
    _correlations.add(correlation);
  }

  public List<Correlation> getCorrelations(final short patternMask) {
    List<Correlation> retVal = new ArrayList<Correlation>(_correlations);
    CollectionsX.remove_if(retVal, new MemberOfFunction<Correlation>() {
      public boolean isMember(Correlation c) {
        return ((c.getPattern() & patternMask) == 0);
      }
    });
    return retVal;
  }

  public List<Correlation> getCorrelations() {
    return _correlations;
  }

}
