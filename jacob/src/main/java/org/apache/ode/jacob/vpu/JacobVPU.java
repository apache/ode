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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.ode.jacob.Channel;
import org.apache.ode.jacob.ChannelListener;
import org.apache.ode.jacob.CompositeProcess;
import org.apache.ode.jacob.JacobObject;
import org.apache.ode.jacob.JacobRunnable;
import org.apache.ode.jacob.JacobThread;
import org.apache.ode.jacob.ReceiveProcess;
import org.apache.ode.jacob.Synch;
import org.apache.ode.jacob.soup.CommChannel;
import org.apache.ode.jacob.soup.CommGroup;
import org.apache.ode.jacob.soup.CommRecv;
import org.apache.ode.jacob.soup.CommSend;
import org.apache.ode.jacob.soup.Continuation;
import org.apache.ode.jacob.soup.ExecutionQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JACOB Virtual Processing Unit ("VPU").
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com" />
 */
public final class JacobVPU {
    private static final Logger LOG = LoggerFactory.getLogger(JacobVPU.class);

    // Thread-local for associating a thread with a VPU. Needs to be stored in a stack to allow reentrance.
    private static final ThreadLocal<Stack<JacobThread>> ACTIVE_THREAD = new ThreadLocal<Stack<JacobThread>>();
    private static final Method REDUCE_METHOD;

    static {
        try {
            // Resolve the {@link JacobRunnable#run} method once statically
            REDUCE_METHOD = JacobRunnable.class.getMethod("run", new Class[]{});
        } catch (Exception e) {
            throw new Error("Cannot resolve 'run()' method", e);
        }
    }

    /**
     * Persisted cross-VPU state (state of the channels)
     */
    private ExecutionQueue _executionQueue;
    private Map<Class<?>, Object> _extensions = new HashMap<Class<?>, Object>();

    /**
     * Classloader used for loading object continuations.
     */
    private ClassLoader _classLoader = getClass().getClassLoader();

    private int _cycle;

    private Statistics _statistics = new Statistics();

    /**
     * The fault "register" of the VPU .
     */
    private RuntimeException _fault;

    public JacobVPU() {
    }

    /**
     * Execute one VPU cycle.
     *
     * @return <code>true</code> if the run queue is not empty after this cycle, <code>false</code> otherwise.
     */
    public boolean execute() {
        LOG.trace(">> JacobVPU.execute()");

        if (_executionQueue == null) {
            throw new IllegalStateException("No state object for VPU!");
        }
        if (_fault != null) {
            throw _fault;
        }
        if (!_executionQueue.hasReactions()) {
            return false;
        }
        _cycle = _executionQueue.cycle();

        Continuation rqe = _executionQueue.dequeueReaction();
        JacobThreadImpl jt = new JacobThreadImpl(rqe);

        long ctime = System.currentTimeMillis();
        try {
            jt.run();
        } catch (RuntimeException re) {
            _fault = re;
            throw re;
        }

        long rtime = System.currentTimeMillis() - ctime;
        ++_statistics.numCycles;
        _statistics.totalRunTimeMs += rtime;
        _statistics.incRunTime(jt._targetStr, rtime);
        return true;
    }

    public void flush() {
        LOG.trace(">> JacobVPU.flush ()");
        _executionQueue.flush();
    }

    /**
     * Set the state of of the VPU; this is analagous to loading a CPU with a
     * thread's context (re-hydration).
     *
     * @param executionQueue
     *            process executionQueue (state)
     */
    public void setContext(ExecutionQueue executionQueue) {
        LOG.trace(">> setContext (executionQueue={})", executionQueue);

        _executionQueue = executionQueue;
        _executionQueue.setClassLoader(_classLoader);
    }

    public void registerExtension(Class<?> extensionClass, Object obj) {
        LOG.trace(">> setContext (extensionClass={}, obj={})", extensionClass, obj);
        _extensions.put(extensionClass, obj);
    }

    /**
     * Add an item to the run queue.
     */
    public void addReaction(JacobObject jo, Method method, Object[] args, String desc) {
        LOG.trace(">> addReaction (jo={}, method={}, args={}, desc={})", jo, method, args, desc);

        Continuation continuation = new Continuation(jo, method, args);
        continuation.setDescription(desc);
        _executionQueue.enqueueReaction(continuation);
        ++_statistics.runQueueEntries;
    }

    /**
     * Get the active Jacob thread, i.e. the one associated with the current Java thread.
     */
    public static JacobThread activeJacobThread() {
        return ACTIVE_THREAD.get().peek();
    }

