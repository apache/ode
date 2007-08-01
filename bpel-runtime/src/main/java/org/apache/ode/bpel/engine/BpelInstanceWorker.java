package org.apache.ode.bpel.engine;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.runtime.PROCESS;

/**
 * Objects used for synchronizing the execution of instance-level work. All work on behalf of an instance is funneled to one of
 * these objects. For all practical purposes these are singletons, with the caveat that they expire as soon as all work for an
 * instance is complete so they may be recreated on-demand. The effect is that all work for an instance occurs in a single thread.
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 * 
 */
class BpelInstanceWorker implements Runnable {

    private static final Log __log = LogFactory.getLog(BpelInstanceWorker.class);

    final BpelProcess _process;

    final Long _iid;

    final Contexts _contexts;

    private boolean _running = false;

    private ArrayList<Runnable> _todoQueue = new ArrayList<Runnable>();

    private final ThreadLocal<Long> _activeInstance = new ThreadLocal<Long>();

    BpelInstanceWorker(BpelProcess process, Long iid) {
        _process = process;
        _iid = iid;
        _contexts = _process._contexts;
    }

    Long getIID() {
        return _iid;
    }

    /**
     * Add a task for this instance.
     * 
     * @param runnable
     */
    synchronized void enqueue(Runnable runnable) {
        _todoQueue.add(runnable);
        // We mayh need to reschedule this thread if we've dropped out of the end of the run() method.
        if (!_running) {
            _running = true;
            _process.scheduleRunnable(this);
        }
    }
 
    /**
     * Execute some work on behalf of the instance, but don't do it in the worker thread, instead do it in the calling thread. Why
     * bother? Well, sometimes we need to do some work in the current thread because it is associated with some transaction we'd
     * like to use, but we don't want to go through the suspend/resume BS. Ok, so why not just do the work directly? Well we want to
     * do the work as if it was occuring in our worker thread, so we have to block it for the duration of the action.
     * 
     * 
     * @param <T>
     *            parameterization of {@link Callable}
     * @param callable
     *            the thing to call
     * @return return value of the callble
     * @throws Exception
     *             forwarded from {@link Callable#call()}
     */
    synchronized <T> T execInCurrentThread(Callable<T> callable) throws Exception {
        final Semaphore ready = new Semaphore(0);
        final Semaphore finished = new Semaphore(0);
        enqueue(new Runnable() {
            public void run() {
                ready.release();
                try {
                    finished.acquire();
                } catch (InterruptedException ie) {
                    __log.error("Thread interrupted.", ie);
                    throw new BpelEngineException("Thread interrupted.", ie);
                }
            }
        });
        try {
            ready.acquire();
        } catch (InterruptedException ex) {
            __log.error("Thread interrupted.", ex);
            throw new BpelEngineException("Thread interrupted.", ex);
        }

        _activeInstance.set(_iid);
        try {
            return callable.call();
        } catch (Exception ex) {
            throw ex;
        } finally {
            finished.release();
            _activeInstance.set(null);
        }

    }

   

    /**
     * Implementation of the {@link Runnable} interface.
     */
    public void run() {
        _activeInstance.set(_iid);
        try {

            do {
                Runnable next;
                synchronized (this) {
                    if (_todoQueue.isEmpty()) {
                        // This is the only way to drop out of this method short of some disasterous error. This is
                        // important since we need to synchronize _running with _todoQueue state.
                        _running = false;
                        return;
                    }

                    next = _todoQueue.remove(0);
                }

                try {
                    next.run();
                } catch (Throwable t) {
                    __log.error("Unexpected error in instance thread.", t);
                }
            } while (true);
        } finally {
            _activeInstance.set(null);
        }
    }

    public String toString() {
        return "{BpelInstanceWorker: PID=" + _process.getPID() + " IID=" + _iid + "}";
    }

    public boolean isWorkerThread() {
        return _activeInstance.get() != null;
    }

}
