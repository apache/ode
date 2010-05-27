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
import org.apache.ode.jacob.IndexedObject;
import org.apache.ode.jacob.JacobObject;
import org.apache.ode.jacob.soup.Comm;
import org.apache.ode.jacob.soup.CommChannel;
import org.apache.ode.jacob.soup.CommGroup;
import org.apache.ode.jacob.soup.CommRecv;
import org.apache.ode.jacob.soup.CommSend;
import org.apache.ode.jacob.soup.Continuation;
import org.apache.ode.jacob.soup.ExecutionQueue;
import org.apache.ode.jacob.soup.ExecutionQueueObject;
import org.apache.ode.jacob.soup.ReplacementMap;
import org.apache.ode.utils.CollectionUtils;
import org.apache.ode.utils.ObjectPrinter;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A fast, in-memory {@link org.apache.ode.jacob.soup.ExecutionQueue} implementation.
 */
public class ExecutionQueueImpl implements ExecutionQueue {
    /** Class-level logger. */
    private static final Log __log = LogFactory.getLog(ExecutionQueueImpl.class);

    private ClassLoader _classLoader;

    public static ConcurrentHashMap<String, ObjectStreamClass> _classDescriptors
        = new ConcurrentHashMap<String, ObjectStreamClass>();

    /**
     * Cached set of enqueued {@link Continuation} objects (i.e. those read using
     * {@link #enqueueReaction(org.apache.ode.jacob.soup.Continuation)}).
     * These reactions are "cached"--that is it is not sent directly to the DAO
     * layer--to minimize unnecessary serialization/deserialization of closures.
     * This is a pretty useful optimization, as most {@link Continuation}s are
     * enqueued, and then immediately dequeued in the next cycle. By caching
     * {@link Continuation}s, we eliminate practically all serialization of
     * these objects, the only exception being cases where the system decides to
     * stop processing a particular soup despite the soup being able to make
     * forward progress; this scenario would occur if a maximum processign
     * time-per-instance policy were in effect.
     */
    private Set<Continuation> _reactions = new HashSet<Continuation>();

    private Map<Integer, ChannelFrame> _channels = new HashMap<Integer, ChannelFrame>();

    /**
     * The "expected" cycle counter, use to detect database serialization
     * issues.
     */
    private int _currentCycle;

    private int _objIdCounter;

    private ExecutionQueueStatistics _statistics = new ExecutionQueueStatistics();

    private ReplacementMap _replacementMap;

    private Serializable _gdata;

    private Map<Object, LinkedList<IndexedObject>> _index = new HashMap<Object, LinkedList<IndexedObject>>();

    public ExecutionQueueImpl(ClassLoader classLoader) {
        _classLoader = classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        _classLoader = classLoader;
    }

    public void setReplacementMap(ReplacementMap replacementMap) {
        _replacementMap = replacementMap;
    }

    public Map<Object, LinkedList<IndexedObject>> getIndex() {
        return _index;
    }

    public void add(CommChannel channel) {
        if (__log.isTraceEnabled())
            __log.trace(ObjectPrinter.stringifyMethodEnter("add", new Object[] { "channel", channel }));

        verifyNew(channel);
        ChannelFrame cframe = new ChannelFrame(channel.getType(), ++_objIdCounter, channel.getType().getName(), channel
                .getDescription());
        _channels.put(cframe.getId(), cframe);
        assignId(channel, cframe.getId());
    }

    public void enqueueReaction(Continuation continuation) {
        if (__log.isTraceEnabled())
            __log.trace(ObjectPrinter.stringifyMethodEnter("enqueueReaction", new Object[] { "continuation",
                    continuation }));

        verifyNew(continuation);
        _reactions.add(continuation);
    }

    public Continuation dequeueReaction() {
        if (__log.isTraceEnabled()) {
            __log.trace(ObjectPrinter.stringifyMethodEnter("dequeueReaction", CollectionUtils.EMPTY_OBJECT_ARRAY));
        }

        Continuation continuation = null;
        if (!_reactions.isEmpty()) {
            Iterator it = _reactions.iterator();
            continuation = (Continuation) it.next();
            it.remove();
        }
        return continuation;
    }

