package org.apache.ode.bpel.obj.migrate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * compare two object for equality. default strategy:
 * for collections and maps, compare their contents
 * for POJO, 
 * 		if any custom equality comparator can handle it, then use it;
 * 		if it has an equals() method defined, use it.
 * 		or compare their non-transient accessible fields by reflection.
 * @author fangzhen
 *
 */
public class DeepEqualityHelper{

	private static final Log __log = LogFactory.getLog(ObjectTraverser.class);
    public boolean logFalseThrough = false;
    private Stack<String> st = new Stack<String>();
    
    private List<EqualityComparator> comparators = new LinkedList<EqualityComparator>();
    private Stack<Long> ongoing = new Stack<Long>();
    
    private Map<Long, Boolean> cache = new HashMap<Long, Boolean>();
    
    public boolean deepEquals(Object obj1, Object obj2){
  //  	__log.debug("comparing Objects: " + obj1 + " and " + obj2); //will cause too much log
    	Boolean c = cachedRes(obj1, obj2);
    	if (c != null) {
    		return c;
    	}
    	Long h12 = hash(obj1, obj2);
    	if (ongoing.contains(h12)) {
    		return true;
    	}
    	ongoing.push(h12);
    	
		boolean n;
		if (isMap(obj1)){
			n = visitMap(obj1, obj2);
		}else if (isSet(obj1)){
			n = visitSet(obj1, obj2);
		}else if (isCollection(obj1)){
			n = visitCollection(obj1, obj2);
		}else if (isArray(obj1)){
			n = visitArray(obj1, obj2);
		}else{
			n = visitPojo(obj1, obj2);
		}
		cacheRes(obj1, obj2, n);
		ongoing.pop();
    	return n;
    }
    
	private void cacheRes(Object obj1, Object obj2, Boolean n) {
		cache.put(hash(obj1, obj2), n);
	}

	private Boolean cachedRes(Object obj1, Object obj2) {
		return cache.get(hash(obj1, obj2));
	}

