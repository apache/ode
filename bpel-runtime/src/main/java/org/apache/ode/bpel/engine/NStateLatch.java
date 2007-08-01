package org.apache.ode.bpel.engine;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An N state synchronization latch useful for implementing hydration/dehydration. The
 * latch functions as follows. At any time, the latch is in one of N states and has a
 * count. Clients can "latch" and "release" the latch, which increments/decrements the 
 * count; however, when latching, a state must be specified. If the state does not match
 * the current state, the latch blocks until the count is zero. Essentially, the latch
 * can change state only when the count is zero. Every time the latch changes state an 
 * optional {@link Runnable} corresponding to the new state is executed. 
 * 
 * 
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 *
 */
public class NStateLatch {
    
    /** Current state. */
    private int _state = -1;

    /** Current depth (i.e. number of enter() calls) */
    private int _depth = 0;

    /** Action for state transition ?-->i */
    protected Runnable _transitions[];
    
    /** Synchronization lock .*/
    private Lock _lock; 
    
    /** _depth == 0 condition. */
    private Condition _depth0;
    
    private boolean _transitioning = false;

    /**
     * Constructor, the array of {@link Runnable}s defines the number of states and the transition 
     * actions.  
     * @param transitions action to perform when entering state x. 
     */
    public NStateLatch(Runnable [] transitions) {
        _transitions = transitions;
        _lock = new ReentrantLock();
        _depth0 = _lock.newCondition();
    }
    
    public void latch(int state) {
        if (state >= _transitions.length || state < 0)
            throw new IllegalArgumentException("Invalid state.");
        
        _lock.lock();
        try {

            if (_transitioning )
                throw new IllegalStateException("Manipulating latch from transition. ");
            
            if (_state != state) {
                // wait for the depth to become 0
                while (_depth != 0) 
                    _depth0.awaitUninterruptibly();
              
                if (_state != state) {
                    if (_transitions[state] != null) 
                        try {
                            _transitioning = true;
                            _transitions[state].run();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        } finally {
                            _transitioning = false;
                        }

                        
                    _state = state;
                    
                }
            }

            _depth ++;
            
            
        } finally {
            _lock.unlock();
        }
    }
    
    public void release(int state) {
        _lock.lock();
        try {
            
            if (_transitioning )
                throw new IllegalStateException("Manipulating latch from transition. ");

            if (_state != state)
                throw new IllegalStateException("Wrong state.");
            if (_depth <= 0)
                throw new IllegalStateException("Too many release() calls.");
            
            _depth --;
            
            if (_depth == 0)
                _depth0.signal();
        } finally {
            _lock.unlock();
        }
    }
    
    public boolean isLatched(int state) {
        return _state == state && _depth > 0;
    }
}
