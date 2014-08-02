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
	private DeepEqualityHelper deepEquality;
	public ExtensibeImplEqualityComp() {
	}

	@Override
	public Boolean objectsEqual(Object obj1, Object obj2) {
		if (obj2 == null) {
			if (!deepEquality.logFalseThrough){
				__log.debug("Unequal in ExtensibleImpl: Object2 is null. " +
					deepEquality.getSt());
		}
			return false;
		}
		ExtensibleImpl esi = (ExtensibleImpl)obj1;
		ExtensibleImpl esio = null;
		if (obj1.getClass() != obj2.getClass()){			
			if (!deepEquality.logFalseThrough){
				__log.debug("Unequal in ExtensibleImpl: Type mismatch. " + deepEquality.getSt() + 
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
		if (obj1 instanceof DebugInfo){
			boolean r = obj1.equals(obj2);
			if (!r){
				if(!deepEquality.logFalseThrough){
					__log.debug("Unequal in ExtensibleImpl: DebugInfo unequal." + deepEquality.getSt());
				}
			}
			return r;
		}
		Map m1 = new LinkedHashMap(esi.getFieldContainer());
		Map m2 = new LinkedHashMap(esio.getFieldContainer());
		dehydrate(m1);
		dehydrate(m2);
		if (obj1 instanceof OProcess){
			dehydrateOProcess(m1);
			dehydrateOProcess(m2);
		}
		return (Boolean) deepEquality.deepEquals(m1, m2);
	}
	private void dehydrateOProcess(Map m) {
		m.remove("compileDate");
		m.remove("namespaceContext");
	}
	@SuppressWarnings("rawtypes")
	private void dehydrate(Map map) {
		if (map == null) return;
		map.remove("originalVersion");
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

	@Override
	public void setDeepEquality(DeepEqualityHelper deepEquality) {
		this.deepEquality = deepEquality;		
	}
}
