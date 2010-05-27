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

package org.apache.ode.bpel.o;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;

import static org.junit.Assert.*;
import org.junit.Test;

public class SerializerTest {
    @Test
    public void testOnMessageBackwardCompatibility() throws Exception {
        OPickReceive.OnMessage onMessage = new OPickReceive.OnMessage(null);

        // had a value in matchCorrelation variable
        OScope.CorrelationSet cset1 = new OScope.CorrelationSet(null);
        cset1.name = "cset1";
        Field matchCorrelationField = OPickReceive.OnMessage.class.getDeclaredField("matchCorrelation");
        matchCorrelationField.setAccessible(true);
        matchCorrelationField.set(onMessage, cset1);

        // joinCorrelations variable was not defined
        Field joinCorrelationsField = OPickReceive.OnMessage.class.getDeclaredField("joinCorrelations");
        joinCorrelationsField.setAccessible(true);
        joinCorrelationsField.set(onMessage, null);

        // had a value in joinCorrelation variable
        OScope.CorrelationSet cset2 = new OScope.CorrelationSet(null);
        cset2.name = "cset2";
        Field joinCorrelationField = OPickReceive.OnMessage.class.getDeclaredField("joinCorrelation");
        joinCorrelationField.setAccessible(true);
        joinCorrelationField.set(onMessage, cset2);

        ObjectOutputStream os = null;
        ObjectInputStream is = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            os = new ObjectOutputStream(baos);
            os.writeObject(onMessage);

            is = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            onMessage = (OPickReceive.OnMessage)is.readObject();
            assertEquals("The value in 'matchCorrelation' member should've been transferred to 'matchCorrelations'",
                    "cset1", onMessage.matchCorrelations.get(0).name);
            assertNotNull("'joinCorrelations' member cannot be null", onMessage.joinCorrelations);
            assertEquals("The value in 'joinCorrelation' member should've been transferred to 'joinCorrelations'",
                    "cset2", onMessage.joinCorrelations.get(0).name);
        } finally {
            if( os != null ) os.close();
            if( is != null ) is.close();
        }
    }

    @Test
    public void testOEventBackwardCompatibility() throws Exception {
        OEventHandler.OEvent oEvent = new OEventHandler.OEvent(null, null);

        OScope.CorrelationSet cset1 = new OScope.CorrelationSet(null);
        cset1.name = "cset1";
        Field matchCorrelationField = OEventHandler.OEvent.class.getDeclaredField("matchCorrelation");
        matchCorrelationField.setAccessible(true);
        matchCorrelationField.set(oEvent, cset1);

        Field joinCorrelationsField = OEventHandler.OEvent.class.getDeclaredField("joinCorrelations");
        joinCorrelationsField.setAccessible(true);
        joinCorrelationsField.set(oEvent, null);

        OScope.CorrelationSet cset2 = new OScope.CorrelationSet(null);
        cset2.name = "cset2";
        Field joinCorrelationField = OEventHandler.OEvent.class.getDeclaredField("joinCorrelation");
        joinCorrelationField.setAccessible(true);
        joinCorrelationField.set(oEvent, cset2);

        ObjectOutputStream os = null;
        ObjectInputStream is = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            os = new ObjectOutputStream(baos);
            os.writeObject(oEvent);

            is = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            oEvent = (OEventHandler.OEvent)is.readObject();
            assertEquals("The value in 'matchCorrelation' member should've been transferred to 'matchCorrelations'",
                    "cset1", oEvent.matchCorrelations.get(0).name);
            assertNotNull("'joinCorrelations' member cannot be null", oEvent.joinCorrelations);
            assertEquals("The value in 'joinCorrelation' member should've been transferred to 'joinCorrelations'",
                    "cset2", oEvent.joinCorrelations.get(0).name);
        } finally {
            if( os != null ) os.close();
            if( is != null ) is.close();
        }
    }

    @Test
    public void testOReplyBackwardCompatibility() throws Exception {
        OReply reply = new OReply(null, null);

        Field joinCorrelationsField = OReply.class.getDeclaredField("joinCorrelations");
        joinCorrelationsField.setAccessible(true);
        joinCorrelationsField.set(reply, null);

        ObjectOutputStream os = null;
        ObjectInputStream is = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            os = new ObjectOutputStream(baos);
            os.writeObject(reply);

            is = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            reply = (OReply)is.readObject();
            assertNotNull("'joinCorrelations' member cannot be null", reply.joinCorrelations);
        } finally {
            if( os != null ) os.close();
            if( is != null ) is.close();
        }
    }

    @Test
    public void testOInvokeBackwardCompatibility() throws Exception {
        OInvoke invoke = new OInvoke(null, null);

        Field joinCorrelationsField = OInvoke.class.getDeclaredField("joinCorrelationsInput");
        joinCorrelationsField.setAccessible(true);
        joinCorrelationsField.set(invoke, null);
        joinCorrelationsField = OInvoke.class.getDeclaredField("joinCorrelationsOutput");
        joinCorrelationsField.setAccessible(true);
        joinCorrelationsField.set(invoke, null);

        ObjectOutputStream os = null;
        ObjectInputStream is = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            os = new ObjectOutputStream(baos);
            os.writeObject(invoke);

            is = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            invoke = (OInvoke)is.readObject();
            assertNotNull("'joinCorrelationsInput' member cannot be null", invoke.joinCorrelationsInput);
            assertNotNull("'joinCorrelationsOutput' member cannot be null", invoke.joinCorrelationsOutput);
        } finally {
            if( os != null ) os.close();
            if( is != null ) is.close();
        }
    }
}