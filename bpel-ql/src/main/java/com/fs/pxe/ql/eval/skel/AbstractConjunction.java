package com.fs.pxe.ql.eval.skel;

import java.util.Collection;

public abstract class AbstractConjunction<R, PARAMC> extends AbstractContainer implements
		ConjunctionEvaluator<R, PARAMC> {

	public AbstractConjunction(Collection<CommandEvaluator> childs) {
		super(childs);
	}

	
}
