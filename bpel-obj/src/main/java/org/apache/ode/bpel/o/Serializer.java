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

import javax.xml.namespace.QName;
import java.io.*;
import java.util.Arrays;

/**
 * Header written at the beginning of every compiled BPEL object file.
 */
public class Serializer  {

    public static final byte[] MAGIC_NUMBER_OFH_20040908 =
            new byte[]  { 0x55, '5', 'S', 0x00, 'O', 'F', 'H', 0x20, 0x04, 0x09, 0x08  };

    public static final byte[] MAGIC_NUMBER_OFH_20061101 =
        new byte[]  { 0x55, '5', 'S', 0x00, 'O', 'F', 'H', 0x20, 0x06, 0x11, 0x01  };

    public static final byte[] MAGIC_NUMBER = MAGIC_NUMBER_OFH_20061101;

    public static final short FORMAT_SERIALIZED_JAVA14 = 0x01;

    // START PERSISTED FIELDS
    public final byte[] magic = new byte[MAGIC_NUMBER.length];

    /** Compiled Process representation format. */
    public short format;

    /** Time of compilation (system local time). */
    public long compileTime;

    /** Deprecated, only one process per file.  */
    public final int numProcesses = 1;

    public InputStream _inputStream;

    public String guid;

//    public OProcess _oprocess;

    public QName type;


  // END PERSISTED FIELDS

    public Serializer(long compileTime) {
        System.arraycopy(MAGIC_NUMBER, 0, magic, 0, MAGIC_NUMBER.length);
        this.format = FORMAT_SERIALIZED_JAVA14;
        this.compileTime  = compileTime;
    }

    public Serializer() {}

    public Serializer(InputStream inputStream) throws IOException {
        _inputStream = inputStream;
        read(inputStream);
    }


    public void read(InputStream is) throws IOException {
        DataInputStream oin = new DataInputStream(is);
        byte[] magic = new byte[MAGIC_NUMBER.length];
        oin.read(magic, 0, magic.length);

        if (Arrays.equals(MAGIC_NUMBER_OFH_20040908, magic)) {
            // Old format requires us to read the OModel to get the type and guid.
            this.format = oin.readShort();
            this.compileTime = oin.readLong();
            oin.readInt();
            ObjectInputStream ois = new CustomObjectInputStream(_inputStream);
            OProcess oprocess;
            try {
                oprocess = (OProcess) ois.readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException("DataStream Error");
            }
            this.type  = new QName(oprocess.targetNamespace, oprocess.processName);
            this.guid = "OLD-FORMAT-NO-GUID";

            return;
        }
        // The current (most recent) scheme
        if (Arrays.equals(MAGIC_NUMBER, magic)) {
            this.format = oin.readShort();
            this.compileTime = oin.readLong();
            this.guid = oin.readUTF();
            String tns = oin.readUTF();
            String name = oin.readUTF();
            this.type = new QName(tns, name);
            return;
        }

        throw new IOException("Unrecognized file format (bad magic number).");
    }

    public void writeOProcess(OProcess process, OutputStream os) throws IOException {
        DataOutputStream out = new DataOutputStream(os);

        out.write(MAGIC_NUMBER);
        out.writeShort(format);
        out.writeLong(compileTime);
        out.writeUTF(process.guid);
        out.writeUTF(process.targetNamespace);
        out.writeUTF(process.processName);
        out.flush();
        ObjectOutputStream oos = new CustomObjectOutputStream(os);
        oos.writeObject(process);
        oos.flush();
    }

    public OProcess readOProcess() throws IOException, ClassNotFoundException {
//        if (_oprocess != null)
//            return _oprocess;

        ObjectInputStream ois = new CustomObjectInputStream(_inputStream);
        OProcess oprocess;
        try {
            oprocess = (OProcess) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("DataStream Error");
        } finally {
            try {
                if (ois != null)
                    ois.close();
            } catch (IOException e) {

            }
        }

        return oprocess;
    }

    static class CustomObjectOutputStream extends ObjectOutputStream {

        /**
         * @param out
         * @throws IOException
         */
        public CustomObjectOutputStream(OutputStream out) throws IOException {
            super(out);
            enableReplaceObject(true);
        }

        protected Object replaceObject(Object obj) throws IOException{
            if(obj instanceof QName){
                QName q = (QName)obj;
                return new OQName(q.getNamespaceURI(), q.getLocalPart(), q.getPrefix());
            }
            return super.replaceObject(obj);
        }

    }

    static class CustomObjectInputStream extends ObjectInputStream {

        /**
         * @param in
         * @throws IOException
         */
        public CustomObjectInputStream(InputStream in) throws IOException {
            super(in);
            enableResolveObject(true);
        }

        protected Object resolveObject(Object obj) throws IOException {
            if(obj instanceof OQName){
                OQName q = (OQName)obj;
                return new QName(q.uri, q.local, q.prefix);
            }
            return super.resolveObject(obj);
        }

        /**
         * Override coverts old class names into new class names to preserve compatibility with
         * pre-Apache namespaces.
         */
        @Override
        protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
            ObjectStreamClass read = super.readClassDescriptor();
            if (read.getName().startsWith("com.fs.pxe.")) {
                return ObjectStreamClass.lookup(Class.forName(read.getName().replace("com.fs.pxe.", "org.apache.ode.")));
            }
            if (read.getName().startsWith("com.fs.utils.")) {
                return ObjectStreamClass.lookup(Class.forName(read.getName().replace("com.fs.utils.", "org.apache.ode.utils.")));
            }
            return read;
        }

    }

    static class OQName implements Serializable{

        private static final long serialVersionUID = 1L;

        final String local;
        final String uri;
        final String prefix;

        OQName(String uri, String local, String prefix){
            this.uri = uri;
            this.local = local;
            this.prefix = prefix;
        }
    }
}
