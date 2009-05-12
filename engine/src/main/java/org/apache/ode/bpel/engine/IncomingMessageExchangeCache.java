package org.apache.ode.bpel.engine;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ode.bpel.dao.MessageExchangeDAO;

/**
 * Manage {@link IncomingMessageExchangeCache} object references.
 */
class IncomingMessageExchangeCache {

    private static final int CLEANUP_PERIOD = 20;

    private Map<String, WeakReference<MessageExchangeImpl>> _cache = new ConcurrentHashMap<String, WeakReference<MessageExchangeImpl>>();

    private int _inserts = 0;

    void put(MessageExchangeImpl mex) {
        ++_inserts;
        if (_inserts > CLEANUP_PERIOD) cleanup();

        WeakReference<MessageExchangeImpl> ref = _cache.get(mex.getMessageExchangeId());
        if (ref != null && ref.get() != null)
            throw new IllegalStateException("InternalError: duplicate myrolemex registration!");

        _cache.put(mex.getMessageExchangeId(), new WeakReference<MessageExchangeImpl>(mex));
    }

    /**
     * Retrieve a {@link MyRoleMessageExchangeImpl} from the cache, re-creating if necessary.
     * 
     * @param mexdao
     * @return
     */
    MessageExchangeImpl get(MessageExchangeDAO mexdao, ODEProcess process) {
        WeakReference<MessageExchangeImpl> ref = _cache.get(mexdao.getMessageExchangeId());
        MessageExchangeImpl mex = ref == null ? null : ref.get();

        if (mex == null) {
            mex = process.recreateIncomingMex(mexdao);
            _cache.put(mexdao.getMessageExchangeId(), new WeakReference<MessageExchangeImpl>(mex));
        }
        return mex;
    }

    /**
     * Remove stale references.
     * 
     */
    private void cleanup() {
        for (Iterator<WeakReference<MessageExchangeImpl>> i = _cache.values().iterator(); i.hasNext();) {
            WeakReference<MessageExchangeImpl> ref = i.next();
            if (ref.get() == null) i.remove();
        }
        _inserts = 0;
    }
}
