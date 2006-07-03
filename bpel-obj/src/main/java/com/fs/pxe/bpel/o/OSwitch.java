/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.o;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Compiled representation of a BPEL <code>&lt;switch&gt;</code>. The
 * BPEL compiler generates instances of this class.
 */
public class OSwitch extends OActivity {
  static final long serialVersionUID = -1L  ;

  /**
   * The cases declared within the <code>&lt;switch&gt;</code> actvity.
   */
  private final List<OCase> _cases = new ArrayList<OCase> ();

  public OSwitch(OProcess owner) {
    super(owner);
  }

  public void addCase(OCase acase) {
    _cases.add(acase);
  }

  public List<OCase> getCases() {
    return Collections.unmodifiableList(_cases);
  }

  public static class OCase extends OBase {
    private static final long serialVersionUID = 1L;
		public OExpression expression;
    public OActivity activity;

    public OCase(OProcess owner) {
      super(owner);
    }
  }
}
