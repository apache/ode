/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.o;

import java.util.HashMap;
import java.util.Map;

/**
 * Compiled representation of an expression language dependency.
 */
public class OExpressionLanguage extends OBase {
  private static final long serialVersionUID = 1L;
	public String expressionLanguageUri;
  public final Map<String,String> properties = new HashMap<String,String>();

  public OExpressionLanguage(OProcess owner, Map<String,String> properties) {
    super(owner);
    if (properties != null)
      this.properties.putAll(properties);
  }
}
