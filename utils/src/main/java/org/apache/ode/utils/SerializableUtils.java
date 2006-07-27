/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package org.apache.ode.utils;

import java.io.*;

/**
 * Utitlity methods for <code>Serializable</code> objects.
 */
public class SerializableUtils {

  /**
   * Clone a <code>Serializable</code> object; for use when a
   * <code>clone()</code> method is not available.
   * 
   * @param obj
   *          object to clone
   * 
   * @return clone object
   * 
   * @throws RuntimeException
   */
  public static Object cloneSerializable(Object obj) {
    Object ret = null;

    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(StreamUtils.DEFAULT_BUFFER_SIZE);
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(obj);
      oos.close();

      ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
      ObjectInputStream ois = new ObjectInputStream(bis);

      try {
        ret = ois.readObject();
      }
      catch (ClassNotFoundException cnfe) {
        assert false;
      }

      ois.close();
    }
    catch (IOException ioex) {
      throw new RuntimeException("Unable to clone object: " + obj);
    }

    return ret;
  }

  public static byte[] toBytes(Object obj) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream(StreamUtils.DEFAULT_BUFFER_SIZE);
    try {
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(obj);
    }
    catch (IOException e) {
      throw new RuntimeException("Error serializing object: " + obj.getClass() + ".", e);
    }
    return bos.toByteArray();
  }

  public static Object toObject(InputStream binaryStream, final ClassLoader cl) {
    try {
      ObjectInputStream ois = new ObjectInputStream(binaryStream) {

        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
            ClassNotFoundException {
          String name = desc.getName();
          Class c = cl.loadClass(name);
          return c;
        }
      };
      return ois.readObject();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Object toObject(byte[] arr, final ClassLoader cl) {
    ByteArrayInputStream bis = new ByteArrayInputStream(arr);
    return toObject(bis, cl);
  }

}
