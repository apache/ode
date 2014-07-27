package org.apache.ode.bpel.obj.migrate;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.obj.ExtensibleImpl;

public class OmOld2new {
    private static final Log __log = LogFactory.getLog(OmOld2new.class);
    
	private static Map<String, String> beanPkgMap = new HashMap<String, String>();
	static{
		beanPkgMap.put("org.apache.ode.bpel.o", "org.apache.ode.bpel.obj");
		beanPkgMap.put("org.apache.ode.bpel.elang.xpath10.o", "org.apache.ode.bpel.elang.xpath10.obj");
		beanPkgMap.put("org.apache.ode.bpel.elang.xpath20.o", "org.apache.ode.bpel.elang.xpath20.obj");
		beanPkgMap.put("org.apache.ode.bpel.elang.xquery10.o", "org.apache.ode.bpel.elang.xquery10.obj");
	}
	private HandleTable htab = new HandleTable(1000, 0.8f);
	private ReplaceTable rtab = new ReplaceTable(1000, 0.8f);
	
	public Object migrateFrom(Object old){
		__log.debug("migrating object: " + old.getClass() + "@" + System.identityHashCode(old));
		if (old == null) return null;
		if (htab.lookup(old) != -1){
			return rtab.lookup(old);
		}
		htab.assign(old);
		Object n;
		if (isOmodelBean(old)){
			n = constructNewOm(old);
		}else if (isMap(old)){
			n = constructNewMap(old);
		}else if (isCollection(old)){
			n = constructNewCollection(old);
		}else if (isArray(old)){
			n = constructNewArray(old);
		}else{
			n = old;
		}
		rtab.assign(old, n);
		__log.debug("Assigned object " + old.getClass() + "@" + System.identityHashCode(old));
		return n;
	}
	
	private Object constructNewArray(Object old) {
		throw new UnsupportedOperationException("Create new Array is unsupported");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object constructNewCollection(Object old) {
		Collection o = (Collection) old;
		try {
			Collection n = o.getClass().newInstance();
			for (Object obj : o){
				n.add(migrateFrom(obj));
			}
			return n;
		} catch (Exception e){
			//should not get here
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object constructNewMap(Object old) {
		Map o = (Map) old;
		try{
			Map n = o.getClass().newInstance();
			Set<Entry> entries = o.entrySet();
			for (Entry e : entries){
				n.put(migrateFrom(e.getKey()), migrateFrom(e.getValue()));
			}
			return n;
		}catch (Exception e){
			//should not get here
			e.printStackTrace();			
		}
		return null;
	}

	private boolean isArray(Object old) {
		return old.getClass().isArray();
	}

	private boolean isCollection(Object old) {
		return old instanceof Collection;
	}

	private boolean isMap(Object old) {
		return old instanceof Map;
	}

	/**
	 * construct new omodel instances from old ones. Assume <code>old</code> is an old OmodelBean
	 * @param old
	 * @return
	 */
	private Object constructNewOm(Object old) {
		Object tn = initiateNew(old);
		assert tn instanceof ExtensibleImpl;
		ExtensibleImpl n = (ExtensibleImpl) tn;
		Field[] fields = old.getClass().getFields();
		Map<String, Object> fieldMap = n.getFieldContainer();
		for (Field f : fields){
			try{
				String fname = f.getName();
				Object fvalue = f.get(old);
				if (fvalue != null){
					fieldMap.put(fname, migrateFrom(fvalue));
				}
			} catch (Exception e) {
				RuntimeException rte = new RuntimeException("Error when try to construct corresponding new Omodel class from old one:"
						+old.getClass() + "; Failed on field:" + f.getName());
				rte.initCause(e);
				throw rte;
			}
		}
		return null;
	}
	private Object initiateNew(Object old) {
		String clsName = old.getClass().getName();
		String qcls = clsName.replace(".o.", ".obj.");
		try {
			return Class.forName(qcls).newInstance();
		} catch (Exception e) {
			RuntimeException rte = new RuntimeException("Error when try to initiate corresponding new Omodel class of old one:"+old.getClass());
			rte.initCause(e);
			throw rte;
		}
	}

	private boolean isOmodelBean(Object old){
		Class<?> cls = old.getClass();
		if (beanPkgMap.containsKey(cls.getPackage().getName()) && !cls.getSimpleName().equals("Serializer")){
			return true;
		}
		return false;
	}
	
	 /**
	  * Stole from openjdk OOS
     * Lightweight identity hash table which maps objects to integer handles,
     * assigned in ascending order.
     */
    private static class HandleTable {

        /* number of mappings in table/next available handle */
        private int size;
        /* size threshold determining when to expand hash spine */
        private int threshold;
        /* factor for computing size threshold */
        private final float loadFactor;
        /* maps hash value -> candidate handle value */
        private int[] spine;
        /* maps handle value -> next candidate handle value */
        private int[] next;
        /* maps handle value -> associated object */
        private Object[] objs;

        /**
         * Creates new HandleTable with given capacity and load factor.
         */
        HandleTable(int initialCapacity, float loadFactor) {
            this.loadFactor = loadFactor;
            spine = new int[initialCapacity];
            next = new int[initialCapacity];
            objs = new Object[initialCapacity];
            threshold = (int) (initialCapacity * loadFactor);
            clear();
        }

        /**
         * Assigns next available handle to given object, and returns handle
         * value.  Handles are assigned in ascending order starting at 0.
         */
        int assign(Object obj) {
            if (size >= next.length) {
                growEntries();
            }
            if (size >= threshold) {
                growSpine();
            }
            insert(obj, size);
            return size++;
        }

        /**
         * Looks up and returns handle associated with given object, or -1 if
         * no mapping found.
         */
        int lookup(Object obj) {
            if (size == 0) {
                return -1;
            }
            int index = hash(obj) % spine.length;
            for (int i = spine[index]; i >= 0; i = next[i]) {
                if (objs[i] == obj) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * Resets table to its initial (empty) state.
         */
        void clear() {
            Arrays.fill(spine, -1);
            Arrays.fill(objs, 0, size, null);
            size = 0;
        }

        /**
         * Returns the number of mappings currently in table.
         */
        int size() {
            return size;
        }

        /**
         * Inserts mapping object -> handle mapping into table.  Assumes table
         * is large enough to accommodate new mapping.
         */
        private void insert(Object obj, int handle) {
            int index = hash(obj) % spine.length;
            objs[handle] = obj;
            next[handle] = spine[index];
            spine[index] = handle;
        }

        /**
         * Expands the hash "spine" -- equivalent to increasing the number of
         * buckets in a conventional hash table.
         */
        private void growSpine() {
            spine = new int[(spine.length << 1) + 1];
            threshold = (int) (spine.length * loadFactor);
            Arrays.fill(spine, -1);
            for (int i = 0; i < size; i++) {
                insert(objs[i], i);
            }
        }

        /**
         * Increases hash table capacity by lengthening entry arrays.
         */
        private void growEntries() {
            int newLength = (next.length << 1) + 1;
            int[] newNext = new int[newLength];
            System.arraycopy(next, 0, newNext, 0, size);
            next = newNext;

            Object[] newObjs = new Object[newLength];
            System.arraycopy(objs, 0, newObjs, 0, size);
            objs = newObjs;
        }

        /**
         * Returns hash value for given object.
         */
        private int hash(Object obj) {
            return System.identityHashCode(obj) & 0x7FFFFFFF;
        }
    }
    
    /**
     * Lightweight identity hash table which maps objects to replacement
     * objects.
     */   
    private static class ReplaceTable {

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
