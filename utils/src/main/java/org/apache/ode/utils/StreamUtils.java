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
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Utilities for dealing with byte ({@link java.io.InputStream} and
 * {@link java.io.OutputStream}) and character ({@link java.io.Reader} and
 * {@link java.io.Writer}) streams.
 */
public class StreamUtils {

  /** The default size of the byte buffer used in the {@link #copy} methods. */
  public static final int DEFAULT_BUFFER_SIZE = 16384;

  /**
   * Read the contents of the given URL into a byte array.
   * 
   * @param input
   *          the URL to read
   * @return an array of bytes.
   * @throws IOException
   *           in case of I/O error
   */
  public static byte[] read(URL input) throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
    copy(byteStream, input);
    return byteStream.toByteArray();
  }

  /**
   * Read the contents of the given InputStream into a byte array.
   * 
   * @param input
   *          the InputStream to read
   * @return an array of bytes.
   * @throws IOException
   *           in case of I/O error
   */
  public static byte[] read(InputStream source) throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream(DEFAULT_BUFFER_SIZE);
    copy(byteStream, source);
    return byteStream.toByteArray();
  }

  /**
   * Copy data from an {@link InputStream} to an {@link OutputStream} until an
   * end-of-stream is reached on the source {@link InputStream}. This method
   * does <em>not</em> attempt to close either the source or the destination
   * stream.
   * 
   * @param dest
   *          destination {@link OutputStream}
   * @param source
   *          source {@link InputStream}
   * @param bufSize
   *          write buffer size
   * @throws IOException
   *           in case of I/O error
   */
  public static void copy(OutputStream dest, InputStream source, int bufSize) throws IOException {
    byte[] buf = new byte[bufSize];
    int read;
    while ((read = source.read(buf)) != -1) {
      dest.write(buf, 0, read);
    }

  }

  /**
   * Copy data from an {@link Reader} to an {@link Writer} until an
   * end-of-stream is reached on the source {@link Reader}. This method does
   * <em>not</em> attempt to close either the source or the destination
   * stream.
   * 
   * @param dest
   *          destination {@link Writer}
   * @param source
   *          source {@link Reader}
   * @param bufSize
   *          write buffer size
   * @throws IOException
   *           in case of I/O error
   */
  public static void copy(Writer dest, Reader source, int bufSize) throws IOException {
    char[] buf = new char[bufSize];
    int read;
    while ((read = source.read(buf)) != -1) {
      dest.write(buf, 0, read);
    }
  }

  /**
   * Copy with default buffer size.
   * 
   * @see #copy(java.io.OutputStream, java.io.InputStream)
   * @see #DEFAULT_BUFFER_SIZE
   */
  public static void copy(OutputStream dest, InputStream source) throws IOException {
    copy(dest, source, DEFAULT_BUFFER_SIZE);
  }

  /**
   * Copy from {@link URL} stream.
   * 
   * @see #copy(java.io.OutputStream, java.io.InputStream, int)
   * @param dest
   *          destination {@link OutputStream}
   * @param source
   *          source {@link URL}
   */
  public static void copy(OutputStream dest, URL source) throws IOException {
    InputStream urlStream = source.openStream();
    copy(dest, urlStream);
    urlStream.close();
  }

  /**
   * Write a {@link Serializable} object to an output stream using the
   * {@link ObjectOutputStream} mechanism.
   * 
   * @param dest
   *          destination {@link OutputStream}
   * @param src
   *          source {@link Serializable}
   * @throws IOException
   */
  public static void write(OutputStream dest, Serializable src) throws IOException {
    ObjectOutputStream oos = new ObjectOutputStream(dest);
    oos.writeObject(src);
    oos.flush();
  }

  public static Object readObj(InputStream is) throws IOException,ClassNotFoundException {
      ObjectInputStream iis = new ObjectInputStream(is);
      return iis.readObject();
  }
  
  /**
   * Expand a Jar input stream.
   */
  public static void extractJar(File dest, InputStream is) throws IOException {
    JarInputStream jis = new JarInputStream(is);
    JarEntry je;
    while ((je = jis.getNextJarEntry()) != null) {
      File outputFile = new File(dest, je.getName());
      if (je.isDirectory()) {
        outputFile.mkdirs();
      }
      else {
        outputFile.getParentFile().mkdirs();
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputFile));
        copy(bos, jis);
        bos.flush();
        bos.close();
      }
      jis.closeEntry();
    }
  }

}
