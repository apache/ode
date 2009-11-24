package org.apache.ode.bpel.rapi;

import java.util.HashSet;
import java.util.Set;

public class PropagationRule {
	private Set<String> contexts = new HashSet<String>();
	private PartnerLink fromPL;
	
	public Set<String> getContexts() {
		return contexts;
	}
	public void setContexts(Set<String> contexts) {
		this.contexts = contexts;
	}
	public boolean isPropagateAll() {
		return contexts.contains("*");
	}
	public PartnerLink getFromPL() {
		return fromPL;
	}
	public void setFromPL(PartnerLink fromPL) {
		this.fromPL = fromPL;
	}
}
