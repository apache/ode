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
package org.apache.ode.jacob.vpu;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.ode.jacob.ExportableChannel;
import org.apache.ode.jacob.Channel;
import org.apache.ode.jacob.ProcessUtil;

import junit.framework.TestCase;


public class ProxyConstructorTimingTest extends TestCase {
    private static final long COUNT = 1000000L;
    public ProxyConstructorTimingTest(String testName) {
        super(testName);
    }

    public void testDoNothing() throws Exception {
        Greeter gp = (Greeter) Proxy.newProxyInstance(Greeter.class.getClassLoader(),
            new Class<?>[] {ExportableChannel.class, Greeter.class}, new GreeterInvocationHandler(new GreeterImpl()));
        assertEquals("Hello World", gp.hello("World"));
        assertEquals("Implemented by InvocationHandler", ProcessUtil.exportChannel(gp));
    }

    public interface TestExecution {
        public void execute() throws Exception;
    }

    public class RepeatExecution implements TestExecution {
        private final long count;
        private final TestExecution test;
        public RepeatExecution(long count, TestExecution test) {
            this.count = count;
            this.test = test;
        }
        public void execute() throws Exception {
            for (long i = 0; i < count; i++) {
                test.execute();
            }
        }
    }

    public class TimedExecution implements TestExecution {
        private final String name;
        private final TestExecution test;
        public TimedExecution(String name, TestExecution test) {
            this.name = name;
            this.test = test;
        }
        public void execute() throws Exception {
            NanoTimer timer = new NanoTimer().start();
            test.execute();
            System.out.println("TimedExecution(" + name + "): " + timer.stop() + "[ns]");
        }
    }

    public void timedRepeatedExecution(String name, TestExecution test) throws Exception {
        new TimedExecution(name, new RepeatExecution(COUNT, test)).execute();
    }

    public void manualTestProxyTiming() throws Exception {
        timedRepeatedExecution("direct invocation", new TestExecution() {
            @Override
            public void execute() throws Exception {
                // Create new instance every time
                new GreeterImpl2().hello("World");
            }
        });

        timedRepeatedExecution("newProxyInstance", new TestExecution() {
            @Override
            public void execute() throws Exception {
                Greeter gp = (Greeter) Proxy.newProxyInstance(Greeter.class.getClassLoader(),
                    new Class<?>[] {Greeter.class}, new GreeterInvocationHandler(new GreeterImpl2()));
                gp.hello("World");
            }
        });

        final ProxyConstructor<Greeter> helper = new ProxyConstructor<Greeter>(Greeter.class);
        timedRepeatedExecution("ProxyConstructor", new TestExecution() {
            @Override
            public void execute() throws Exception {
                Greeter gp = (Greeter) helper.newInstance(new GreeterInvocationHandler(new GreeterImpl2()));
                gp.hello("World");
            }
        });
    }
    
    public interface Greeter extends Channel {
        String hello(String name);
    }
    
    @SuppressWarnings("serial")
	public class GreeterImpl implements Greeter {
        public String hello(String name) {
            return "Hello " + name;
        }
    }

    @SuppressWarnings("serial")
	public class GreeterImpl2 implements Greeter {
        public String hello(String name) {
            return "";
        }
    }

    public class GreeterInvocationHandler implements InvocationHandler {
        private Object greeter;
        GreeterInvocationHandler(Object o) {
            greeter = o;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
           if(Object.class  == method.getDeclaringClass()) {
               String name = method.getName();
               if("equals".equals(name)) {
                   return proxy == args[0];
               } else if("hashCode".equals(name)) {
                   return System.identityHashCode(proxy);
               } else if("toString".equals(name)) {
                   return proxy.getClass().getName() + "@" +
                       Integer.toHexString(System.identityHashCode(proxy)) +
                       ", with InvocationHandler " + this;
               } else {
                   throw new IllegalStateException(String.valueOf(method));
               }
           }
           if (method.equals(ExportableChannel.class.getMethod("export", new Class[] {}))) {
               return "Implemented by InvocationHandler";
           }
           return method.invoke(greeter, args);
        }    
    }

    // TODO: may be useful for other things? move it somewhere else?
    public class NanoTimer {
        private long start;
        private long lap;
        // TODO: we could also count laps...
        public NanoTimer() {
            // don't start by default, easy to just call .start();
        }
        public NanoTimer start() {
            start = System.nanoTime();
            lap = start;
            return this;
        }
        public long stop() {
            long span = System.nanoTime() - start;
            start = 0;
            lap = 0;
            return span;
        }
        public long lap() {
            long prev = lap;
            lap = (start != 0) ? System.nanoTime() : 0;
            return lap - prev;
        }
    }
}
