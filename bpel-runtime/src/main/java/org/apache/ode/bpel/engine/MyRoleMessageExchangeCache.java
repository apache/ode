package org.apache.ode.bpel.engine;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ode.bpel.dao.MessageExchangeDAO;

/**
 * Manage {@link MyRoleMessageExchangeImpl} object references.
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 * 
 */
class MyRoleMessageExchangeCache {

    private static final int CLEANUP_PERIOD = 20;

    private Map<String, WeakReference<MyRoleMessageExchangeImpl>> _cache = new ConcurrentHashMap<String, WeakReference<MyRoleMessageExchangeImpl>>();

    private int _inserts = 0;

    void put(MyRoleMessageExchangeImpl mex) {
        ++_inserts;
        if (_inserts > CLEANUP_PERIOD) cleanup();

        WeakReference<MyRoleMessageExchangeImpl> ref = _cache.get(mex.getMessageExchangeId());
        if (ref != null && ref.get() != null)
            throw new IllegalStateException("InternalError: duplicate myrolemex registration!");

        _cache.put(mex.getMessageExchangeId(), new WeakReference<MyRoleMessageExchangeImpl>(mex));
    }

    /**
     * Retrieve a {@link MyRoleMessageExchangeImpl} from the cache, re-creating if necessary.
     * 
     * @param mexdao
     * @return
     */
    MyRoleMessageExchangeImpl get(MessageExchangeDAO mexdao, ODEProcess process) {
        WeakReference<MyRoleMessageExchangeImpl> ref = _cache.get(mexdao.getMessageExchangeId());
        MyRoleMessageExchangeImpl mex = ref == null ? null : ref.get();

        if (mex == null) {
            mex = process.recreateMyRoleMex(mexdao);
            _cache.put(mexdao.getMessageExchangeId(), new WeakReference<MyRoleMessageExchangeImpl>(mex));
        }
        return mex;
    }

    /**
     * Remove stale references.
     * 
     */
    private void cleanup() {
        for (Iterator<WeakReference<MyRoleMessageExchangeImpl>> i = _cache.values().iterator(); i.hasNext();) {
            WeakReference<MyRoleMessageExchangeImpl> ref = i.next();
            if (ref.get() == null) i.remove();
        }
        _inserts = 0;
    }
}
