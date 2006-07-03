/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.jacob.vpu;

import com.fs.jacob.Channel;
import com.fs.jacob.JavaClosure;
import com.fs.jacob.ML;
import com.fs.jacob.soup.*;
import com.fs.utils.ArrayUtils;
import com.fs.utils.ObjectPrinter;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A fast, in-memory {@link com.fs.jacob.soup.Soup} implementation.
 */
public class FastSoupImpl implements Soup {
  /** Class-level logger. */
  private static final Log __log = LogFactory.getLog(FastSoupImpl.class);

  private ClassLoader _classLoader;

  /**
   * Cached set of enqueued {@link Reaction} objects (i.e. those reed using the
   * {@link #enqueueReaction(com.fs.jacob.soup.Reaction) method}). These reactions are
   * "cached"--that is it is not sent directly to the DAO layer--to minimize
   * unnecessary serialization/deserialization of closures. This is a pretty useful
   * optimization, as most {@link Reaction}s are enqueued, and then immediately
   * dequeued in the next cycle. By caching {@link Reaction}s, we eliminate practically
   * all serialization of these objects, the only exception being cases where the
   * system decides to stop processing a particular soup despite the soup being
   * able to make forward progress; this scenario would occur if a maximum processign
   * time-per-instance policy were in effect.
   */
  private Set<Reaction> _reactions = new HashSet<Reaction>();
  private Map<Integer, ChannelFrame> _channels = new HashMap<Integer, ChannelFrame>();

  /** The "expected" cycle counter, use to detect database serialization issues. */
  private int _currentCycle;

  private int _objIdCounter;

  private SoupStatistics _statistics = new SoupStatistics();
  private ReplacementMap _replacementMap;
  private Serializable _gdata;

  public FastSoupImpl(ClassLoader classLoader) {
    _classLoader = classLoader;
  }

  public void setClassLoader(ClassLoader classLoader) {
    _classLoader = classLoader;
  }

  public void setReplacementMap(ReplacementMap replacementMap) {
    _replacementMap = replacementMap;
  }

  public void add(CommChannel channel) {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("add", new Object[] { "channel", channel}));

