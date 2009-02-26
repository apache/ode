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
package org.apache.ode.utils.xml.capture;

import org.apache.ode.utils.XMLParserUtils;

import java.net.URI;
import java.util.*;

import org.xml.sax.EntityResolver;
import org.xml.sax.XMLReader;

/**
 * XML Dependency Scanner, processes XML and follows "import"s, "include"s,
 * and the like to discover the transitive closure of the dependencies.
 * The scanner supports WSDL, XMLSchema, and BPEL import elements, and can
 * be extended by implementing additional {@link Tracker} classes.
 */
public class XmlDependencyScanner {

  /** Errors. */
  private Map<URI,Exception> _errors = new HashMap<URI,Exception>();

  /** References. */
  private Map<URI, Set<URI>> _references = new HashMap<URI, Set<URI>>();

  /** Referers. */
  private Map<URI, Set<URI>> _referers = new HashMap<URI, Set<URI>>();

  private EntityResolver _resolver;

  private MultiplexTracker _mch;

  /** Constructor. */
  public XmlDependencyScanner() {
    Set<Tracker> handlers = new HashSet<Tracker>();
    handlers.add(new Wsdl11Tracker());
    handlers.add(new XmlSchemaTracker());
    _mch = new MultiplexTracker(handlers);
  }

  /**
   * Set the {@link EntityResolver} that should be used to obtain the
   * byte streams for URIs.
   * @param resolver {@link EntityResolver} or null for the default (URL) resolver
   */
  public void setResolver(EntityResolver resolver) {
    _resolver = resolver;
  }

  public EntityResolver getResolver() {
    return _resolver;
  }

  /**
   * Process the URI: parse the document and follow any imports (recursively)
   * to discover all imported resources.
   * @param uri
   */
  public void process(URI uri) {

    if (_references.keySet().contains(uri))
      return;

    HashSet<URI> todo = new HashSet<URI>();
    todo.add(uri);

    while (!todo.isEmpty()) {
      Iterator<URI> i = todo.iterator();
      URI x = i.next();
      i.remove();
      HashSet<URI> refs = new HashSet<URI>();
      _mch.init(x, refs);
      try {
        XMLReader reader = XMLParserUtils.getXMLReader();
        reader.setContentHandler(_mch);
        reader.setFeature("http://xml.org/sax/features/namespaces", true);
        reader.setEntityResolver(_resolver);
        reader.parse(x.toASCIIString());
        _references.put(x, new HashSet<URI>(refs));
      } catch (Exception e) {
        _errors.put(x, e);
      }

      for (Iterator<URI> i1 = refs.iterator();i1.hasNext();)
        addReferer(i1.next(), x);
      refs.removeAll(_references.keySet());
      todo.addAll(refs);
    }
  }

  public Set<URI> getURIs() {
    return _references.keySet();
  }

  /**
   * Get the references (imports, includes, etc) of a given
   * resource.
   * @param uri URI of the resource
   * @return {@link Set} of references
   */
  public Set<URI> getReferences(URI uri) {
    Set<URI> r = _references.get(uri);
    if (r == null)
      r = Collections.emptySet();
    return r;
  }

  public boolean isError() {
    return !_errors.isEmpty();
  }
  /**
   * Get all the URI's that refer to a URI.
   * @param uri URI refered to
   * @return {@link Set} of resources that refer to the resource
   */
  public Set<URI> getReferers(URI uri) {
    Set<URI> r = _referers.get(uri);
    if (r == null)
      r = Collections.emptySet();
    return r;
  }

  void addReferer(URI reference, URI referer) {
    Set<URI> x = _referers.get(reference);
    if (x == null) {
      x = new HashSet<URI>();
      _referers.put(reference,x);
    }
    x.add(referer);
  }

  public Map<URI, Exception> getErrors() {
    return _errors;
  }

}
