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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.jacob.Channel;
import org.apache.ode.jacob.ChannelListener;
import org.apache.ode.jacob.JacobObject;
import org.apache.ode.jacob.JacobRunnable;
import org.apache.ode.jacob.JacobThread;
import org.apache.ode.jacob.SynchChannel;
import org.apache.ode.jacob.soup.CommChannel;
import org.apache.ode.jacob.soup.CommGroup;
import org.apache.ode.jacob.soup.CommRecv;
import org.apache.ode.jacob.soup.CommSend;
import org.apache.ode.jacob.soup.Continuation;
import org.apache.ode.jacob.soup.ExecutionQueue;
import org.apache.ode.utils.ArrayUtils;
import org.apache.ode.utils.ObjectPrinter;
import org.apache.ode.utils.msg.MessageBundle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * The JACOB Virtual Processing Unit ("VPU").
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com" />
 */
public final class JacobVPU {
  /** Class-level logger. */
  private static final Log __log = LogFactory.getLog(JacobVPU.class);

  /** Internationalization messages. */
  private static final JacobMessages __msgs = MessageBundle.getMessages(JacobMessages.class);

  /** Thread-local for associating a thread with a VPU. */
  static final ThreadLocal<JacobThread> __activeJacobThread = new ThreadLocal<JacobThread>();

  private static final Method REDUCE_METHOD;

  /** Pre-fetch the {@link JacobRunnable#self} method */
  static {
    Method rm = null;

    try {
      rm = JacobRunnable.class.getMethod("self", ArrayUtils.EMPTY_CLASS_ARRAY);
    } catch (Exception e) {
      e.printStackTrace();
    }

    REDUCE_METHOD = rm;
  }

  /** Persisted cross-VPU state (state of the channels) */
  private ExecutionQueue _executionQueue;

  private Map<Class, Object> _extensions = new HashMap<Class, Object>();

  /** Classloader used for loading object continuations. */
  private ClassLoader _classLoader = getClass().getClassLoader();

  private boolean _debug = false;

  private int _cycle;

  private Statistics _statistics = new Statistics();

  /** The fault "register" of the VPU . */
  private RuntimeException _fault;

  /**
   * Default constructor.
   */
  public JacobVPU() {
  }

  /**
   * Re-hydration constructor.
   * @param executionQueue previously saved execution context
   */
  public JacobVPU(ExecutionQueue executionQueue) {
    this();
    setContext(executionQueue);
  }

  /**
   * Instantiation constructor; used to initialize context with
   * the concretion of a process abstraction.
   * @param context virgin context object
   * @param concretion the process
   */
  public JacobVPU(ExecutionQueue context, JacobRunnable concretion) {
    setContext(context);
    inject(concretion);
  }

  /**
   * Execute one VPU cycle.
   * @return <code>true</code> if the run queue is not empty after this cycle,
   *         <code>false</code> otherwise.
   */
  public boolean execute() {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("execute", ArrayUtils.EMPTY_OBJECT_ARRAY));

