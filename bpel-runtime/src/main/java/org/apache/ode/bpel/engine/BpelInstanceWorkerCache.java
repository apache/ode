package org.apache.ode.bpel.engine;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * A cache of {@link BpelInstanceWorker} objects. 
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 *
 */
class BpelInstanceWorkerCache {
    private HashMap<Long, WeakReference<BpelInstanceWorker>> _cache = new HashMap<Long, WeakReference<BpelInstanceWorker>>();
    private ReferenceQueue<BpelInstanceWorker> _refQ = new ReferenceQueue<BpelInstanceWorker>();
    
    private ODEProcess _process;
    
    public BpelInstanceWorkerCache(ODEProcess process) {
        _process = process;
    }
    
    synchronized BpelInstanceWorker get(long iid) {
        expungeStaleEntries();
        WeakReference<BpelInstanceWorker> wref = _cache.get(iid);
        BpelInstanceWorker worker;
        
        // Case: not in cache.
        if (wref == null) {
            worker = new BpelInstanceWorker(_process, iid);
            wref = new WeakReference<BpelInstanceWorker>(worker,_refQ);
            _cache.put(iid, wref);
        } else {
            worker = wref.get();
            
            // Case: garbage collected
            if (worker == null) {
                worker = new BpelInstanceWorker(_process, iid);
                wref = new WeakReference<BpelInstanceWorker>(worker,_refQ);
                _cache.put(iid, wref); 
            }
        }

        return worker;
    }
    
    
    private void expungeStaleEntries() {
        Reference<? extends BpelInstanceWorker> x;
        while ((x=_refQ.poll()) != null){
            _cache.values().remove(x);
        }
    }
}
