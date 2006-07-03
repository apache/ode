/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.elang.xpath20;

import com.fs.pxe.bpel.elang.xpath10.o.OXPath10Expression;
import com.fs.pxe.bpel.explang.EvaluationContext;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.om.Item;

public class SaxonXPathContext extends XPathContextMajor{
	
  public final OXPath10Expression oxpath;
  public final EvaluationContext evalCtx;

	public SaxonXPathContext(OXPath10Expression expr, EvaluationContext ctx, Item item, Configuration config) {
    super(item, config);
    oxpath = expr;
    evalCtx = ctx;
	}
  
}
