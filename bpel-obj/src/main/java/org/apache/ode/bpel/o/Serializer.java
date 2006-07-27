/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.o;

import java.io.*;
import java.util.Arrays;

import javax.xml.namespace.QName;

/**
 * Header written at the beginning of every compiled BPEL object file.
 */
public class Serializer  {

  public static final byte[] MAGIC_NUMBER_BARFILE_PRE20040908 =
          new byte[] { 0x50, 0x4b, 0x03, 0x04  };

  public static final byte[] MAGIC_NUMBER_OFH_20040908 =
          new byte[]  { 0x55, '5', 'S', 0x00, 'O', 'F', 'H', 0x20, 0x04, 0x09, 0x08  };

  public static final byte[] MAGIC_NUMBER = MAGIC_NUMBER_OFH_20040908;

  public static final short FORMAT_SERIALIZED_JAVA14 = 0x01;


  public static final short FORMAT_OLD_BAR = 0x02;

  // START PERSISTED FIELDS
  public final byte[] magic = new byte[MAGIC_NUMBER.length];

  /** Compiled Process representation format. */
  public short format;

  /** Time of compilation (system local time). */
  public long compileTime;

  /** Number of compiled processes in this file. */
  public int numProcesses;
  private InputStream _inputStream;


  // END PERSISTED FIELDS

  public Serializer(long compileTime, int numProcesses) {
    System.arraycopy(MAGIC_NUMBER, 0, magic, 0, MAGIC_NUMBER.length);
    this.format = FORMAT_SERIALIZED_JAVA14;
    this.compileTime  = compileTime;
    this.numProcesses = numProcesses;
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

    // Check old (BAR-file) encoding scheme
    if (Arrays.equals(MAGIC_NUMBER_BARFILE_PRE20040908, magic)) {
      this.format = FORMAT_OLD_BAR;
      this.compileTime = 0;
      this.numProcesses = 1;
      return;
    }

    // The current (most recent) scheme
    if (Arrays.equals(MAGIC_NUMBER, magic)) {
      this.format = oin.readShort();
      this.compileTime = oin.readLong();
      this.numProcesses = oin.readInt();
      return;
    }

    throw new IOException("Unrecognized file format (bad magic number).");
  }

  public void write(OutputStream os) throws IOException {
    
    DataOutputStream out = new DataOutputStream(os);

    out.write(MAGIC_NUMBER);
    out.writeShort(format);
    out.writeLong(compileTime);
    out.writeInt(numProcesses);
    out.flush();
  }
  
  public void writeOProcess(OProcess process, OutputStream os) throws IOException{
  	ObjectOutputStream oos = new CustomObjectOutputStream(os);
    oos.writeObject(process);
    oos.flush();
  }

  public OProcess readOProcess() throws IOException, ClassNotFoundException {
    ObjectInputStream ois = new CustomObjectInputStream(_inputStream);
    return (OProcess) ois.readObject();
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
  }
  
  static class OQName implements Serializable{
     
      private static final long serialVersionUID = 1L; 
      
      final String local;
      final String uri;
      final String prefix;
      
      /**
       * @param localPart
       */
      OQName(String uri, String local, String prefix){
        this.uri = uri;
        this.local = local;
        this.prefix = prefix;
      }
    }
}