    public void add(CommGroup group) {
        if (__log.isTraceEnabled())
            __log.trace(ObjectPrinter.stringifyMethodEnter("add", new Object[] { "group", group }));

        verifyNew(group);
        CommGroupFrame commGroupFrame = new CommGroupFrame(group.isReplicated());
        for (Iterator i = group.getElements(); i.hasNext();) {
            Comm comm = (Comm) i.next();
            ChannelFrame chnlFrame = findChannelFrame(comm.getChannel().getId());
            if (comm instanceof CommSend) {
                if (chnlFrame.replicatedSend) {
                    // TODO: JACOB "bad-process" ex
                    throw new IllegalStateException("Send attempted on channel containing replicated send! Channel= "
                            + comm.getChannel());
                }
                if (group.isReplicated())
                    chnlFrame.replicatedSend = true;

                CommSend commSend = (CommSend) comm;
                MessageFrame mframe = new MessageFrame(commGroupFrame, chnlFrame, commSend.getMethod().getName(),
                        commSend.getArgs());
                commGroupFrame.commFrames.add(mframe);
                chnlFrame.msgFrames.add(mframe);
            } else if (comm instanceof CommRecv) {
                if (chnlFrame.replicatedRecv) {
                    // TODO: JACOB "bad-process" ex
                    throw new IllegalStateException(
                            "Receive attempted on channel containing replicated receive! Channel= " + comm.getChannel());
                }
                if (group.isReplicated())
                    chnlFrame.replicatedRecv = true;
                CommRecv commRecv = (CommRecv) comm;
                ObjectFrame oframe = new ObjectFrame(commGroupFrame, chnlFrame, commRecv.getContinuation());
                commGroupFrame.commFrames.add(oframe);
                chnlFrame.objFrames.add(oframe);
            }
        }

        // Match communications.
        for (Iterator i = group.getElements(); i.hasNext();) {
            Comm comm = (Comm) i.next();
            matchCommunications(comm.getChannel());
        }
    }

    private ChannelFrame findChannelFrame(Object id) {
        ChannelFrame chnlFrame = _channels.get(id);
        if (chnlFrame == null) {
            throw new IllegalArgumentException("No such channel; id=" + id);
        }
        return chnlFrame;
    }

    public int cycle() {
        if (__log.isTraceEnabled()) {
            __log.trace(ObjectPrinter.stringifyMethodEnter("cycle", CollectionUtils.EMPTY_OBJECT_ARRAY));
        }

        return ++_currentCycle;
    }

    public String createExport(CommChannel channel) {
        if (__log.isTraceEnabled())
            __log.trace(ObjectPrinter.stringifyMethodEnter("createExport", new Object[] { "channel", channel }));
        ChannelFrame cframe = findChannelFrame(channel.getId());
        cframe.refCount++;
        return channel.getId().toString();
    }

