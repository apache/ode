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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EqualityVisitor extends AbstractObjectVisitor{
    private static final Log __log = LogFactory.getLog(TraverseObject.class);
    private List<EqualityComparator> comparators = new LinkedList<EqualityComparator>();
    private List<String> falseChain = new LinkedList<String>();
    
    protected Object other;
    
    public EqualityVisitor(){
    	
    }
	public EqualityVisitor(Object other){
		this.other = other;
	}

	public void setOther(Object other){
		this.other = other;
	}
	public void addCustomComparator(EqualityComparator oe){
		comparators.add(0, oe);
	}
	public List<String> getFalseChain(){
		return falseChain;
	}
	@Override
	public Boolean visited(Object obj){
		return true;
	}
	@Override
	public Boolean visit(Object obj){
		__log.debug("comparing Object " + obj.getClass() + "@" + System.identityHashCode(obj) + " " + obj + 
				" and " + other.getClass() + "@" + System.identityHashCode(other) + " " + other);
		Boolean res =  (Boolean)super.visit(obj);
		if (!res){
			falseChain.add(0, obj.getClass().getSimpleName());
		}
		return res;
	}
	@SuppressWarnings("rawtypes")
	@Override
	public Boolean visitMap(Object obj) {
		if (obj == other) return true;
		if (other == null) return false;
		Map m1 = (Map)obj;
		Map m2 = null;
		try{
			m2 = (Map)other;
		}catch (ClassCastException e){
			return false;
		}
		if (m1.size() != m2.size()) return false;
		Set ks1 = m1.keySet();
		Set ks2 = m2.keySet();
		for (Object k1 : ks1){
			Object k2 = contains(ks2, k1);
			if (k2 == null){
				return false;
			}
			Object o1 = m1.get(k1);
			Object o2 = m2.get(k2);
			if (o1 == null){
				if (!(o2 == null)){
					return false;
				}
			}
			Object pre = other;
			other = o2;
			Boolean e = (Boolean)traverse.traverseObject(o1);
			if (!e) return false;
			other = pre;
		}
		return true;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Boolean visitSet(Object obj){
		if (obj == other) return true;
		if (other == null) return false;
		
		Collection c1 = (Collection)obj;
		Collection c2 = null;
		try {
			c2 = (Collection)other;
		}catch(ClassCastException e){
			return false;
		}
		if (c1.size() != c2.size()) return false;
		Iterator i1 = c1.iterator();
		while (i1.hasNext()){
			if (contains(c2, i1.next()) == null) return false;
		}
		return true;
	}
	
	private Object contains(Collection c, Object t1) {
		Iterator itor = c.iterator();
		Object t2;
		Object pre = other;
		while (itor.hasNext()){
			t2 = itor.next();
			other = t2;
			if ((Boolean)traverse.traverseObject(t1, false)) {
				return t2;
			}
		}
		traverse.getHtab().assign(t1);
		other = pre;
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Boolean visitCollection(Object obj) {
		if (obj == other) return true;
		if (other == null) return false;
		
		Collection c = (Collection)obj;
		Collection c2 = null;
		try {
			c2 = (Collection)other;
		}catch(ClassCastException e){
			return false;
		}
		if (c.size() != c2.size()) return false;

		Iterator i1 = c.iterator();
		Iterator i2 = c2.iterator();
		while (i1.hasNext()){
			Object o1 = i1.next();
			Object o2 = i2.next();
			Object pre = other;
			other = o2;
			Boolean e = (Boolean)traverse.traverseObject(o1);
			if (!e) return false;
			other = pre;
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
		if (other == null) return false;
		if (obj.getClass() != other.getClass()) return false;
		try{
			obj.getClass().getDeclaredMethod("equals", Object.class);
			return obj.equals(other);
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
			return false;
		}
		
		for (Field f : fields){
			if ((Modifier.TRANSIENT & f.getModifiers()) != 0){
				continue; //skip transient fields
			}
			try {
				Object v1 = f.get(obj);
				Object v2 = f.get(other);
				if (v1 == null && v2 == null){
					continue;
				}
				if (v1 == null || v2 == null){
					return false;
				}
				Object pre = other;
				other = v2;
				Boolean res = (Boolean)traverse.traverseObject(v1);
				if (!res){
					falseChain.add(f.getName());
					return false;
				}
				other = pre;
			} catch (Exception e) {
				//should not get here
				e.printStackTrace();
			}
		}
		return true;
	}

}
