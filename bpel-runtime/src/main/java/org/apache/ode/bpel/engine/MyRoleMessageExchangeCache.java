package org.apache.ode.bpel.engine;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;

import javax.wsdl.Operation;

import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.o.OPartnerLink;

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

    private BpelProcess _process;

    MyRoleMessageExchangeCache(BpelProcess process) {
        _process = process;
    }
    
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
     * Retrieve a {@link MyRoleMessageExchangeImpl} from the cache, re-creating if necessary.
     * 
     * @param mexdao
     * @return
     */
    MyRoleMessageExchangeImpl get(MessageExchangeDAO mexdao) {
        synchronized (this) {
            WeakReference<MyRoleMessageExchangeImpl> ref = _cache.get(mexdao.getMessageExchangeId());
            MyRoleMessageExchangeImpl mex = ref == null ? null : ref.get();

            if (mex == null) {
                mex = _process.recreateMyRoleMex(mexdao);
                _cache.put(mexdao.getMessageExchangeId(), new WeakReference<MyRoleMessageExchangeImpl>(mex));
            }
                
            return mex;

        }

    }

    /**
     * Remove stale references.
     * 
     */
    private void cleanup() {
        for (Iterator<WeakReference<MyRoleMessageExchangeImpl>> i = _cache.values().iterator(); i.hasNext();) {
            WeakReference<MyRoleMessageExchangeImpl> ref = i.next();
            if (ref.get() == null)
                i.remove();
        }

        _inserts = 0;
    }
}
