/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime;

import com.fs.jacob.SynchChannel;
import com.fs.jacob.SynchML;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Serially activates a list of compensations in order.
 */
class ORDEREDCOMPENSATOR extends BpelAbstraction  {
  private static final long serialVersionUID = -3181661355085428370L;

  private static final Log __log = LogFactory.getLog(ORDEREDCOMPENSATOR.class);

  private List<CompensationHandler> _compensations;
  private SynchChannel _ret;

  public ORDEREDCOMPENSATOR(List<CompensationHandler> compensations, SynchChannel ret) {
    _compensations = compensations;
    _ret = ret;
  }

  public void self() {
    if (_compensations.isEmpty()) {
      _ret.ret();
    }
    else {
      SynchChannel r = newChannel(SynchChannel.class);
      CompensationHandler cdata = _compensations.remove(0);
      cdata.compChannel.compensate(r);
      object(new SynchML(r) {
        private static final long serialVersionUID = 7173916663479205420L;

        public void ret() {
          instance(ORDEREDCOMPENSATOR.this);
        }
      });
    }

  }

  protected Log log() {
    return __log;
  }

  public String toString() {
    return new StringBuffer("ORDEREDCOMPENSATOR(comps=")
            .append(_compensations)
            .append(")")
            .toString();
  }

}
