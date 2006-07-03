package com.fs.pxe.ql.eval.skel;

import java.util.Collection;

public interface CommandContainer {
	public Collection<CommandEvaluator> getChilds();
}
