package org.apache.ode.bpel.obj.migrate;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.obj.DebugInfo;
import org.apache.ode.bpel.obj.ExtensibleImpl;
import org.apache.ode.bpel.obj.OProcess;

public class ExtensibeImplEqualityComp implements EqualityComparator{
    private static final Log __log = LogFactory.getLog(ExtensibeImplEqualityComp.class);
	private EqualityVisitor visitor;
	
	public ExtensibeImplEqualityComp() {
	}
	public ExtensibeImplEqualityComp(EqualityVisitor visitor){
		this.visitor = visitor;
	}

	@Override
	public Boolean objectsEqual(Object obj1, Object obj2) {
		if (obj2 == null) {
			if (!visitor.logFalseThrough){
				__log.debug("Unequal in ExtensibleImpl: Object2 is null. " +
					visitor.getSt());
		}
			return false;
		}
		ExtensibleImpl esi = (ExtensibleImpl)obj1;
		ExtensibleImpl esio = null;
		if (obj1.getClass() != obj2.getClass()){			
			if (!visitor.logFalseThrough){
				__log.debug("Unequal in ExtensibleImpl: Type mismatch. " + visitor.getSt() + 
					"\nmismatched type: " + obj1.getClass().getSimpleName() + 
					" and " + obj2.getClass().getSimpleName());
			}
			return false;
		}
		try{
			esio = (ExtensibleImpl)obj2;
		}catch(ClassCastException e){
			//won't get here
			return false;
		}
		Map m1 = new LinkedHashMap(esi.getFieldContainer());
		Map m2 = new LinkedHashMap(esio.getFieldContainer());
		dehydrate(m1);
		dehydrate(m2);
		if (obj1 instanceof OProcess){
			dehydrateOProcess(m1);
			dehydrateOProcess(m2);
		}
		if (obj1 instanceof DebugInfo){
			dehydrateDebugInfo(m1);
			dehydrateDebugInfo(m2);
		}
		visitor.setOther(m2);
		return (Boolean) visitor.getTraverse().traverseObject(m1);
	}
	private void dehydrateDebugInfo(Map m) {
		m.remove("description");
	}
	private void dehydrateOProcess(Map m) {
		m.remove("compileDate");
	}
	@SuppressWarnings("rawtypes")
	private void dehydrate(Map map) {
		if (map == null) return;
		Set<Entry> entries = map.entrySet();
		Iterator<Entry> itor = entries.iterator();
		while (itor.hasNext()){
			Entry entry = itor.next();
			if (entry.getValue() == null){
				itor.remove();
			}
		}
	}
	@Override
	public Boolean canHanle(Object obj) {
		return obj instanceof ExtensibleImpl;
	}
}
