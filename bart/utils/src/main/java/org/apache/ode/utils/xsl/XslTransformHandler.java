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

package org.apache.ode.utils.xsl;

import javax.xml.namespace.QName;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton wrapping the basic <code>javax.xml.transform</code> operations. The
 * transformation is then delegated to a Transformer. Supports both XSL 1.0 and XSL 2.0
 * depending on the version attribute provided in the XSL stylesheet (see
 * http://www.w3.org/TR/xslt20/#backwards - 3.8 Backwards-Compatible Processing).
 * <br/>
 * The transform handler also implements a simple cache to avo??d multiple pre-compilation
 * of the same XSL sheet.
 */
public class XslTransformHandler {

  private static XslTransformHandler __singleton;

  private TransformerFactory _transformerFactory = null;
  private final HashMap<URI,Templates> _templateCache = new HashMap<URI, Templates>();

  /**
   * Singleton access.
   * @return single XslTransformHandler instance.
   */
  public static synchronized XslTransformHandler getInstance() {
    if (__singleton == null) {
      __singleton = new XslTransformHandler();
    }
    return __singleton;
  }

  private XslTransformHandler() { }

  /**
   * Sets the transformer factory for initialization.
   * @param transformerFactory
   */
  public void setTransformerFactory(TransformerFactory transformerFactory) {
    _transformerFactory = transformerFactory;
  }

  /**
   * Always parses the provided stylesheet and stores it in cache from its URI.
   * @param uri referencing the stylesheet
   * @param body of the XSL document
   * @param resolver used to resolve includes and imports
   */
  public void parseXSLSheet(URI uri, String body, URIResolver resolver) {
    Templates tm;
    try {
      _transformerFactory.setURIResolver(resolver);
      tm = _transformerFactory.newTemplates(new StreamSource(new StringReader(body)));
    } catch (TransformerConfigurationException e) {
      throw new XslTransformException(e);
    }
    synchronized(_templateCache) {
      _templateCache.put(uri, tm);
    }
  }

  /**
   * Parses the provided stylesheet and stores it in cache only if it's not there
   * already.
   * @param uri referencing the stylesheet
   * @param body of the XSL document
   * @param resolver used to resolve includes and imports
   */
  public void cacheXSLSheet(URI uri, String body, URIResolver resolver) {
    Templates tm;
    synchronized (_templateCache) {
      tm = _templateCache.get(uri);
    }
    if (tm == null) parseXSLSheet(uri, body, resolver);
  }

  /**
   * Transforms a Source document to a result using the XSL stylesheet referenced
   * by the provided URI. The stylesheet MUST have been parsed previously.
   * @param uri referencing the stylesheet
   * @param source XML document
   * @param result of the transformation (XSL, HTML or text depending of the output method specified in stylesheet
   * @param parameters passed to the stylesheet
   * @param resolver used to resolve includes and imports
   */
  public void transform(URI uri, Source source, Result result,
                        Map<QName, Object> parameters, URIResolver resolver) {
    Templates tm;
    synchronized (_templateCache) {
      tm = _templateCache.get(uri);
    }
    if (tm == null)
      throw new XslTransformException("XSL sheet" + uri + " has not been parsed before transformation!");
    try {
      Transformer tf = tm.newTransformer();
      tf.setURIResolver(resolver);
      if (parameters != null) {
        for (Map.Entry<QName, Object> param : parameters.entrySet()) {
          tf.setParameter(param.getKey().getLocalPart(), param.getValue());
        }
      }
      tf.transform(source, result);
    } catch (TransformerConfigurationException e) {
      throw new XslTransformException(e);
    } catch (TransformerException e) {
      throw new XslTransformException("XSL Transformation failed!", e);
    }
  }

  public void setErrorListener(ErrorListener l) {
    _transformerFactory.setErrorListener(l);
  }

}
