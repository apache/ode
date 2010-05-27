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

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/**
 * <p>
 * This is a utility for a SAX <code>ContentHandler</code> implementation to use in
 * tracking namespace declarations during a parse.  The assumption is that the
 * handler has access to the full stream of events relevant to managing the
 * declarations.  The primary use case is the resolution of <code>String</code>s,
 * e.g., from attribute values or element content, to <code>QName</code> objects.
 * </p>
 */
public class NamespaceStack {

  private Frame _current;

  /**
   * <p>
   * Construct a new instance with the bare minimum bindings for the
   * <code>xmlns</code>, <code>xml</code>, and empty prefixes.  Note that the empty
   * prefix is bound to the empty (non-<code>null</code>) URI.
   * </p>
   */
  public NamespaceStack() {
    _current = new Frame();
    /*
     * As per the Namespaces in XML Errata:
     * http://www.w3.org/XML/xml-names-19990114-errata
     */
    _current.declarePrefix("xmlns","http://www.w3.org/2000/xmlns/");
    /*
     * As per the Namespaces in XML Rec:
     * http://www.w3.org/TR/1999/REC-xml-names-19990114
     */
    _current.declarePrefix("xml","http://www.w3.org/XML/1998/namespace");
    /*
     * As per the Namespaces in XML Rec:
     * http://www.w3.org/TR/1999/REC-xml-names-19990114
     */
    _current.declarePrefix("","");
  }

  /**
   * <p>
   * Convert the current stack of contexts into a single <code>NSContext</code>.
   * </p>
   * @return the <code>NSContext</code> instance
   */
  public NSContext toNSContext() {
    NSContext n = new NSContext();
    for (Frame f = _current;f != null; f=f._parent) {
      if (f._bindings == null) continue;
      for (Iterator it = f._bindings.keySet().iterator();it.hasNext();) {
        String pfx = (String) it.next();
        if (n.getNamespaceURI(pfx) == null) {
          n.register(pfx, f._bindings.get(pfx));
        }
      }
    }
    return n;
  }

  /**
   * <p>
   * Push a fresh context onto the stack.  This method should be called somewhere in
   * the body of a <code>startElement()</code>, as it represents the namespace
   * resolution context for the events that occur between that event and the
   * corresponding <code>endElement()</code>.
   * </p>
   * @see org.xml.sax.ContentHandler
   */
  public void pushNewContext() {
    _current = new Frame(_current);
  }

  /**
   * <p>
   * Pop a context from the stack.  This method should be called somewhere in the
   * body of an <code>endElement</code>, as it clears the context that was used for
   * namespace resolution within the body of the corresponding element.
   * </p>
   * @see org.xml.sax.ContentHandler
   */
  public void pop() {
    if (_current._parent == null) {
      throw new EmptyStackException();
    }
    _current = _current._parent;
  }

  /**
   * <p>
   * Declare a new prefix binding.  This binding will supercede a binding with the
   * same prefix in the same scope.  As a crutch, <code>null</code> arguments may be
   * passed and will be interpreted as <code>&quot;&quot;</code>.  Note that binding
   * a non-empty prefix to an empty URI is not permitted in XML 1.0 but is not
   * flagged as an error by the method.
   * </p>
   * @param prefix the prefix to bind
   * @param uri the URI to bind it to
   */
  public void declarePrefix(String prefix, String uri) {
    _current.declarePrefix(prefix==null?"":prefix, uri==null?"":uri);
  }

  /**
   * <p>
   * Retrieve the URI bound to the supplied prefix or <code>null</code> if no URI
   * is bound to the supplied prefix.  As a crutch, a <code>null</code> argument
   * may be passed and will be interpreted as the empty prefix
   * (<code>&quot;&quot;</code>).
   * </p>
   * @returns the URI or <code>null</code> if no URI is bound.
   */
  public String getNamespaceUri(String prefix) {
    return _current.getNamespaceURI(prefix==null?"":prefix);
  }

