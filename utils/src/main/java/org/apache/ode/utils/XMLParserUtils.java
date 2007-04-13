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

import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * A collection of utility methods for the Apache Xerces XML parser.
 */
public class XMLParserUtils {
  
  public static final String NAMESPACES_SAXF = 
    "http://xml.org/sax/features/namespaces";
  public static final String VALIDATION_SAXF =
    "http://xml.org/sax/features/validation";
  public static final String SCHEMA_V_XERCESF =
    "http://apache.org/xml/features/validation/schema";
  private static final String XERCES_P_ROOT =
    "http://apache.org/xml/properties/schema/";
  private static final String EXTERNAL_SCHEMA_LOC_XERCESP = 
    XERCES_P_ROOT + "external-schemaLocation";
  private static final String EXTERNAL_SCHEMA_NNS_LOC_XERCESP = 
    XERCES_P_ROOT + "external-noNamespaceSchemaLocation";

  /**
   * <p>
   * Get the 'correct' implementation of a JAXP <code>SAXParserFactory</code>; this is
   * intended to ensure that local implementations (e.g., Crimson) don't sneak into
   * the mix.
   * </p>
   * @return the Xerces-specific implementaiton
   */
  public static SAXParserFactory getSAXParserFactory() {
    return new SAXParserFactoryImpl();
  }
  
  /**
   * <p>
   * Get the 'correct' implementation of a JAXP <code>DocumentBuilderFactory</code>;
   * this is intended to ensure that local implementations (e.g., Crimson) don't
   * sneak into the mix.
   * </p>
   * @return the Xerces-specific implementation
   */
  public static DocumentBuilderFactory getDocumentBuilderFactory() {
    return new DocumentBuilderFactoryImpl();
  }
  
  /**
   * Set the <code>namespaces</code> SAX property on the supplied
   * <code>XMLReader</code>.
   * @param xr the <code>XMLReader</code> to apply the feature to.
   */
  public static void setNamespaces(XMLReader xr) {
    try {
      xr.setFeature(NAMESPACES_SAXF, true);
    } catch (SAXException snse) {
      throw new SystemConfigurationException(snse);
    }
  }
  
  /**
   * @return a Xerces-specific <code>XMLReader</code> instance.
   */
  public static XMLReader getXMLReader() {
    return new SAXParser();
  }
  
  /**
   * @return a Xerces-specific DOM parser.
   */
  public static DOMParser getDOMParser() {
    return new DOMParser();
  }
  
  /**
   * <p>
   * Specify an external schema location and turn on validation via setting features
   * and properties.
   * </p>
   * @param xr the XMLReader to apply the features and properties to.
   * @param namespace the namespace URI of the schema to validate, with the empty
   * string or <code>null</code> serving to represent the empty namespace.
   * @param u the URL (or relative URL) that contains the schema.
   * @throws SAXNotSupportedException if one of the underlying feature/property
   * settings does.
   * @throws SAXNotRecognizedException if one of the underlying feature/property
   * settings does.
   */
  public static void setExternalSchemaURL(XMLReader xr, String namespace, String u)
      throws SAXNotRecognizedException, SAXNotSupportedException
  {
    xr.setFeature(NAMESPACES_SAXF,true);
    if (namespace != null && namespace.length() > 0) {
      xr.setProperty(EXTERNAL_SCHEMA_LOC_XERCESP, namespace + " " + u);
    } else {
      xr.setProperty(EXTERNAL_SCHEMA_NNS_LOC_XERCESP, u);
    }
    xr.setFeature(VALIDATION_SAXF,true);
    xr.setFeature(SCHEMA_V_XERCESF,true);    
  }
  
  /**
   * <p>
   * Specify an external schema location and turn on validation via setting features
   * and properties.
   * </p>
   * @param dp the <code>DOMParser</code> to apply the features and properties to.
   * @param namespace the namespace URI of the schema to validate, with the empty
   * string or <code>null</code> serving to represent the empty namespace.
   * @param u the URL or relative URL that contains the schema.
   * @throws SAXNotSupportedException if one of the underlying feature/property
   * settings does.
   * @throws SAXNotRecognizedException if one of the underlying feature/property
   * settings does.
   */  
  public static void setExternalSchemaURL(DOMParser dp, String namespace, String u)
      throws SAXNotRecognizedException, SAXNotSupportedException
  {
    dp.setFeature(VALIDATION_SAXF,true);
    dp.setFeature(SCHEMA_V_XERCESF,true);
    if (namespace != null && namespace.length() > 0) {
      dp.setProperty(EXTERNAL_SCHEMA_LOC_XERCESP, namespace + " " + u);
    } else {
      dp.setProperty(EXTERNAL_SCHEMA_NNS_LOC_XERCESP, u);
    }
  }
  
