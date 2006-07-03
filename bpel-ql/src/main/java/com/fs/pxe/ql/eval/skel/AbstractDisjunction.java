package com.fs.pxe.ql.eval.skel;

import java.util.Collection;

public abstract class AbstractDisjunction<R, PARAMC> extends AbstractContainer implements
		DisjunctionEvaluator<R, PARAMC> {

	public AbstractDisjunction(Collection<CommandEvaluator> childs) {
		super(childs);
	}

	
}
