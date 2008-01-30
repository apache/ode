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

        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
          String name = desc.getName();
          try {
              return Class.forName(name, false, cl);
          } catch (ClassNotFoundException ex) {
              return super.resolveClass(desc);
          }
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
