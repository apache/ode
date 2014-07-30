package org.apache.ode.bpel.obj.migrate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
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
public class EqualityVisitor extends AbstractObjectVisitor{
	private static final Log __log = LogFactory.getLog(ObjectTraverser.class);
    private List<EqualityComparator> comparators = new LinkedList<EqualityComparator>();
    /**stack that holds current visit path */
    private Stack<String> st = new Stack<String>();
    /**object that compared to */
    protected Object other;
    public boolean logFalseThrough = false;

	public EqualityVisitor(Object other){
		this.other = other;
		st.push(":" + other.getClass().getSimpleName());
	}

	@Override
	public Boolean visit(Object obj){
		if (!logFalseThrough){
			__log.debug("comparing Object " + obj.getClass() + "@" + System.identityHashCode(obj) + " " + obj + 
				" and " + other.getClass() + "@" + System.identityHashCode(other) + " " + other);
		}
		Boolean res =  (Boolean)super.visit(obj);
		return res;
	}
	@SuppressWarnings("rawtypes")
	@Override
	public Boolean visitMap(Object obj) {
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
						return false;
				}
			}

			st.pop();
			st.push(k1.toString() + ":" + o1.getClass().getSimpleName());
			
			Object pre = other;
			other = o2;
			Boolean e = (Boolean)traverse.traverseObject(o1);
			if (!e) {
				return false;
			}
			other = pre;
			st.pop();
		}
		return true;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Boolean visitSet(Object obj){
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
						"\n" + "cann't find" + o1);
				}
				return false;
			}
			st.pop();
		}
		return true;
	}
	
	private Object contains(Collection c, Object t1) {
		Iterator itor = c.iterator();
		Object t2;
		Object pre = other;
		logFalseThrough = true;
		while (itor.hasNext()){
			t2 = itor.next();
			other = t2;
			if ((Boolean)traverse.traverseObject(t1, false)) {
				logFalseThrough = false;
				return t2;
			}
		}
		traverse.getHtab().assign(t1);
		other = pre;
		logFalseThrough = false;
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Boolean visitCollection(Object obj) {
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
			Object pre = other;
			other = o2;
			Boolean e = (Boolean)traverse.traverseObject(o1);
			if (!e) {
				return false;
			}
			other = pre;
			st.pop();
		}
		return true;
	}

	@Override
	public Boolean visitArray(Object obj) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Boolean visitPojo(Object obj) {
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
			return equalityByReflection(obj);
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
	public Boolean equalityByReflection(Object obj) {
		//TODO if it's sufficient to just compare public fields?
		Field[] fields = obj.getClass().getFields();
		Field[] fields2 = other.getClass().getFields();
		if (! Arrays.equals(fields, fields2)){
			if (!logFalseThrough){
				__log.debug("Unequal: getFields() of two Object do not match " + st);
			}
			return false;
		}
		
		for (Field f : fields){
			if ((Modifier.TRANSIENT & f.getModifiers()) != 0){
				continue; //skip transient fields
			}
			try {
				st.push(f.getName()+ ":" + f.getType().getSimpleName());
				Object v1 = f.get(obj);
				Object v2 = f.get(other);
				if (v1 == null && v2 == null){
					continue;
				}
				if (v1 == null || v2 == null){
					if (!logFalseThrough){
						__log.debug("Unequal: one field is null" + st + ".\n When dealing with " 
							+ v1 + " and " + v2);
					}
					return false;
				}
				Object pre = other;
				other = v2;
				Boolean res = (Boolean)traverse.traverseObject(v1);
				if (!res){
					if (!logFalseThrough){
						__log.debug("Unequal:" + st + ".\n When dealing with " 
							+ v1 + " and " + v2);
					}
					return false;
				}
				other = pre;
				st.pop();
			} catch (Exception e) {
				//should not get here
				e.printStackTrace();
			}
		}
		return true;
	}
	
	public void setOther(Object other){
		this.other = other;
	}
	public void addCustomComparator(EqualityComparator oe){
		comparators.add(0, oe);
	}
	@Override
	public Boolean visited(Object obj){
		return true;
	}
    public Stack<String> getSt() {
		return st;
	}
}
