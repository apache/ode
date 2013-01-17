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
package org.apache.ode.jacob.examples.helloworld;

import org.apache.ode.jacob.Channel;
import org.apache.ode.jacob.JacobRunnable;
import org.apache.ode.jacob.ReceiveProcess;
import org.apache.ode.jacob.Synch;
import org.apache.ode.jacob.Val;
import org.apache.ode.jacob.examples.sequence.Sequence;
import org.apache.ode.jacob.vpu.ExecutionQueueImpl;
import org.apache.ode.jacob.vpu.JacobVPU;

/**
 * Simple Hello World example to showcase different
 * features and approaches of the Jacob API.
 * 
 * Inspired by http://scienceblogs.com/goodmath/2007/04/16/back-to-calculus-a-better-intr-1/
 * 
 */
@SuppressWarnings("serial")
public class HelloWorld extends JacobRunnable {

    public interface Callback<T, R extends Channel> extends Channel {
        public void invoke(T value, R callback);
    }

    static class ReliablePrinterProcess extends JacobRunnable {
        private Callback<String, Synch> _in;
        public ReliablePrinterProcess(Callback<String, Synch> in) {
            _in = in;
        }

        public void run() {
            object(true, new ReceiveProcess<Callback<String, Synch>>(_in, new Callback<String, Synch>(){
                @Override
                public void invoke(String value, Synch callback) {
                    System.out.println(value);
                    callback.ret();
                }
            }) {
                private static final long serialVersionUID = 1L;
            });
        }
    }

    static class ReliableStringEmitterProcess extends JacobRunnable {
        private String str;
        private Callback<String, Synch> to;
        
        public ReliableStringEmitterProcess(String str, Callback<String, Synch> to) {
            this.str = str;
            this.to = to;
        }

        public void run() {
            Synch callback = newChannel(Synch.class, "callback channel to ACK " + str);
            object(new ReceiveProcess<Synch>(callback, new Synch() {
                
                @Override
                public void ret() {
                    System.out.println(str + " ACKed");
                }
            }) {
                 private static final long serialVersionUID = 1L;
            });
            to.invoke(str, callback);
        }
    }

    
    static class PrinterProcess extends JacobRunnable {
        private Val _in;
        public PrinterProcess(Val in) {
            _in = in;
        }

        public void run() {
            object(true, new ReceiveProcess<Val>(_in, new Val(){
                public void val(Object o) {
                    System.out.println(o);
                }
            }) {
                private static final long serialVersionUID = 1L;
            });
        }
    }

    static class StringEmitterProcess extends JacobRunnable {
        private String str;
        private Val to;
        
        public StringEmitterProcess(String str, Val to) {
            this.str = str;
            this.to = to;
        }

        public void run() {
            to.val(str);
        }
    }

    static class ForwarderProcess extends JacobRunnable {
        private Val in;
        private Val out;
        public ForwarderProcess(Val in, Val out) {
            this.in = in;
            this.out = out;
        }

        public void run() {
            object(true, new ReceiveProcess<Val>(in, new Val(){
                public void val(Object o) {
                    out.val(o);
                }
            }) {
                private static final long serialVersionUID = 1L;
            });
        }
    }

    private void simpleHelloWorld() {
        // new(out)
        final Val out = newChannel(Val.class, "simpleHelloWorld-out");
        // new(x)
        final Val x = newChannel(Val.class, "simpleHelloWorld-x");
        // *(?out(str).!sysout(str))
        instance(new PrinterProcess(out));
        // *(?x(str).!out(str))
        instance(new ForwarderProcess(x, out));

        // !out(hello) | !out(world)
        instance(new StringEmitterProcess("Hello", x));
        instance(new StringEmitterProcess("World", x));
    }
    
    private void reliableHelloWorld() {
        // reliable version of the code above
        // (new(callback).!out(hello).?callback) | (new(callback).!out(world).?callback)
        
        // new(rout)
        Callback<String, Synch> rout = newChannel(Callback.class, "reliableHelloWorld-rout");
        // *(?rout(str).!sysout(str))
        instance(new ReliablePrinterProcess(rout));
        // (new(callback).!out(hello).?callback)
        instance(new ReliableStringEmitterProcess("Hello", rout));
        // (new(callback).!out(world).?callback)
        instance(new ReliableStringEmitterProcess("World", rout));
    }
    
    
    private void sequencedHelloWorld() {
        // send hello world as a sequence
        // !out(hello).!out(world)

        // new(out)
        final Val out = newChannel(Val.class, "sequencedHelloWorld-out");

        final String[] greeting = {"Hello", "World"};
        instance(new Sequence(greeting.length, null) {
            @Override
            protected JacobRunnable doStep(final int step, final Synch done) {
                return new JacobRunnable() {
                    @Override
                    public void run() {
                        instance(new StringEmitterProcess(greeting[step], out));
                        done.ret();
                    }
                };
            }
        });
    }
    
    @Override
    public void run() {
        simpleHelloWorld();
        reliableHelloWorld();
        sequencedHelloWorld();
    }

    public static void main(String args[]) {
        JacobVPU vpu = new JacobVPU();
        vpu.setContext(new ExecutionQueueImpl(null));
        vpu.inject(new HelloWorld());
        while (vpu.execute()) {
            System.out.println(vpu.isComplete() ? "<0>" : ".");
            //vpu.dumpState();
        }
        vpu.dumpState();
    }

}
