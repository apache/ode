package com.fs.pxe.ql.eval.skel;

import java.util.Collection;

public abstract class AbstractContainer implements CommandContainer {
	protected final Collection<CommandEvaluator> childs;
	
	public AbstractContainer(Collection<CommandEvaluator> childs) {
		super();
		this.childs = childs;
	}

	public Collection<CommandEvaluator> getChilds() {
		return childs;
	}

}
