package org.apache.ode.bpel.obj.migrate;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.ode.bpel.obj.migrate.ObjectTraverser.HandleTable;

public abstract class AbstractObjectVisitor implements ObjectVisitor{
	protected ReplaceTable rtab = new ReplaceTable(1000, 0.8f);
	protected ObjectTraverser traverse;
	protected Map<Class, ObjectVisitor>  visitors = new HashMap<Class, ObjectVisitor>();
	
	@Override
	public Object visited(Object obj) {
		if (visitors.get(obj.getClass()) != null){
			return visitors.get(obj.getClass()).visited(obj);
		}
		return rtab.lookup(obj);
	}
	
	@Override
	public ObjectTraverser getTraverse(){
		return traverse;
	}
	@Override
	public Object visit(Object obj) {
		ObjectVisitor customVisitor = visitors.get(obj.getClass());
		if (customVisitor != null){
			return customVisitor.visit(obj);
		}
		Object n;
		if (isMap(obj)){
			n = visitMap(obj);
		}else if (isSet(obj)){
			n = visitSet(obj);
		}
		else if (isCollection(obj)){
			n = visitCollection(obj);
		}else if (isArray(obj)){
			n = visitArray(obj);
		}else{
			n = visitPojo(obj);
		}
		rtab.assign(obj, n);
		return n;
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
	@Override
	public void setTraverse(ObjectTraverser traverseObject) {
		this.traverse = traverseObject;		
	}
	
	public void addCustomVisitor(Class cls, ObjectVisitor visitor){
		visitors.put(cls, visitor);
	}
	public Object visitMap(Object obj) {
		Map m = (Map)obj;
		Set<Entry> entries = m.entrySet();
		for (Entry e : entries){
			traverse.traverseObject(e.getKey());
			traverse.traverseObject(e.getValue());
		}
		return null;
	}

	public Object visitCollection(Object obj) {
		Collection c = (Collection)obj;
		for (Object item : c){
			traverse.traverseObject(item);
		}
		return null;
	}

	public Object visitArray(Object obj) {
		int len = Array.getLength(obj);
		int i;
		for (i = 0; i < len; i++){
			traverse.traverseObject(Array.get(obj, i));
		}
		return null;
	}
	
	public Object visitSet(Object obj) {
		//nothing to do
		return null;
	}

	public abstract Object visitPojo(Object obj);	
	/**
     * Lightweight identity hash table which maps objects to replacement
     * objects.
     */   
    public static class ReplaceTable {

        /* maps object -> index */
        private final HandleTable htab;
        /* maps index -> replacement object */
        private Object[] reps;

        /**
         * Creates new ReplaceTable with given capacity and load factor.
         */
        ReplaceTable(int initialCapacity, float loadFactor) {
            htab = new HandleTable(initialCapacity, loadFactor);
            reps = new Object[initialCapacity];
        }

        /**
         * Enters mapping from object to replacement object.
         */
        void assign(Object obj, Object rep) {
            int index = htab.assign(obj);
            while (index >= reps.length) {
                grow();
            }
            reps[index] = rep;
        }

        /**
         * Looks up and returns replacement for given object.  If no
         * replacement is found, returns the lookup object itself.
         */
        Object lookup(Object obj) {
            int index = htab.lookup(obj);
            return (index >= 0) ? reps[index] : obj;
        }

        /**
         * Resets table to its initial (empty) state.
         */
        void clear() {
            Arrays.fill(reps, 0, htab.size(), null);
            htab.clear();
        }

        /**
         * Returns the number of mappings currently in table.
         */
        int size() {
            return htab.size();
        }

        /**
         * Increases table capacity.
         */
        private void grow() {
            Object[] newReps = new Object[(reps.length << 1) + 1];
            System.arraycopy(reps, 0, newReps, 0, reps.length);
            reps = newReps;
        }
    }

}