    verifyNew(channel);
    ChannelFrame cframe = new ChannelFrame(channel.getType(), ++_objIdCounter,channel.getType().getName(), channel.getDescription());
    _channels.put(cframe.getId(), cframe);
    assignId(channel, cframe.getId());
  }


  public void enqueueReaction(Reaction reaction) {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("enqueueReaction", new Object[] { "reaction", reaction} ));

    verifyNew(reaction);
    _reactions.add(reaction);
  }

  public Reaction dequeueReaction() {
    if (__log.isTraceEnabled()) {
      __log.trace(ObjectPrinter.stringifyMethodEnter("dequeueReaction", ArrayUtils.EMPTY_OBJECT_ARRAY));
    }

    Reaction reaction = null;
    if (!_reactions.isEmpty()) {
      Iterator it = _reactions.iterator();
      reaction = (Reaction)it.next();
      it.remove();
    }
    // At this point it is wise to clone the reaction, so that we do not have weird
    // concurrency issues. We only clone the closure, the arguments should not be
    // a problem.
//    Reaction clone = new Reaction(cloneClosure(reaction.getClosure()), reaction.getMethod(), reaction.getArgs());
//    clone.setDescription(reaction.getDescription());
//    return clone;
    return reaction;
  }

  public void add(CommGroup group) {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("add", new Object[] { "group", group} ));

    verifyNew(group);
    CommGroupFrame commGroupFrame = new CommGroupFrame(group.isReplicated());
    for (Iterator i = group.getElements(); i.hasNext(); ) {
      Comm comm = (Comm)i.next();
      ChannelFrame chnlFrame = findChannelFrame(comm.getChannel().getId());
      if (comm instanceof CommSend) {
        if (chnlFrame.replicatedSend) {
          // TODO: JACOB "bad-process" ex
          throw new IllegalStateException("Send attempted on channel containing replicated send! Channel= " + comm.getChannel());
        }
        if (group.isReplicated())
          chnlFrame.replicatedSend = true;

        CommSend commSend = (CommSend) comm;
        MessageFrame mframe = new MessageFrame(commGroupFrame,chnlFrame, commSend.getMethod().getName(), commSend.getArgs());
        commGroupFrame.commFrames.add(mframe);
        chnlFrame.msgFrames.add(mframe);
      } else if (comm instanceof CommRecv) {
        if (chnlFrame.replicatedRecv) {
          // TODO: JACOB "bad-process" ex
          throw new IllegalStateException("Receive attempted on channel containing replicated receive! Channel= " + comm.getChannel());
        }
        if (group.isReplicated())
          chnlFrame.replicatedRecv = true;
        CommRecv  commRecv = (CommRecv) comm;
        ObjectFrame oframe = new ObjectFrame(commGroupFrame,chnlFrame, commRecv.getContinuation());
        commGroupFrame.commFrames.add(oframe);
        chnlFrame.objFrames.add(oframe);
      }
    }

    // Match communications.
    for (Iterator i = group.getElements(); i.hasNext();) {
      Comm comm = (Comm)i.next();
      matchCommunications(comm.getChannel());
    }
  }

  private ChannelFrame findChannelFrame(Object id) {
    ChannelFrame chnlFrame = _channels.get(id);
    if (chnlFrame == null) {
      throw new IllegalArgumentException("No such channel; id=" +id);
    }
    return chnlFrame;
  }

  public int cycle() {
    if (__log.isTraceEnabled()) {
      __log.trace(ObjectPrinter.stringifyMethodEnter("cycle", ArrayUtils.EMPTY_OBJECT_ARRAY));
    }

    return ++_currentCycle;
  }

  public String createExport(CommChannel channel) {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("createExport", new Object[] { "channel", channel} ));
    ChannelFrame cframe = findChannelFrame(channel.getId());
    cframe.refCount++;
    return channel.getId().toString();
  }

  public CommChannel consumeExport(String exportId) {
    if (__log.isTraceEnabled()) {
      __log.trace(ObjectPrinter.stringifyMethodEnter("consumeExport", new Object[] {"exportId", exportId} ));
    }

    Integer id = Integer.valueOf(exportId);
    ChannelFrame cframe = findChannelFrame(id);
    cframe.refCount--;
    CommChannel commChannel = new CommChannel(cframe.type);
    commChannel.setId(id);
    commChannel.setDescription("EXPORTED CHANNEL");
    return commChannel;
  }

  public boolean hasReactions() {
    return ! _reactions.isEmpty();
  }

  public void flush() {
    if (__log.isTraceEnabled()) {
      __log.trace(ObjectPrinter.stringifyMethodEnter("flush", ArrayUtils.EMPTY_OBJECT_ARRAY));
    }
  }

  public void read(InputStream iis) throws IOException, ClassNotFoundException {
    _channels.clear();
    _reactions.clear();

    SoupInputStream sis = new SoupInputStream(iis);

    _objIdCounter = sis.readInt();
    _currentCycle = sis.readInt();
    int reactions = sis.readInt();
    for (int i = 0; i < reactions; ++i) {
      JavaClosure closure = (JavaClosure)sis.readObject();
      String methodName = sis.readUTF();
      Method method = closure.getMethod(methodName);
      int numArgs = sis.readInt();
      Object[] args = new Object[numArgs];
      for (int j = 0; j < numArgs; ++j) {
        args[j] = sis.readObject();
      }
      _reactions.add(new Reaction(closure,  method, args));
    }

    int numChannels = sis.readInt();
    for (int i = 0 ; i < numChannels; ++i) {
      int objFrames = sis.readInt();
      for (int j = 0 ; j < objFrames; ++j) {
        sis.readObject();
      }
      int msgFrames = sis.readInt();
      for (int j = 0; j < msgFrames; ++j) {
        sis.readObject();
      }
    }

    numChannels  = sis.readInt();
    for (int i = 0; i < numChannels ; ++i) {
      ChannelFrame cframe = (ChannelFrame)sis.readObject();
      _channels.put(cframe.getId(), cframe);
    }
    _gdata = (Serializable) sis.readObject();
    sis.close();
  }
  public void write(OutputStream oos) throws IOException {
    flush();

    SoupOutputStream sos = new SoupOutputStream(oos);

    sos.writeInt(_objIdCounter);
    sos.writeInt(_currentCycle);

    // Write out the reactions.
    sos.writeInt(_reactions.size());
    for (Iterator i = _reactions.iterator();i.hasNext(); ) {
      Reaction reaction = (Reaction) i.next();
      sos.writeObject(reaction.getClosure());
      sos.writeUTF(reaction.getMethod().getName());
      sos.writeInt(reaction.getArgs() == null ? 0 : reaction.getArgs().length);
      for (int j = 0; reaction.getArgs() != null && j < reaction.getArgs().length; ++j)
        sos.writeObject(reaction.getArgs()[j]);
    }

    sos.writeInt(_channels.values().size());
    for (Iterator i = _channels.values().iterator(); i.hasNext(); ) {
      ChannelFrame cframe = (ChannelFrame)i.next();
      sos.writeInt(cframe.objFrames.size());
      for (Iterator j = cframe.objFrames.iterator(); j.hasNext() ;) {
        sos.writeObject(j.next());
      }
      sos.writeInt(cframe.msgFrames.size());
      for (Iterator j = cframe.msgFrames.iterator(); j.hasNext() ;) {
        sos.writeObject(j.next());
      }
    }

    Set referencedChannels = sos.getSerializedChannels();
    for (Iterator i = _channels.values().iterator(); i.hasNext(); ) {
      ChannelFrame cframe = (ChannelFrame) i.next();
      if (referencedChannels.contains(Integer.valueOf(cframe.id)) || cframe.refCount > 0) {
        // skip
      }
      else {
        if (__log.isDebugEnabled())
          __log.debug("GC Channel: " + cframe);
        i.remove();
      }

    }

    sos.writeInt(_channels.values().size());
    for (Iterator i = _channels.values().iterator(); i.hasNext(); ) {
      ChannelFrame cframe = (ChannelFrame) i.next();
      if (__log.isDebugEnabled()) {
        __log.debug("Writing Channel: " + cframe);
      }
      sos.writeObject(cframe);
    }

    sos.writeObject(_gdata);
    sos.close();
  }

  public boolean isComplete() {
    // If we have more reactions we're not done.
    if (!_reactions.isEmpty()) {
      return false;
    }

    // If we have no reactions, but there are some channels that have external references,
    // we are not done.
    for (Iterator<ChannelFrame> i = _channels.values().iterator(); i.hasNext(); ) {
      if (i.next().refCount > 0) {
        return false;
      }
    }

    // Otherwise, we are done.
    return true;
  }

  public void dumpState(PrintStream ps) {
    ps.print(this.toString());
    ps.println(" state dump:");
    ps.println("-- GENERAL INFO");
    ps.println("   Current Cycle          : " + _currentCycle);
    ps.println("   Num. Reactions  : " + _reactions.size());
    _statistics.printStatistics(ps);
    if (!_reactions.isEmpty()) {
      ps.println("-- REACTIONS");
      int cnt =0;
      for (Iterator i = _reactions.iterator(); i.hasNext();) {
        Reaction reaction = (Reaction) i.next();
        ps.println("   #" + (++cnt) + ":  " +  reaction.toString());
      }
    }
  }

  private void matchCommunications(CommChannel channel) {
    if (__log.isTraceEnabled()){
      __log.trace(ObjectPrinter.stringifyMethodEnter("matchCommunications", new Object[] {
        "channel", channel
      }));
    }
    ChannelFrame cframe = _channels.get(channel.getId());
    while(cframe != null && !cframe.msgFrames.isEmpty() && !cframe.objFrames.isEmpty()) {
      MessageFrame mframe = cframe.msgFrames.iterator().next();
      ObjectFrame oframe = cframe.objFrames.iterator().next();

      Reaction reaction = new Reaction(oframe.continuation, oframe.continuation.getMethod(mframe.method),  mframe.args);
      if(__log.isInfoEnabled()) {
      	reaction.setDescription(channel + " ? {...} | " + channel + " ! " + mframe.method + "(...)");
      }
      enqueueReaction(reaction);
      if (!mframe.commGroupFrame.replicated) {
        removeCommGroup(mframe.commGroupFrame);
      }
      if (!oframe.commGroupFrame.replicated) {
        removeCommGroup(oframe.commGroupFrame);
      }
    }

//    // Do some cleanup, if the channel is empty we can remove it from memory.
//    if (cframe != null && cframe.msgFrames.isEmpty() && cframe.objFrames.isEmpty() && cframe.refCount ==0)
//      _channels.values().remove(cframe);
  }

  // TODO revisit: apparently dead wood