    /**
     * Inject a concretion into the process context. This amounts to chaning the
     * process context from <code>P</code> to <code>P|Q</code> where
     * <code>P</code> is the previous process context and <code>Q</code> is
     * the injected process. This method is equivalent to the parallel operator,
     * but is intended to be used from outside of an active {@link JacobThread}.
     */
    public void inject(JacobRunnable concretion) {
        LOG.debug("injecting {}", concretion);
        addReaction(concretion, REDUCE_METHOD, new Class[]{},
            (LOG.isInfoEnabled() ? concretion.toString() : null));
    }

    static String stringifyMethods(Class<?> kind) {
        StringBuffer buf = new StringBuffer();
        Method[] methods = kind.getMethods();
        boolean found = false;

        for (Method method : methods) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            if (found) {
                buf.append(" & ");
            }
            buf.append(method.getName()).append('(');
            Class<?>[] argTypes = method.getParameterTypes();
            for (int j = 0; j < argTypes.length; ++j) {
                if (j > 0) {
                    buf.append(", ");
                }
                buf.append(argTypes[j].getName());
            }
            buf.append(") {...}");
            found = true;
        }
        return buf.toString();
    }

    static String stringify(Object[] list) {
        if (list == null) {
            return "";
        }
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < list.length; ++i) {
            if (i > 0) {
                buf.append(',');
            }
            buf.append(list[i]);
        }
        return buf.toString();
    }

    public void setClassLoader(ClassLoader classLoader) {
        _classLoader = classLoader;
        if (_executionQueue != null) {
            _executionQueue.setClassLoader(classLoader);
        }
    }

    /**
     * Dump the state of the VPU for debugging purposes.
     */
    public void dumpState() {
        _statistics.printToStream(System.err);
        _executionQueue.dumpState(System.err);
    }

    public boolean isComplete() {
        return _executionQueue.isComplete();
    }

    private class JacobThreadImpl implements Runnable, JacobThread {
        private final JacobObject _methodBody;

        private final Object[] _args;

        private final Method _method;

        /** Text string identifying the left side of the reduction (for debug). */
        private String _source;

        /** Text string identifying the target class and method (for debug) . */
        private String _targetStr = "Unknown";

        JacobThreadImpl(Continuation rqe) {
            assert rqe != null;

            _methodBody = rqe.getClosure();
            _args = rqe.getArgs();
            _source = rqe.getDescription();
            _method = rqe.getMethod();

            if (LOG.isDebugEnabled()) {
                StringBuffer buf = new StringBuffer(_methodBody.getClass().getName());
                buf.append('.');
                buf.append(rqe.getMethod());
                _targetStr = buf.toString();
            }
        }

        public void instance(JacobRunnable template) {
            LOG.trace(">> [{}] : {}", _cycle, template);

            _statistics.numReductionsStruct++;
            addReaction(template, REDUCE_METHOD, new Class[]{}, 
                LOG.isInfoEnabled() ? template.toString() : null);
        }

        public Channel message(Channel channel, Method method, Object[] args) {
            LOG.trace(">> [{}] : {} ! {} ({})", _cycle, channel, method.getName(),
                LOG.isTraceEnabled() ? stringify(args) : null);

            _statistics.messagesSent++;

            Synch replyChannel = null;
            // Check for synchronous methods; create a synchronization channel
            if (method.getReturnType() != void.class) {
                if (method.getReturnType() != Synch.class) {
                    throw new IllegalStateException(
                        "Channel method '" + method + "' must only return void or Synch");
                }
                replyChannel = (Synch) newChannel(Synch.class, "", "Reply Channel");
                Object[] newArgs = new Object[args.length + 1];
                System.arraycopy(args, 0, newArgs, 0, args.length);
                newArgs[args.length] = replyChannel;
                args = newArgs;
            }
            CommChannel chnl = (CommChannel) ChannelFactory.getBackend((Channel)channel);
            CommGroup grp = new CommGroup(false);
            CommSend send = new CommSend(chnl, method, args);
            grp.add(send);
            _executionQueue.add(grp);
            return replyChannel;
        }

        public Channel newChannel(Class<?> channelType, String creator, String description) {
            CommChannel chnl = new CommChannel(channelType);
            chnl.setDescription(description);
            _executionQueue.add(chnl);

            Channel ret = ChannelFactory.createChannel(chnl, channelType);
            LOG.trace(">> [{}] : new {}", _cycle, ret);

            _statistics.channelsCreated++;
            return ret;
        }

        public String exportChannel(Channel channel) {
            LOG.trace(">> [{}] : export<{}>", _cycle, channel);

            CommChannel chnl = (CommChannel)ChannelFactory.getBackend((Channel)channel);
            return _executionQueue.createExport(chnl);
        }

        public Channel importChannel(String channelId, Class<?> channelType) {
            CommChannel cframe = _executionQueue.consumeExport(channelId);
            return ChannelFactory.createChannel(cframe, channelType);
        }

        public void object(boolean replicate, ChannelListener[] ml) {
            if (LOG.isTraceEnabled()) {
                StringBuffer msg = new StringBuffer();
                msg.append(_cycle);
                msg.append(": ");
                for (int i = 0; i < ml.length; ++i) {
                    if (ml[i] instanceof ReceiveProcess) {
                        ReceiveProcess<?> rp = (ReceiveProcess<?>)ml[i];
                        if (i != 0) msg.append(" + ");
                        msg.append(rp.getChannel());
                        msg.append(" ? ");
                        msg.append(rp.toString());
                    }
                }
                LOG.trace(msg.toString());
            }

            _statistics.numContinuations++;

            CommGroup grp = new CommGroup(replicate);
            for (int i = 0; i < ml.length; ++i) {
                addCommChannel(grp, ml[i]);
            }
            _executionQueue.add(grp);
        }

        public void object(boolean replicate, ChannelListener methodList) throws IllegalArgumentException {
            object(replicate, new ChannelListener[] { methodList });
        }

        private void addCommChannel(CommGroup group, ChannelListener receiver) {
            if (receiver instanceof CompositeProcess) {
                for (ChannelListener r : ((CompositeProcess)receiver).getProcesses()) {
                    addCommChannel(group, r);
                }
            } else if (receiver instanceof ReceiveProcess) {
                CommChannel chnl = (CommChannel)ChannelFactory.getBackend(
                        ((ReceiveProcess<?>)receiver).getChannel());
                    // TODO see below..
                    // oframe.setDebugInfo(fillDebugInfo());
                    CommRecv recv = new CommRecv(chnl, receiver);
                    group.add(recv);
            } else {
                throw new IllegalStateException("Don't know how to handle Process type...");
            }
        }

        /* UNUSED
         private DebugInfo fillDebugInfo() {
            // Some of the debug information is a bit lengthy, so lets not put
            // it in all the time... eh.
            DebugInfo frame = new DebugInfo();
            frame.setCreator(_source);
            Exception ex = new Exception();
            StackTraceElement[] st = ex.getStackTrace();
            if (st.length > 2) {
                StackTraceElement[] stcut = new StackTraceElement[st.length - 2];
                System.arraycopy(st, 2, stcut, 0, stcut.length);
                frame.setLocation(stcut);
            }

            return frame;
        }
        */

        public Object getExtension(Class<?> extensionClass) {
            return _extensions.get(extensionClass);
        }

        public void run() {
            assert _methodBody != null;
            assert _method != null;
            assert _method.getDeclaringClass().isAssignableFrom(_methodBody.getClass());

            LOG.trace(">> [{}] : {}", _cycle, _source);

            Object[] args;
            Synch synchChannel;
            if (_method.getReturnType() != void.class) {
                args = new Object[_args.length - 1];
                System.arraycopy(_args, 0, args, 0, args.length);
                synchChannel = (Synch)_args[args.length];
            } else {
                args = _args;
                synchChannel = null;
            }
            stackThread();
            long ctime = System.currentTimeMillis();
            try {
                _method.invoke(_methodBody instanceof ReceiveProcess ? 
                    ((ReceiveProcess<?>)_methodBody).receiver() : _methodBody, args);
                if (synchChannel != null) {
                    synchChannel.ret();
                }
            } catch (IllegalAccessException iae) {
                throw new RuntimeException("MethodNotAccessible: " + _method.getName() + " in " + _method.getDeclaringClass().getName(), iae);
            } catch (InvocationTargetException e) {
                Throwable target = e.getTargetException();
                throw (target instanceof RuntimeException)
                    ? (RuntimeException) target
                    : new RuntimeException("ClientMethodException: " + _method.getName() + " in " + _methodBody.getClass().getName(), target);
            } finally {
                ctime = System.currentTimeMillis() - ctime;
                _statistics.totalClientTimeMs += ctime;
                unstackThread();
            }
        }

        public String toString() {
            return "PT[ " + _methodBody + " ]";
        }

        private void stackThread() {
            Stack<JacobThread> crt = ACTIVE_THREAD.get();
            if (crt == null) {
                crt = new Stack<JacobThread>();
                ACTIVE_THREAD.set(crt);
            }
            crt.push(this);
        }

        private JacobThread unstackThread() {
            return ACTIVE_THREAD.get().pop();
        }
    }
}
