package org.apache.ode.bpel.engine;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Manage {@link MyRoleMessageExchangeImpl} object references. 
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 *
 */
class MyRoleMessageExchangeCache {
    
    private static final int CLEANUP_PERIOD = 20;

    private HashMap<String, WeakReference<MyRoleMessageExchangeImpl>> _cache = new HashMap<String, WeakReference<MyRoleMessageExchangeImpl>>();

    private int _inserts = 0;
    
    void put(MyRoleMessageExchangeImpl mex) {
        synchronized (this) {
            ++_inserts;
            if (_inserts > CLEANUP_PERIOD) {
                cleanup();
            }
                
            WeakReference<MyRoleMessageExchangeImpl> ref = _cache.get(mex.getMessageExchangeId());
            if (ref != null && ref.get() != null)
                throw new IllegalStateException("InternalError: duplicate myrolemex registration!");
            
            _cache.put(mex.getMessageExchangeId(), new WeakReference<MyRoleMessageExchangeImpl>(mex));
        }
    }
    
    /**
     * Attempt to retrieve a {@link MyRoleMessageExchangeImpl} for the given identifier.
     * @param mexId
     * @return
     */
    MyRoleMessageExchangeImpl get(String mexId) {
        synchronized(this) {
            WeakReference<MyRoleMessageExchangeImpl> ref = _cache.get(mexId);
            if (ref == null)
                return null;
            MyRoleMessageExchangeImpl mex = ref.get();
            if (mex == null)
                _cache.remove(mexId);
            return mex;
        
        }

    }

    /**
     * Remove stale references.
     *
     */
    void cleanup() {
        synchronized(this){
            for (Iterator<WeakReference<MyRoleMessageExchangeImpl>> i = _cache.values().iterator(); i.hasNext(); ) {
                WeakReference<MyRoleMessageExchangeImpl> ref = i.next();
                if (ref.get() == null)
                    i.remove();
            }
            
            _inserts = 0;
        }
    }
}