	public Boolean visitMap(Object obj, Object other) {
		if (obj == other) return true;
		if (other == null) {
			if (!logFalseThrough){
				__log.debug("Unequal in Map: Object2 is null. " + st);
			}
			return false;
		}
		Map m1 = (Map)obj;
		Map m2 = null;
		try{
			m2 = (Map)other;
		}catch (ClassCastException e){
			if (!logFalseThrough){
				__log.debug("Unequal in Map: Object2 is not a map, it's a" + other.getClass() + "\n" + st);
			}
			return false;
		}
		if (m1.size() != m2.size()) {
			if (!logFalseThrough){
				__log.debug("Unequal in Map: size mismatch. " + st + 
					"\n size: " + m1.size() + " and " + m2.size());
			}
			return false;
		}
		Set ks1 = m1.keySet();
		Set ks2 = m2.keySet();
		for (Object k1 : ks1){
			st.push(k1.toString());
			Object k2 = contains(ks2, k1);
			if (k2 == null){
				if (!logFalseThrough){
					__log.debug("Unequal in Map: cant find key. " + st + "\n missing key: " + k1);
				}
				st.pop();
				return false;
			}
			Object o1 = m1.get(k1);
			Object o2 = m2.get(k2);
			if (o1 == null){
				if (!(o2 == null)){
					if (!logFalseThrough){
						__log.debug("Unequal in Map: mismatch, one is null" + st + 
							"\n When dealing with " + o1 + " and " + o2);
					}	
					st.pop();
					return false;
				}
			}

			st.pop();
			st.push(k1.toString() + ":" + o1.getClass().getSimpleName());
			
			Boolean e = deepEquals(o1, o2);
			if (!e) {
				st.pop();
				return false;
			}
			st.pop();
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	public Boolean visitSet(Object obj, Object other){
		if (obj == other) return true;
		if (other == null) {
			if (!logFalseThrough){
				__log.debug("Unequal in Set, Object2 is null. " + st);
			}
			return false;
		}
		
		Collection c1 = (Collection)obj;
		Collection c2 = null;
		try {
			c2 = (Collection)other;
		}catch(ClassCastException e){
			if (!logFalseThrough){
				__log.debug("Unequal in Set: Object2 is not a Set, it's a" + other.getClass() + "\n" + st);
			}
			return false;
		}
		if (c1.size() != c2.size()) {
			if (!logFalseThrough){
				__log.debug("Unequal in Set: size mismatch. " + st + 
					"\n. sizes are " + c1.size() + " and " + c2.size());
			}
			return false;
		}
		Iterator i1 = c1.iterator();
		while (i1.hasNext()){
			Object o1 = i1.next();
			st.push(":" + o1.getClass().getSimpleName());
			if (contains(c2, o1) == null) {
				if (!logFalseThrough){
					__log.debug("Unequal in Set: Object mismatch. " + st + 
						"\n" + "cann't find " + o1);
				}
				st.pop();
				return false;
			}
			st.pop();
		}
		return true;
	}
	
	private Object contains(Collection c, Object t1) {
		Iterator itor = c.iterator();
		Object t2;
		logFalseThrough = true;
		while (itor.hasNext()){
			t2 = itor.next();
			if (deepEquals(t1, t2)) {
				logFalseThrough = false;
				return t2;
			}
		}
		logFalseThrough = false;
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Boolean visitCollection(Object obj, Object other) {
		if (obj == other) return true;
		if (other == null) {
			if (!logFalseThrough){
				__log.debug("Unequal in Collection, Object2 is null. " + st);
			}
			return false;
		}
		
		Collection c = (Collection)obj;
		Collection c2 = null;
		try {
			c2 = (Collection)other;
		}catch(ClassCastException e){
			if (!logFalseThrough){
				__log.debug("Unequal in Collection: Object2 is not a Collection, it's a" + other.getClass() + "\n" + st);
			}
			return false;
		}
		if (c.size() != c2.size()) {
			if (!logFalseThrough){
				__log.debug("Unequal in Collection: size mismatch. " + st + 
					"\n. sizes are " + c.size() + " and " + c2.size());
			}
			return false;
		}

		Iterator i1 = c.iterator();
		Iterator i2 = c2.iterator();
		while (i1.hasNext()){
			Object o1 = i1.next();
			Object o2 = i2.next();
			st.push(":" + o1.getClass().getSimpleName());
			Boolean e = deepEquals(o1, o2);
			if (!e) {
				st.pop();
				return false;
			}
			st.pop();
		}
		return true;
	}

	public Boolean visitArray(Object obj, Object other) {
		throw new UnsupportedOperationException();
	}

	public Boolean visitPojo(Object obj, Object other) {
		EqualityComparator customComp = getCustomComparator(obj);
		if (customComp != null){
			return customComp.objectsEqual(obj, other);
		}
		if (obj == other) return true;
		if (other == null) {
			if (!logFalseThrough){
				__log.debug("Unequal in POJO: Object2 is null." + st);
			}
			return false;
		}
		if (obj.getClass() != other.getClass()) {
			if(!logFalseThrough){
				__log.debug("Unequal in POJO: type mistach. " + st + 
						"\nmismatched types are: " + obj.getClass().getSimpleName() + 
						" and " + other.getClass().getSimpleName());
			}
			return false;
		}
		try{
			obj.getClass().getDeclaredMethod("equals", Object.class);
			boolean e = obj.equals(other);
			if (!e){
				if (!logFalseThrough){
					__log.debug("Unequal in POJO: not equal by equals() method" + st); 
				}
			}
			return e;
		}catch (NoSuchMethodException e){
			return equalityByReflection(obj, other);
		}
	}

	private EqualityComparator getCustomComparator(Object obj) {
		for (EqualityComparator c : comparators){
			if (c.canHanle(obj)){
				return c;
			}
		}
		return null;
	}
	public Boolean equalityByReflection(Object obj, Object other) {
		List<Field> fields = MigUtils.getAllFields(obj.getClass());
		List<Field> fields2 = MigUtils.getAllFields(other.getClass());
		if (!fields.equals(fields2)){
			if (!logFalseThrough){
				__log.debug("Unequal: getFields() of two Object do not match " + st);
			}
			return false;
		}
		
		for (Field f : fields){
			f.setAccessible(true);
			if (((Modifier.TRANSIENT | Modifier.STATIC) & f.getModifiers()) != 0){
				continue; //skip transient fields
			}
			try {
				Object v2 = f.get(other);
				Object v1 = f.get(obj);
				if (v1 == null && v2 == null){
					continue;
				}
				st.push(f.getName()+ ":" + f.getType().getSimpleName());				
				if (v1 == null || v2 == null){
					if (!logFalseThrough){
						__log.debug("Unequal: one field is null" + st + ".\n When dealing with " 
							+ v1 + " and " + v2);
					}
					st.pop();
					return false;
				}
				Boolean res = deepEquals(v1, v2);
				if (!res){
					st.pop();
					return false;
				}
				st.pop();
			} catch (Exception e) {
				//should not get here
				e.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * determine if obj is collections that order doesn't matter.
	 * @param obj
	 * @return
	 */
	protected boolean isSet(Object obj) {
		return obj instanceof Set;
	}

	protected boolean isArray(Object old) {
		return old.getClass().isArray();
	}

	/**
	 * determine if obj is collections that order does matter.
	 * @param obj
	 * @return
	 */
	protected boolean isCollection(Object old) {
		return (old instanceof Collection) && !isSet(old);
	}

	protected boolean isMap(Object old) {
		return old instanceof Map;
	}

	private Long hash(Object obj1, Object obj2) {
		int h1 = System.identityHashCode(obj1);
		int h2 = System.identityHashCode(obj2);
		return ((long)h1) << 32 | h2;
	}
	
	public void addCustomComparator(EqualityComparator oe){
		comparators.add(0, oe);
		oe.setDeepEquality(this);
	}
    public Stack<String> getSt() {
		return st;
	}
}