    if (_executionQueue == null)
      throw new IllegalStateException("No state object for VPU!");
    
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
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("flush", ArrayUtils.EMPTY_OBJECT_ARRAY));
    _executionQueue.flush();
  }

  /**
   * DOCUMENTME
   */
  public void reset() {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("reset", ArrayUtils.EMPTY_OBJECT_ARRAY));
  }

  /**
   * Set the state of of the VPU; this is analagous to loading a CPU with
   * a thread's context (re-hydration).
   * @param executionQueue process executionQueue (state)
   */
  public void setContext(ExecutionQueue executionQueue) {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("setContext", new Object[] {"soupDao", executionQueue} ));
    _executionQueue = executionQueue;
    _executionQueue.setClassLoader(_classLoader);
  }


  public void registerExtension(Class extensionClass, Object obj) {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("registerExtension", new Object[] {
        "extensionClass", extensionClass,
        "obj", obj
      } ));

    _extensions.put(extensionClass, obj);
  }

  /**
   * Add an item to the run queue.
   */
  public void addReaction(JacobObject jmb, Method method, Object[] args, String desc) {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("addReaction", new Object[] {
        "jmb", jmb,
        "method", method,
        "args", args,
        "desc", desc
      }));

    Continuation continuation = new Continuation(jmb, method, args);
    continuation.setDescription(desc);
    _executionQueue.enqueueReaction(continuation);
    ++_statistics.runQueueEntries;
  }

  /**
   * Get the active Jacob thread, i.e. the one associated with the current
   * Java thread.
   *
   * @return Jacob thread ({@link JacobThread}) associated with the current Java thread
   * @see JacobThread
   */
  public static JacobThread activeJacobThread() {
    return __activeJacobThread.get();
  }




  /**
   * Inject a concretion into the process context. This amounts to
   * chaning the process context from <code>P</code> to <code>P|Q</code>
   * where <code>P</code> is the previous process context and
   * <code>Q</code> is the injected process. This method is equivalent
   * to the parallel operator, but is intended to be used from outside
   * of an active {@link JacobThread}.
   * @param concretion the concretion to inject into the process context
   */
  public void inject(JacobRunnable concretion) {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("inject", new Object[] { "concretion", concretion }));


    if (__log.isDebugEnabled())
      __log.debug("injecting " + concretion);
    addReaction(concretion, REDUCE_METHOD, ArrayUtils.EMPTY_OBJECT_ARRAY,
            (__log.isInfoEnabled() ? concretion.toString() : null));
  }

  static String stringifyMethods(Class kind) {
    StringBuffer buf = new StringBuffer();
    Method[] methods = kind.getMethods();
    boolean found = false;

    for (int i = 0; i < methods.length; ++i) {
      if (methods[i].getDeclaringClass() == Object.class) {
        continue;
      }

      if (found) {
        buf.append(" & ");
      }

      buf.append(methods[i].getName());
      buf.append('(');

      Class[] argTypes = methods[i].getParameterTypes();

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
    if (_executionQueue != null)
      _executionQueue.setClassLoader(classLoader);
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
    private String _prefix;

    /** Text string identifying the left side of the reduction (for debug). */
    private String _source;

    /** Text string identifying the target class and method (for debug) .*/
    private String _targetStr = "Unknown";

    JacobThreadImpl(Continuation rqe) {
      assert rqe != null;

      _methodBody = rqe.getClosure();
      _args = rqe.getArgs();
      _source = rqe.getDescription();
      _method = rqe.getMethod();
      
      if(__log.isDebugEnabled()){
      	StringBuffer buf = new StringBuffer(_methodBody.getClass().getName());
      	buf.append('.');
        buf.append(rqe.getMethod());
        _targetStr = buf.toString();
      }
     
    }

    public void instance(JacobRunnable template) {
      String desc = null;
      if (__log.isDebugEnabled()){
        __log.debug(_cycle + ": " + _prefix +  template);
        desc = template.toString();
      }

      _statistics.numReductionsStruct++;
      addReaction(template, REDUCE_METHOD, ArrayUtils.EMPTY_OBJECT_ARRAY, desc);
    }

    public Channel message(Channel channel, Method method, Object[] args) {
      if (__log.isDebugEnabled())
        __log.debug(_cycle + ": " + _prefix  + channel + " ! "
                + method.getName() + "(" + stringify(args) + ")");

      _statistics.messagesSent++;

      SynchChannel replyChannel = null;

      // Check for synchronous methods; create a synchronization channel
      if (method.getReturnType() != void.class) {
        if (method.getReturnType() != SynchChannel.class)
          throw new IllegalStateException("ChannelListener method can only return SynchChannel: " + method);
        replyChannel = (SynchChannel) newChannel(SynchChannel.class,"", "Reply Channel");
        Object[] newArgs = new Object[args.length + 1];
        System.arraycopy(args, 0, newArgs, 0, args.length);
        newArgs[args.length] = replyChannel;
        args = newArgs;
      }
      CommChannel chnl = (CommChannel) ChannelFactory.getBackend(channel);
      CommGroup grp = new CommGroup(false);
      CommSend send = new CommSend(chnl, method, args);

      grp.add(send);
      _executionQueue.add(grp);

      return replyChannel;
    }

    public Channel newChannel(Class channelType, String creator,
                              String description) {
      CommChannel chnl = new CommChannel(channelType);
      chnl.setDescription(description);
      _executionQueue.add(chnl);

      // Some of the debug information is a bit lengthy...
      //cframe.setDebugInfo(fillDebugInfo());

      Channel ret = ChannelFactory.createChannel(chnl, channelType);
      if (__log.isDebugEnabled())
        __log.debug(_cycle + ": " + _prefix + "new " + ret );

      _statistics.channelsCreated++;
      return ret;

    }

    public String exportChannel(Channel channel) {
      if (__log.isDebugEnabled())
        __log.debug(_cycle + ": " + _prefix + "export<" + channel + ">");

      CommChannel chnl = (CommChannel)ChannelFactory.getBackend(channel);
      return _executionQueue.createExport(chnl);
    }

    public Channel importChannel(String channelId, Class channelType) {
      try {
        CommChannel cframe = _executionQueue.consumeExport(channelId);
        return ChannelFactory.createChannel(cframe, channelType);
      } catch (RuntimeException re) {
        throw re;
      }
    }

    /**
     * @see JacobThread#object
     */
    public void object(boolean replicate,  ChannelListener[] ml) {
      if (__log.isDebugEnabled()) {
        StringBuffer msg = new StringBuffer();
        msg.append(_cycle);
        msg.append(": ");
        msg.append(_prefix);
        for (int i = 0 ; i < ml.length; ++i) {
          if (i != 0)
            msg.append(" + ");
          msg.append(ml[i].getChannel());
          msg.append(" ? ");
          msg.append(ml.toString());

        }
        __log.debug(msg.toString());
      }

      _statistics.numContinuations++;

      CommGroup grp = new CommGroup(replicate);
      for (int i = 0; i < ml.length; ++i) {
        CommChannel chnl = (CommChannel) ChannelFactory.getBackend(ml[i].getChannel());
        // TODO see below..
        // oframe.setDebugInfo(fillDebugInfo());
        CommRecv recv = new CommRecv(chnl,ml[i]);
        grp.add(recv);
      }

      _executionQueue.add(grp);

    }

    public void object(boolean replicate, ChannelListener methodList) throws IllegalArgumentException {
      object(replicate, new ChannelListener[] { methodList } );
    }

//    private DebugInfo fillDebugInfo() {
//      // Some of the debug information is a bit lengthy, so lets not put it in
//      // all the time... eh.
//      if (_debug) {
//        DebugInfo frame  = new DebugInfo();
//        frame.setCreator(_source);
//        Exception ex = new Exception();
//        StackTraceElement[] st = ex.getStackTrace();
//        if (st.length > 2) {
//          StackTraceElement[] stcut = new StackTraceElement[st.length - 2];
//          System.arraycopy(st, 2, stcut, 0, stcut.length);
//          frame.setLocation(stcut);
//        }
//
//        return frame;
//      }
//      return null;
//    }

    public Object getExtension(Class extensionClass) {
      return _extensions.get(extensionClass);
    }

    public void run() {
      assert _methodBody != null;

      assert _method != null;
      assert _method.getDeclaringClass()
                    .isAssignableFrom(_methodBody.getClass());
      assert __activeJacobThread.get() == null;


      if (__log.isDebugEnabled()) {
        String dbgMsg = _cycle + ": " + _source;
        __log.debug(dbgMsg);
        _prefix = "     ===> ";
      }

      Object[] args;
      SynchChannel synchChannel;
      if (_method.getReturnType() != void.class) {
        args = new Object[_args.length-1];
        System.arraycopy(_args,0, args, 0, args.length);
        synchChannel = (SynchChannel)_args[args.length];
      } else {
        args = _args;
        synchChannel = null;
      }
      __activeJacobThread.set(this);
      long ctime = System.currentTimeMillis();
      try {
        _method.invoke(_methodBody, args);
        if (synchChannel != null)
          synchChannel.ret();
      } catch (IllegalAccessException iae) {
        String msg = __msgs.msgMethodNotAccessible(_method.getName(), _method.getDeclaringClass().getName());
        __log.error(msg, iae);
        throw new RuntimeException(msg, iae);
      } catch (InvocationTargetException e) {
        String msg = __msgs.msgClientMethodException(_method.getName(), _methodBody.getClass().getName());
        __log.error(msg, e.getTargetException());
        throw new RuntimeException(e.getTargetException());
      } finally {
        ctime = System.currentTimeMillis() - ctime;
        _statistics.totalClientTimeMs += ctime;
        __activeJacobThread.set(null);
        _prefix = null;
      }

      assert __activeJacobThread.get() == null;
    }

    public String toString() {
      StringBuffer buf = new StringBuffer("PT[ ");
      buf.append(_methodBody);
      buf.append(" ]");
      return buf.toString();
    }
  }

}
