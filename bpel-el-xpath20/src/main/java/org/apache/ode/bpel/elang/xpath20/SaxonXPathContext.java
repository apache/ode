/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.elang.xpath20;

import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import org.apache.ode.bpel.explang.EvaluationContext;

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
