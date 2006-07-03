/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment;

import com.fs.pxe.sfwk.deployment.som.SystemDescriptor;
import com.fs.pxe.sfwk.deployment.som.sax.SystemDescriptorFactory;
import com.fs.utils.sax.FailOnErrorErrorHandler;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import java.io.*;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * <p>
 * Utility class for working with pre-parsed <code>SystemDescriptor</code>s in
 * various states (serialized to <code>String</code>, etc.).
 * </p>
 * @see com.fs.pxe.sfwk.deployment.som.sax.SystemDescriptorFactory
 */
public class SystemDescriptorSerUtility {
  
  /*
   * No instance for you.
   */
  private SystemDescriptorSerUtility() {
  }

  /**
   * <p>
   * Convert a <code>SystemDescriptor</code> instance into XML text.  This includes
   * any unique identifiers or other attributes that are are set only after the
   * system is loaded for deployment.
   * </p>
   *
   *<p>
   *<em>Note:</em>  The encoding is specified as UTF-8. 
   *</p>
   *
   * @param s the <code>SystemDescriptor</code> to serialize.
   *
   * @return the XML text for the descriptor.
   *
   * @throws RuntimeException if a <code>SAXException</code> occurs while writing
   * out the descriptor.  (This is wrapped in a <code>RuntimeException</code>
   * because the <code>SAXException</code> should not occur under normal
   * circumstances.)
   */
  public static String fromSystemDescriptor(SystemDescriptor s) {
    try {
      StringWriter sw = new StringWriter();
      OutputFormat of = new OutputFormat();
      of.setEncoding("UTF-8");
      of.setMethod("XML");
      of.setIndent(2);
      of.setIndenting(true);
      XMLSerializer xs = new XMLSerializer();
      xs.setOutputCharStream(sw);
      s.toSaxEvents(xs);
      return sw.toString();
    } catch (SAXException se) {
      throw new RuntimeException(se);
    }
  }

  /**
   * <p>
   * Convert a <code>SystemDescriptor</code> to a stream of UTF-8 encoded bytes on
   * the specified <code>OutputStream</code>.
   * </p>
   *   
   * @param s the <code>SystemDescriptor</code> to serialize. 
   * @param os the <code>OutputStream</code> to serialize the descriptor to.
   * 
   * @throws IOException if one occurs while writing to the output stream
   * @throws RuntimeException if a <code>SAXException</code> occurs while writing
   * out the descriptor.  (This is wrapped in a <code>RuntimeException</code>
   * because the <code>SAXException</code> should not occur under normal
   * circumstances.)
   */
  public static void toOutputStream(SystemDescriptor s, OutputStream os)
    throws IOException
  {
    try {
      OutputFormat of = new OutputFormat();
      of.setEncoding("UTF-8");
      of.setMethod("XML");
      of.setIndent(2);
      of.setIndenting(true);
      XMLSerializer xs = new XMLSerializer();
      xs.setOutputByteStream(os);
      s.toSaxEvents(xs);
    } catch (SAXException se) {
      throw new IOException("SAX Error");
    }
  }

  
  /**
   * <p>
   * Convenience method to turn a <code>InputSource</code> representation of a system 
   * descriptor into an <code>SystemDescriptor</code> instance.
   * </p>
   * <p>
   * <em>Note:</em> This should not be used to parse descriptors from external
   * sources; it should only be used to parse representations generated from
   * known-good sources, as the descriptor is not validated or checked in any way.
   * </p>
   * 
   * @param is the byte stream to parse
   *
   * @return the parsed <code>SystemDescriptor</code> 
   *
   * @throws RuntimeException DOCUMENTME
   */
  public static SystemDescriptor toSystemDescriptor(InputStream is) throws SarFormatException, IOException {
    try {
      InputSource in = new InputSource();
      in.setSystemId("<<stream>>");
      in.setByteStream(is);
      return SystemDescriptorFactory.parseDescriptor(
          in, new FailOnErrorErrorHandler(),null,false);
    } catch (SAXException se) {
      throw new SarFormatException("Error parsing system descriptor. ", se);
    }
  }

  public static SystemDescriptor toSystemDescriptor(Reader r) throws SarFormatException, IOException {
    try {
      InputSource in = new InputSource();
      in.setSystemId("<<stream>>");
      in.setCharacterStream(r);
      return SystemDescriptorFactory.parseDescriptor(
          in, new FailOnErrorErrorHandler(),null,false);
    } catch (SAXException se) {
      throw new SarFormatException("Error parsing system descriptor. ", se);
    }
  }
  /**
   * <p>
   * Convenience method to turn a <code>String</code> representation of a system 
   * descriptor into an <code>SystemDescriptor</code> instance.
   * </p>
   * <p>
   * <em>Note:</em> This should not be used to parse descriptors from external
   * sources; it should only be used to parse representations generated from
   * known-good sources, as the descriptor is not validated or checked in any way.
   * </p>
   *
   * @param s the content of the descriptor.
   *
   * @return a <code>SystemDescriptor</code>.
   *
   * @throws RuntimeException DOCUMENTME
   * @see SystemDescriptorFactory
   */
  public static SystemDescriptor toSystemDescriptor(String s) {
    StringReader sr = new StringReader(s);
    InputSource is = new InputSource();
    is.setCharacterStream(sr);
    is.setSystemId("<<string>>");
    try {
      return SystemDescriptorFactory.parseDescriptor(
          is,new FailOnErrorErrorHandler(),null,false);
    } catch (SAXException se) {
      throw new RuntimeException(se);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
}
