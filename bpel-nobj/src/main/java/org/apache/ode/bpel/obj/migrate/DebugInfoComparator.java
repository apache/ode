package org.apache.ode.bpel.obj.migrate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.obj.DebugInfo;

public class DebugInfoComparator implements EqualityComparator{
    private static final Log __log = LogFactory.getLog(DebugInfoComparator.class);
    private EqualityVisitor visitor;
    public DebugInfoComparator(EqualityVisitor visitor){
    	this.visitor = visitor;
    }
    
	@Override
	public Boolean objectsEqual(Object obj1, Object obj2) {
		if (!(obj2 instanceof DebugInfo)){
			if (!visitor.logFalseThrough){
				__log.debug("Unequal in DebugInfo, object2 is not a DebugInfo");
			}
			return false;
		}
		DebugInfo o1 = (DebugInfo)obj1;
		DebugInfo o2 = (DebugInfo)obj2;
		boolean res =  o1.getStartLine() == o2.getStartLine() &&
				o1.getEndLine() == o2.getEndLine() &&
				o1.getSourceURI().equals(o2.getSourceURI());
		if (!res){
			if (!visitor.logFalseThrough){
				__log.info("Unequal in DebugInfo, " + o1 + " and " + o2);
			}
		}
		return res;
	}

	@Override
	public Boolean canHanle(Object obj) {
		return obj instanceof DebugInfo;
	}

}
