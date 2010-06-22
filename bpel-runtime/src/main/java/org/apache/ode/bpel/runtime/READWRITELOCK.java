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
package org.apache.ode.bpel.runtime;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.ode.bpel.runtime.channels.ReadWriteLockChannel;
import org.apache.ode.bpel.runtime.channels.ReadWriteLockChannelListener;
import org.apache.ode.jacob.JacobRunnable;
import org.apache.ode.jacob.SynchChannel;

/**
 * A fair READ-WRITE lock.
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 * 
 */
public class READWRITELOCK extends JacobRunnable {

    private static final long serialVersionUID = -7415586067226921615L;

    private static enum Status {
        UNLOCKED, READLOCK, WRITELOCK,
    }
    
    private LinkedList<Waiter> _waiters = new LinkedList<Waiter>();

    private HashSet<SynchChannel> _owners = new HashSet<SynchChannel>();

    private Status _status = Status.UNLOCKED;

    private ReadWriteLockChannel _self;

    public READWRITELOCK(ReadWriteLockChannel self) {
        _self = self;
    }

    @Override
    public void run() {
        object(new ReadWriteLockChannelListener(_self) {
            private static final long serialVersionUID = -8644268413754259515L;

            public void readLock(SynchChannel s) {
                switch (_status) {
                case UNLOCKED:
                    _status = Status.READLOCK;
                    _owners.add(s);
                    s.ret();
                    break;
                case READLOCK:
                    _owners.add(s);
                    s.ret();
                    break;
                case WRITELOCK:
                    _waiters.add(new Waiter(s, false));
                    break;

                }

                instance(READWRITELOCK.this);

            }

            public void writeLock(SynchChannel s) {
                switch (_status) {
                case UNLOCKED:
                    _status = Status.WRITELOCK;
                    _owners.add(s);
                    s.ret();
                    break;
                case READLOCK:
                    _waiters.add(new Waiter(s, true));
                    break;
                case WRITELOCK:
                    _waiters.add(new Waiter(s, false));
                    break;
                }

                instance(READWRITELOCK.this);
            }

            public void unlock(SynchChannel s) {

                _owners.remove(s);
                if (_owners.isEmpty()) {
                    _status = Status.UNLOCKED;
                    if (!_waiters.isEmpty()) {
                        Waiter w = _waiters.removeFirst();
                        _owners.add(w.synch);
                        _status = w.write ? Status.WRITELOCK : Status.READLOCK;
                        w.synch.ret();

                        if (_status == Status.READLOCK)
                            for (Iterator<Waiter> i = _waiters.iterator(); i.hasNext();) {
                                Waiter w1 = i.next();
                                if (w1.write)
                                    break;
                                _owners.add(w1.synch);
                                w1.synch.ret();
                                i.remove();
                            }
                    }
                }

                instance(READWRITELOCK.this);
            }

        });
    }

    private static class Waiter {
        SynchChannel synch;

        boolean write;

        Waiter(SynchChannel s, boolean w) {
            synch = s;
            write = w;
        }
    }

  
}
