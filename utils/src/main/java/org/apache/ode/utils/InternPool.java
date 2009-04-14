package org.apache.ode.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.collections.map.MultiKeyMap;


/**
 * A key value based cache that internalizes immutable values 
 * so that they can be shared across various BPEL objects. 
 *
  */
public class InternPool {
    private static MultiKeyMap cachedValues = MultiKeyMap.decorate(new LRUMap());
    private static Set<String> cachedBlocks = Collections.synchronizedSet(new HashSet<String>());

    /**
     * Creates a new KeyValueCache object.
     */
    protected InternPool() {
    }

    /**
     * Runs the given block in the context of a cache.
     * If you do not run your block this way, the caching
     * mechanism will be disabled.    
     *
     * @param block block 
     */
    public static void runBlock(InternableBlock block) {
        String processId = getProcessId();
        cachedBlocks.add(processId);
        block.run();
        cachedBlocks.remove(processId);
        clearAll(processId);
    }

    /**
     * Returns an internalized value if it already exists in the cache
     *
     * @param value value 
     *
     * @return the internalized value
     */
    public static Object intern(Object key, Object value) {
        String processId = getProcessId();

        if (!cachedBlocks.contains(processId)) {
        	return value;
        }
        
        synchronized (cachedValues) {
            List values = (List) cachedValues.get(processId, key);
            if (values == null) {
            	cachedValues.put(processId, key, (values = new ArrayList()));
            }

            Object intern;
            if (values.contains(value)) {
            	intern = values.get(values.indexOf(value));
            } else {
            	values.add(intern = value);            	
            }

            return intern;
        }
    }

    /**
     * Clears all the values corresponding to the given process
     *
     * @param processId processId 
     */
    protected static void clearAll(String processId) {
        synchronized (cachedValues) {
            cachedValues.remove(processId);
        }
    }

    /**
     * Returns the current thread id as the process id.
     *
     * @return the "process id"
     */
    private static String getProcessId() {
        return String.valueOf(Thread.currentThread().getId());
    }

    /**
     * An interface that clients should implement to run their
     * blocks of code in the context of this caching mechanism. 
     */
    public interface InternableBlock {
        /**
         * The block to run
         */
        public void run();
    }
}