    public CommChannel consumeExport(String exportId) {
        if (__log.isTraceEnabled()) {
            __log.trace(ObjectPrinter.stringifyMethodEnter("consumeExport", new Object[] { "exportId", exportId }));
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
        return !_reactions.isEmpty();
    }

    public void flush() {
        if (__log.isTraceEnabled()) {
            __log.trace(ObjectPrinter.stringifyMethodEnter("flush", CollectionUtils.EMPTY_OBJECT_ARRAY));
        }
    }

    public void read(InputStream iis) throws IOException, ClassNotFoundException {
        _channels.clear();
        _reactions.clear();
        _index.clear();

        ExecutionQueueInputStream sis = new ExecutionQueueInputStream(iis);

        _objIdCounter = sis.readInt();
        _currentCycle = sis.readInt();
        int reactions = sis.readInt();
        for (int i = 0; i < reactions; ++i) {
            JacobObject closure = (JacobObject) sis.readObject();
            String methodName = sis.readUTF();
            Method method = closure.getMethod(methodName);
            int numArgs = sis.readInt();
            Object[] args = new Object[numArgs];
            for (int j = 0; j < numArgs; ++j) {
                args[j] = sis.readObject();
            }
            _reactions.add(new Continuation(closure, method, args));
        }

        int numChannels = sis.readInt();
        for (int i = 0; i < numChannels; ++i) {
            int objFrames = sis.readInt();
            for (int j = 0; j < objFrames; ++j) {
                sis.readObject();
            }
            int msgFrames = sis.readInt();
            for (int j = 0; j < msgFrames; ++j) {
                sis.readObject();
            }
        }

        numChannels = sis.readInt();
        for (int i = 0; i < numChannels; ++i) {
            ChannelFrame cframe = (ChannelFrame) sis.readObject();
            _channels.put(cframe.getId(), cframe);
        }
        _gdata = (Serializable) sis.readObject();
        sis.close();
    }

    private void index(IndexedObject object) {
        LinkedList<IndexedObject> vals = _index.get(object.getKey());
        if (vals == null) {
            vals = new LinkedList<IndexedObject>();
            _index.put(object.getKey(), vals);
        }
        vals.add(object);
    }

    public void write(OutputStream oos) throws IOException {
        flush();

        ExecutionQueueOutputStream sos = new ExecutionQueueOutputStream(oos);
//        XQXMLOutputStream sos = createObjectOutputStream(new OutputStreamWriter(oos));

        sos.writeInt(_objIdCounter);
        sos.writeInt(_currentCycle);

        // Write out the reactions.
        sos.writeInt(_reactions.size());
        for (Continuation c : _reactions) {
            sos.writeObject(c.getClosure());
            sos.writeUTF(c.getMethod().getName());
            sos.writeInt(c.getArgs() == null ? 0 : c.getArgs().length);
            for (int j = 0; c.getArgs() != null && j < c.getArgs().length; ++j)
                sos.writeObject(c.getArgs()[j]);
        }

        sos.writeInt(_channels.values().size());
        for (Iterator i = _channels.values().iterator(); i.hasNext();) {
            ChannelFrame cframe = (ChannelFrame) i.next();
            sos.writeInt(cframe.objFrames.size());
            for (Iterator j = cframe.objFrames.iterator(); j.hasNext();) {
                sos.writeObject(j.next());
            }
            sos.writeInt(cframe.msgFrames.size());
            for (Iterator j = cframe.msgFrames.iterator(); j.hasNext();) {
                sos.writeObject(j.next());
            }
        }

        Set referencedChannels = sos.getSerializedChannels();
        for (Iterator i = _channels.values().iterator(); i.hasNext();) {
            ChannelFrame cframe = (ChannelFrame) i.next();
            if (referencedChannels.contains(Integer.valueOf(cframe.id)) || cframe.refCount > 0) {
                // skip
            } else {
                if (__log.isDebugEnabled())
                    __log.debug("GC Channel: " + cframe);
                i.remove();
            }

        }

        sos.writeInt(_channels.values().size());
        for (Iterator i = _channels.values().iterator(); i.hasNext();) {
            ChannelFrame cframe = (ChannelFrame) i.next();
            if (__log.isDebugEnabled()) {
                __log.debug("Writing Channel: " + cframe);
            }
            sos.writeObject(cframe);
        }

        // Write the global data.
        sos.writeObject(_gdata);
        sos.close();
    }

    public boolean isComplete() {
        // If we have more reactions we're not done.
        if (!_reactions.isEmpty()) {
            return false;
        }

        // If we have no reactions, but there are some channels that have
        // external references, we are not done.
        for (Iterator<ChannelFrame> i = _channels.values().iterator(); i.hasNext();) {
            if (i.next().refCount > 0) {
                return false;
            }
        }
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
            int cnt = 0;
            for (Iterator i = _reactions.iterator(); i.hasNext();) {
                Continuation continuation = (Continuation) i.next();
                ps.println("   #" + (++cnt) + ":  " + continuation.toString());
            }
        }
    }

    private void matchCommunications(CommChannel channel) {
        if (__log.isTraceEnabled()) {
            __log.trace(ObjectPrinter.stringifyMethodEnter("matchCommunications", new Object[] { "channel", channel }));
        }
        ChannelFrame cframe = _channels.get(channel.getId());
        while (cframe != null && !cframe.msgFrames.isEmpty() && !cframe.objFrames.isEmpty()) {
            MessageFrame mframe = cframe.msgFrames.iterator().next();
            ObjectFrame oframe = cframe.objFrames.iterator().next();

            Continuation continuation = new Continuation(oframe._continuation, oframe._continuation
                    .getMethod(mframe.method), mframe.args);
            if (__log.isInfoEnabled()) {
                continuation.setDescription(channel + " ? {...} | " + channel + " ! " + mframe.method + "(...)");
            }
            enqueueReaction(continuation);
            if (!mframe.commGroupFrame.replicated) {
                removeCommGroup(mframe.commGroupFrame);
            }
            if (!oframe.commGroupFrame.replicated) {
                removeCommGroup(oframe.commGroupFrame);
            }
        }

        // Do some cleanup, if the channel is empty we can remove it from memory.
        // if (cframe != null && cframe.msgFrames.isEmpty() &&
        // cframe.objFrames.isEmpty() && cframe.refCount ==0)
        // _channels.values().remove(cframe);
    }

