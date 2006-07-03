/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.engine;

import com.fs.jacob.soup.ReplacementMap;
import com.fs.pxe.bpel.o.OBase;
import com.fs.pxe.bpel.o.OProcess;

/**
 * A JACOB {@link ReplacementMap} implementation that eliminates unnecessary serialization
 * of the (constant) compiled process model.
 */
class ReplacementMapImpl implements ReplacementMap {
  private OProcess _oprocess;

  ReplacementMapImpl(OProcess oprocess) {
    _oprocess = oprocess;
  }

  public boolean isReplacement(Object obj) {
    return obj instanceof BpelProcess.OBaseReplacementImpl;
  }

  public Object getOriginal(Object replacement) throws IllegalArgumentException {
    if (!(replacement instanceof BpelProcess.OBaseReplacementImpl))
      throw new IllegalArgumentException("Not OBaseReplacementObject!");
    return _oprocess.getChild(((BpelProcess.OBaseReplacementImpl)replacement)._id);
  }

  public Object getReplacement(Object original) throws IllegalArgumentException {
    if (!(original instanceof OBase))
      throw new IllegalArgumentException("Not OBase!");
    return new BpelProcess.OBaseReplacementImpl(((OBase)original).getId());
  }

  public boolean isReplaceable(Object obj) {
    return obj instanceof OBase;
  }

}
