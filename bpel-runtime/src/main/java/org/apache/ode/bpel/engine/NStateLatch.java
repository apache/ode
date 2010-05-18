/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ode.bpel.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 */
public class NStateLatch {
    static final Log __log = LogFactory.getLog(NStateLatch.class);

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
                        } finally {
                            _transitioning = false;
                        }
                    _state = state;
                }
            }
        } finally {
            _depth ++;
            _lock.unlock();
        }
    }
    
    public void release(int state) {
        _lock.lock();
        try {
            
            if (_transitioning )
                throw new IllegalStateException("Manipulating latch from transition. ");

            if (_state != state)
                __log.error("Latch error, was releasing for state " + state + " but actually in " + _state);
            if (_depth <= 0)
                throw new IllegalStateException("Too many release() calls.");
            
            _depth --;
            
            if (_depth == 0)
                _depth0.signal();
        } finally {
            _lock.unlock();
        }
    }
    
    public int getDepth(int state) {
        return (_state == state ? _depth : 0);
    }
}