    /**
     * Verify that a {@link ExecutionQueueObject} is new, that is it has not
     * already been added to the soup.
     *
     * @param so object to check.
     * @throws IllegalArgumentException in case the object is not new
     */
    private void verifyNew(ExecutionQueueObject so) throws IllegalArgumentException {
        if (so.getId() != null)
            throw new IllegalArgumentException("The object " + so + " is not new!");
    }

    private void assignId(ExecutionQueueObject so, Object id) {
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

        public ChannelFrame() {
        }

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
                objFrames.add((ObjectFrame) in.readObject());
            }
            cnt = in.readInt();
            for (int i = 0; i < cnt; ++i) {
                msgFrames.add((MessageFrame) in.readObject());
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
            for (Iterator<ObjectFrame> i = objFrames.iterator(); i.hasNext();) {
                out.writeObject(i.next());
            }
            out.writeInt(msgFrames.size());
            for (Iterator<MessageFrame> i = msgFrames.iterator(); i.hasNext();)
                out.writeObject(i.next());
        }

        public String toString() {
            StringBuffer buf = new StringBuffer(32);
            buf.append("{CFRAME ");
            buf.append(type.getSimpleName());
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

        public CommFrame() {
        }

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
        private static final long serialVersionUID = -7212430608484116919L;

        ChannelListener _continuation;

        public ObjectFrame() {
            super();
        }

        public ObjectFrame(CommGroupFrame commGroupFrame, ChannelFrame channelFrame, ChannelListener continuation) {
            super(commGroupFrame, channelFrame);
            this._continuation = continuation;
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            _continuation = (ChannelListener) in.readObject();
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal(out);
            out.writeObject(_continuation);
        }
    }

    private static class MessageFrame extends CommFrame implements Externalizable {
        private static final long serialVersionUID = -1112437852498126297L;

        String method;

        Object[] args;

        public MessageFrame() {
            super();
        }

        public MessageFrame(CommGroupFrame commFrame, ChannelFrame channelFrame, String method, Object[] args) {
            super(commFrame, channelFrame);
            this.method = method;
            this.args = args == null ? CollectionUtils.EMPTY_CLASS_ARRAY : args;
        }

        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            method = in.readUTF();
            int numArgs = in.readInt();
            args = new Object[numArgs];
            for (int i = 0; i < numArgs; ++i)
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
     * <p>
     * Created on Feb 16, 2004 at 8:09:48 PM.
     * </p>
     *
     * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
     */
    private class ExecutionQueueOutputStream extends ObjectOutputStream {
        private Set<Object> _serializedChannels = new HashSet<Object>();

        public ExecutionQueueOutputStream(OutputStream outputStream) throws IOException {
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
         * Use this method to spy on any channels that are being serialized to
         * this stream.
         *
         * @param obj
         * @return
         * @throws IOException
         */
        protected Object replaceObject(Object obj) throws IOException {
            if (!Serializable.class.isAssignableFrom(obj.getClass()))
                return null;

            if (obj instanceof org.apache.ode.jacob.Channel) {
                CommChannel commChannel = (CommChannel) ChannelFactory.getBackend((Channel) obj);
                _serializedChannels.add(commChannel.getId());
                return new ChannelRef(commChannel.getType(), (Integer) commChannel.getId());
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
    public class ExecutionQueueInputStream extends ObjectInputStream {
        private Set<CommChannel> _deserializedChannels = new HashSet<CommChannel>();

        public ExecutionQueueInputStream(InputStream in) throws IOException {
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
                ObjectStreamClass cached = _classDescriptors.get(clsName);
                if (cached == null) {
                    cached = ObjectStreamClass.lookup(Class.forName(clsName, true, _classLoader));
                    _classDescriptors.put(clsName, cached);
                }
                return cached;
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
                    __log.debug("ReplacementMap: getOriginal(" + obj + ") = " + resolved);
                }
            } else {
                resolved = obj;
            }

            if (resolved != null && resolved instanceof IndexedObject)
                index((IndexedObject) resolved);

            return resolved;
        }
    }

    private static final class ChannelRef implements Externalizable {
        private Class _type;

        private Integer _id;

        private ChannelRef(Class type, Integer id) {
            _type = type;
            _id = id;
        }

        public ChannelRef() {
        }

        public boolean equals(Object obj) {
            return ((ChannelRef) obj)._id.equals(_id);
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

    private static final class ExecutionQueueStatistics {
        public long cloneClosureTimeMs;

        public long cloneClosureBytes;

        public long cloneClousreCount;

        public long cloneClosureReadTimeMs;

        public void printStatistics(PrintStream ps) {
            Field[] fields = getClass().getFields();

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