  /**
   * <p>
   * Add a namespace/URL pair to the mapping between namespaces and the schemas used
   * to validate elements in them.  Adding a pair for a namespace that's already
   * bound will result in overwriting the URL previously bound.
   * </p>
   * @param xr
   * @param namespace
   * @param u
   * @throws SAXNotRecognizedException
   * @throws SAXNotSupportedException
   */
  public static void addExternalSchemaURL(XMLReader xr, String namespace, String u)
    throws SAXNotRecognizedException,SAXNotSupportedException
  {
    xr.setFeature(VALIDATION_SAXF,true);
    xr.setFeature(SCHEMA_V_XERCESF,true);
    if (namespace == null || namespace.length() == 0) {
      setExternalSchemaURL(xr,namespace,u);
      return;
    }    
    String s = (String) xr.getProperty(EXTERNAL_SCHEMA_LOC_XERCESP);
    if (s == null) {
      setExternalSchemaURL(xr,namespace,u);
      return;
    }
    StringTokenizer st = new StringTokenizer(s);
    HashMap<String,String> namespaces = new HashMap<String,String>();
    while (st.hasMoreTokens()) {
      String key = st.nextToken();
      if (!st.hasMoreTokens()) {
        throw new RuntimeException("Property has been misconfigured; expected an " +
            "even number of tokens.  Value was: " + s);
      }
      String value = st.nextToken();
      namespaces.put(key,value);
    }
    namespaces.put(namespace,u);
    StringBuffer sb = new StringBuffer();
    Iterator it = namespaces.keySet().iterator();
    while (it.hasNext()) {
      String ns = (String) it.next();
      sb.append(ns);
      sb.append(' ');
      sb.append(namespaces.get(ns));
      if (it.hasNext()) {
        sb.append(' ');
      }
    }
    xr.setProperty(EXTERNAL_SCHEMA_LOC_XERCESP,sb.toString());
    xr.setFeature(VALIDATION_SAXF,true);
    xr.setFeature(SCHEMA_V_XERCESF,true);     
  }
  
  /**
   * <p>
   * Add a namespace/URL pair to the mapping between namespaces and the schemas used
   * to validate elements in them.  Adding a pair for a namespace that's already
   * bound will result in overwriting the URL previously bound.
   * </p>
   * @param dp the <code>DOMParser</code> to apply the features and properties to.
   * @param namespace the namespace URI of the schema to validate, with the empty
   * string or <code>null</code> serving to represent the empty namespace.
   * @param u the URL or relative URL that contains the schema.
   * @throws SAXNotSupportedException if one of the underlying feature/property
   * settings does.
   * @throws SAXNotRecognizedException if one of the underlying feature/property
   * settings does.
   */
  public static void addExternalSchemaURL(DOMParser dp, String namespace, String u)
    throws SAXNotRecognizedException,SAXNotSupportedException
  {
    dp.setFeature(VALIDATION_SAXF,true);
    dp.setFeature(SCHEMA_V_XERCESF,true);
    if (namespace == null || namespace.length() == 0) {
      setExternalSchemaURL(dp,namespace,u);
      return;
    }
    String s = (String) dp.getProperty(EXTERNAL_SCHEMA_LOC_XERCESP);
    if (s == null) {
      setExternalSchemaURL(dp,namespace,u);
      return;
    }
    StringTokenizer st = new StringTokenizer(s);
    HashMap<String,String> namespaces = new HashMap<String,String>();
    while (st.hasMoreTokens()) {
      String key = st.nextToken();
      if (!st.hasMoreTokens()) {
        throw new RuntimeException("Property has been misconfigured; expected an " +
            "even number of tokens.  Value was: " + s);
      }
      String value = st.nextToken();
      namespaces.put(key,value);
    }
    namespaces.put(namespace,u);
    StringBuffer sb = new StringBuffer();
    Iterator it = namespaces.keySet().iterator();
    while (it.hasNext()) {
      String ns = (String) it.next();
      sb.append(ns);
      sb.append(' ');
      sb.append(namespaces.get(ns));
      if (it.hasNext()) {
        sb.append(' ');
      }
    }
    dp.setProperty(EXTERNAL_SCHEMA_LOC_XERCESP,sb.toString());
    dp.setFeature(VALIDATION_SAXF,true);
    dp.setFeature(SCHEMA_V_XERCESF,true);     
  }


  public static ContentHandler getXercesSerializer(OutputStream os) {
    XMLSerializer serializer =  new XMLSerializer();
    OutputFormat format = new OutputFormat();
    format.setPreserveSpace(true);
    format.setOmitDocumentType(true);
    serializer.setOutputFormat(format);
    serializer.setOutputByteStream(os);
    return serializer;

  }
}
