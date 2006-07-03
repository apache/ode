/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.Correlation;
import com.fs.utils.stl.CollectionsX;
import com.fs.utils.stl.MemberOfFunction;

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