  /**
   * <p>
   * Fire the events for the current frame's prefixes into a <code>ContentHandler</code>.
   * </p>
   * @param ch the target <code>ContentHandler</code>
   * @throws SAXException if the target method does.
   */
  public void startPrefixMappings(ContentHandler ch) throws SAXException {
    _current.startPrefixMappings(ch);
  }

  /**
   * <p>
   * Fire the events for the current frame's prefixes into a <code>ContentHandler</code>.
   * </p>
   * @param ch the target <code>ContentHandler</code>.
   * @throws SAXException if the target method does.
   */
  public void endPrefixMappings(ContentHandler ch) throws SAXException {
    _current.endPrefixMappings(ch);
  }

  /**
   * <p>
   * Allocate and declare a new namespace prefix for the current context that uses
   * the supplied &quot;hint&quot; as a start.  The algorithm used will defer to an
   * existing binding, then try the hint, then use a variant of the hint until it
   * finds an available prefix.
   * </p>
   * @param hint a hint as to the desired prefix or <code>null</code> if any prefix
   * will do.
   * @param uri the URI to bind to the prefix.
   * @return the suggested prefix.
   */
  public String allocatePrefix(String hint, String uri) {
    String pfx = getPrefix(uri);
    if (pfx == null) {
      hint = hint==null?"ns":hint;
      String u = getNamespaceUri(hint);
      if (u == null) {
        declarePrefix(hint,uri);
        pfx = hint;
      } else if (u.equals(uri)) {
        pfx = hint;
      } else {
        // ??
      }
    }
    return pfx;
  }

  public String getPrefix(String uri) {
    return toNSContext().getPrefix(uri);
  }

  /**
   * <p>
   * Derference the prefix on a QName in <code>String</code> form and return a Java
   * <code>QName</code> object.
   * </p>
   * @param qname the QName in string form.
   * @return the dereferenced <code>QName</code>.
   * @throws IllegalArgumentException if a <code>null</code> argument is passed,
   * a malformed argument (e.g., <code>:foo</code> or <code>foo:</code>) is passed,
   * or if the prefix cannot be resolved to a URI.
   */
  public QName dereferenceQName(String qname) {
    if (qname == null) {
      throw new IllegalArgumentException("Unable to dereference <null> as a QName.");
    }
    int pos = qname.indexOf(':');
    QName qn;
    if (pos == qname.length() - 1 || pos == 0) {
      throw new IllegalArgumentException("\"" + qname + "\" is a malformed QName.");
    } else if (pos == -1) {
      qn = new QName (getNamespaceUri(""),qname);
    } else {
      String uri = getNamespaceUri(qname.substring(0,pos));
      if (uri == null) {
        throw new IllegalArgumentException("No URI is bound to " +
            qname.substring(0,pos) + ".");
      }
      qn = new QName(uri,qname.substring(pos+1));
    }
    return qn;
  }

  private class Frame {

    Frame _parent;

    HashMap<String,String> _bindings;

    Frame() {
      // This space intentionally left blank.
    }

    Frame(Frame parent) {
      _parent = parent;
    }

    void startPrefixMappings(ContentHandler ch) throws SAXException {
      if (_bindings != null) {
        for(Iterator it = _bindings.keySet().iterator();it.hasNext();) {
          String prefix = (String) it.next();
          ch.startPrefixMapping(prefix, _bindings.get(prefix));
        }
      }
    }

    void endPrefixMappings(ContentHandler ch) throws SAXException {
      if (_bindings != null) {
        for (Iterator it = _bindings.keySet().iterator();it.hasNext();) {
          ch.endPrefixMapping((String) it.next());
        }
      }
    }

    void declarePrefix(String prefix, String uri) {
      if (_bindings == null) {
        _bindings = new HashMap<String,String>();
      }
      _bindings.put(prefix,uri);
    }

    String getPrefix(String uri) {
      return "";
    }


    String getNamespaceURI(String prefix) {
      String uri = null;
      if (_bindings != null) {
        uri = _bindings.get(prefix);
      }
      if (uri == null) {
        uri= _parent==null ? null:(_parent.getNamespaceURI(prefix));
      }
      return uri;
    }

  }
}