//  private JavaClosure cloneClosure(JavaClosure closure) {
//    long startTime = System.currentTimeMillis();
//    try {
//      ByteArrayOutputStream bos = new ByteArrayOutputStream(10000);
//      SoupOutputStream sos = new SoupOutputStream(bos);
//      sos.writeObject(closure);
//      sos.close();
//      long readStart = System.currentTimeMillis();
//      SoupInputStream cis = new SoupInputStream(new ByteArrayInputStream(bos.toByteArray()));
//      JavaClosure ret = (JavaClosure) cis.readObject();
//      cis.close();
//
//      long copyTime = System.currentTimeMillis() - startTime;
//      long readTime = System.currentTimeMillis() - readStart;
//      long copyBytes = bos.size();
//
//      _statistics.cloneClosureBytes += copyBytes;
//      _statistics.cloneClosureTimeMs += copyTime;
//      _statistics.cloneClosureReadTimeMs += readTime;
//      _statistics.cloneClousreCount++;
//
//      if (__log.isDebugEnabled()) {
//        __log.debug("cloneClosure(" + closure + "): serialized " + copyBytes + " bytes in " + copyTime+ "ms.");
//      }
//      return ret;
//    } catch (Exception ex) {
//      throw new RuntimeException("Internal Error in FastSoupImpl.java", ex);
//    }
//  }

  /**
   * Verify that a {@link SoupObject} is new, that is it has not already been
   * added to the soup.
   * @param so object to check.
   * @throws IllegalArgumentException in case the object is not new
   */
  private void verifyNew(SoupObject so) throws IllegalArgumentException {
    if (so.getId() != null)
      throw new IllegalArgumentException("The object " + so + " is not new!");
  }

  private void assignId(SoupObject so, Object id) {
    so.setId(id);
  }

  private void removeCommGroup(CommGroupFrame groupFrame) {
    // Add all channels reference in the group to the GC candidate set.
    for (Iterator i = groupFrame.commFrames.iterator(); i.hasNext();) {
      CommFrame frame = (CommFrame) i.next();
      if (frame instanceof ObjectFrame) {
        assert frame.channelFrame.objFrames.contains(frame);
        frame.channelFrame.objFrames.remove(frame);
      } else {
        assert frame instanceof MessageFrame;
        assert frame.channelFrame.msgFrames.contains(frame);
        frame.channelFrame.msgFrames.remove(frame);
      }
    }
  }

  public void setGlobalData(Serializable data) {
    _gdata = data;
  }

  public Serializable getGlobalData() {
    return _gdata;
  }

  private static class ChannelFrame implements Externalizable {
    Class type;

    int id;

    /** External Reference Count */
    int refCount;

    boolean replicatedSend;

    boolean replicatedRecv;

    Set<ObjectFrame> objFrames = new HashSet<ObjectFrame>();
    Set<MessageFrame> msgFrames = new HashSet<MessageFrame>();

    public String description;

    public ChannelFrame() {}
    public ChannelFrame(Class type, int id, String name, String description) {
      this.type = type;
      this.id = id;
      this.description = description;
    }

    public Integer getId() {
      return Integer.valueOf(id);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      type = (Class) in.readObject();
      id = in.readInt();
      description = in.readUTF();
      refCount = in.readInt();
      replicatedSend = in.readBoolean();
      replicatedRecv = in.readBoolean();
      int cnt = in.readInt();
      for (int i = 0; i < cnt; ++i) {
        objFrames.add((ObjectFrame)in.readObject());
      }
      cnt = in.readInt();
      for (int i = 0; i < cnt; ++i) {
        msgFrames.add((MessageFrame)in.readObject());
      }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
      out.writeObject(type);
      out.writeInt(id);
      out.writeUTF(description == null ? "" : description);
      out.writeInt(refCount);
      out.writeBoolean(replicatedSend);
      out.writeBoolean(replicatedRecv);
      out.writeInt(objFrames.size());
      for (Iterator<ObjectFrame> i = objFrames.iterator();i.hasNext(); ) {
        out.writeObject(i.next());
      }
      out.writeInt(msgFrames.size());
      for (Iterator<MessageFrame> i = msgFrames.iterator(); i.hasNext(); )
        out.writeObject(i.next());
    }

    public String toString() {
      StringBuffer buf = new StringBuffer(32);
      buf.append("{CFRAME ");
      buf.append(ObjectPrinter.getShortClassName(type));
      buf.append(':');
      buf.append(description);
      buf.append('#');
      buf.append(id);
      buf.append(" refCount=");
      buf.append(refCount);
      buf.append(", msgs=");
      buf.append(msgFrames.size());
      if (replicatedSend) {
        buf.append("R");
      }
      buf.append(", objs=");
      buf.append(objFrames.size());
      if (replicatedRecv) {
        buf.append("R");
      }
      buf.append("}");
      return buf.toString();
    }
  }

  private static class CommGroupFrame implements Serializable {
    boolean replicated;
    public Set<CommFrame> commFrames = new HashSet<CommFrame>();

    public CommGroupFrame(boolean replicated) {
      this.replicated = replicated;
    }

  }

  private static class CommFrame implements Externalizable {
    CommGroupFrame commGroupFrame;
    ChannelFrame channelFrame;

    public CommFrame() { }

    CommFrame(CommGroupFrame commGroupFrame, ChannelFrame channelFrame) {
      this.commGroupFrame = commGroupFrame;
      this.channelFrame = channelFrame;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      commGroupFrame = (CommGroupFrame) in.readObject();
      channelFrame = (ChannelFrame) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
      out.writeObject(commGroupFrame);
      out.writeObject(channelFrame);
    }
  }

  private static class ObjectFrame extends CommFrame implements Externalizable {
    ML continuation;

    public ObjectFrame() { super() ; }

    public ObjectFrame(CommGroupFrame commGroupFrame, ChannelFrame channelFrame, ML continuation) {
      super(commGroupFrame, channelFrame);
      this.continuation = continuation;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      super.readExternal(in);
      continuation = (ML)in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
      super.writeExternal(out);
      out.writeObject(continuation);
    }
  }

  private static class MessageFrame extends CommFrame implements Externalizable {
    String method;
    Object[] args;

    public MessageFrame() { super(); }

    public MessageFrame(CommGroupFrame commFrame, ChannelFrame channelFrame, String method, Object[] args) {
      super(commFrame, channelFrame);
      this.method = method;
      this.args = args == null ? ArrayUtils.EMPTY_CLASS_ARRAY : args;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      super.readExternal(in);
      method = in.readUTF();
      int numArgs = in.readInt();
      args = new Object[numArgs];
      for (int i = 0; i < numArgs ; ++i)
        args[i] = in.readObject();

    }

    public void writeExternal(ObjectOutput out) throws IOException {
      super.writeExternal(out);
      out.writeUTF(method);
      out.writeInt(args.length);
      for (int i = 0; i < args.length; ++i)
        out.writeObject(args[i]);
    }
  }


  /**
   * DOCUMENTME.
   * <p>Created on Feb 16, 2004 at 8:09:48 PM.</p>
   *
   * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
   */
  private class SoupOutputStream extends ObjectOutputStream {
    private Set<Object> _serializedChannels = new HashSet<Object>();

    public SoupOutputStream(OutputStream outputStream) throws IOException {
      super(new GZIPOutputStream(outputStream));
      enableReplaceObject(true);
    }

    public Set<Object> getSerializedChannels() {
      return _serializedChannels;
    }

    protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
    	if (Serializable.class.isAssignableFrom(desc.forClass())) {
        writeBoolean(true);
        writeUTF(desc.getName());
      } else {
        writeBoolean(false);
        super.writeClassDescriptor(desc);
      }
    }

    /**
     * Use this method to spy on any channels that are being serialized to this stream.
     * @param obj
     * @return
     * @throws IOException
     */
    protected Object replaceObject(Object obj) throws IOException {
      if (!Serializable.class.isAssignableFrom(obj.getClass()))
        return null;

      if (obj instanceof com.fs.jacob.Channel) {
        CommChannel commChannel = (CommChannel) ChannelFactory.getBackend((Channel)obj);
        _serializedChannels.add(commChannel.getId());
        return new ChannelRef(commChannel.getType(), (Integer)commChannel.getId());
      } else if (_replacementMap != null && _replacementMap.isReplaceable(obj)) {
        Object replacement = _replacementMap.getReplacement(obj);
        if (__log.isDebugEnabled())
          __log.debug("ReplacmentMap: getReplacement(" + obj + ") = " + replacement);
        return replacement;
      }

      return obj;
    }


  }

  /**
   */
  public class SoupInputStream extends ObjectInputStream {
    private Set<CommChannel> _deserializedChannels = new HashSet<CommChannel>();

    public SoupInputStream(InputStream in) throws IOException {
      super(new GZIPInputStream(in));
      enableResolveObject(true);
    }

    public Set<CommChannel> getSerializedChannels() {
      return _deserializedChannels;
    }

    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
      return Class.forName(desc.getName(), true, _classLoader);
    }

    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
    	 boolean ser = readBoolean();
       if (ser) {
         String clsName = readUTF();
         return ObjectStreamClass.lookup(Class.forName(clsName, true, _classLoader));
       }
       return super.readClassDescriptor();
    }

    protected Object resolveObject(Object obj) throws IOException {
      Object resolved;

      if (obj instanceof ChannelRef) {
        // We know this is a channel reference, so we have to resolve
        // the channel.
        ChannelRef oref = (ChannelRef) obj;
        CommChannel channel = new CommChannel(oref._type);
        channel.setId(oref._id);
        _deserializedChannels.add(channel);
        resolved = ChannelFactory.createChannel(channel, channel.getType());
      } else if (_replacementMap != null && _replacementMap.isReplacement(obj)) {
        resolved = _replacementMap.getOriginal(obj);
        if (__log.isDebugEnabled()) {
          __log.debug("ReplacementMap: getOriginal(" + obj + ") = "  + resolved);
        }
      } else {
        resolved = obj;
      }

      return resolved;
    }
  }


  private static final class ChannelRef implements Externalizable {
    private Class _type;
    private Integer _id;

    private ChannelRef(Class type, Integer id) {
      _type  = type;
      _id = id;
    }

    public ChannelRef() {}

    public boolean equals(Object obj) {
      return ((ChannelRef)obj)._id.equals(_id);
    }

    public int hashCode() {
      return _id.hashCode();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
      out.writeObject(_type);
      out.writeInt(_id.intValue());
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      _type = (Class) in.readObject();
      _id = Integer.valueOf(in.readInt());
    }
  }


  private static final class SoupStatistics {
    public long cloneClosureTimeMs;
    public long cloneClosureBytes;
    public long cloneClousreCount;
    public long cloneClosureReadTimeMs;

    public void printStatistics(PrintStream ps) {
      Field[] fields = getClass()
              .getFields();

      for (int i = 0; i < fields.length; ++i) {
        ps.print(fields[i].getName());
        ps.print(" = ");

        try {
          ps.println(fields[i].get(this));
        } catch (Exception ex) {
          ps.println(ex.toString());
        }
      }
    }
  }
}
