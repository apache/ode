package org.apache.ode.ql.eval.skel;

import java.util.Collection;

public interface CommandContainer {
	public Collection<CommandEvaluator> getChilds();
}
