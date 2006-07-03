/*
 * File: $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 * 
 */

package com.fs.utils;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fs.utils.stl.*;

/**
 * A simple in-memory implementation of the {@link NamespaceContext} interface
 * with fairly generic applicability. This class allows clients to manipulate
 * the context through publicly accessible methods, and provides serialization
 * support.
 * 
 * @see NamespaceContext
 */
public class NSContext implements NamespaceContext, Externalizable {
  private static final long serialVersionUID = 1L;

  /** Class-level logger. */
  private static final Log __log = LogFactory.getLog(NSContext.class);

  /** Prefix-to-URI map. */
  private HashMap<String, String> _prefixToUriMap = new HashMap<String, String>();

  public NSContext() {
  }

  public NSContext(NSContext map) {
    _prefixToUriMap.putAll(map._prefixToUriMap);
  }

  /**
   * @see NamespaceContext#getNamespaceURI(java.lang.String)
   */
  public String getNamespaceURI(String prefix) {
    return _prefixToUriMap.get(prefix);
  }

  /**
   * @see NamespaceContext#getPrefix(java.lang.String)
   */
  public String getPrefix(String uri) {
    Iterator i = getPrefixes(uri);

    if (i.hasNext()) {
      return (String)i.next();
    }

    return null;
  }

  /**
   * @see NamespaceContext#getPrefixes
   */
  @SuppressWarnings("unchecked")
  public Iterator getPrefixes(final String uri) {
    return new TransformIterator(new FilterIterator(_prefixToUriMap.entrySet().iterator(),
        new CompositeUnaryFunction(new EqualsUnaryFunction(uri), CollectionsX.ufnMapEntry_getValue)),
        CollectionsX.ufnMapEntry_getKey);
  }

  /**
   * Get all the prefixes with a URI mapping in this context
   * 
   * @return{@link Set} of prefix {@link String}s with a URI mapping in this
   *         context
   */
  public Set<String> getPrefixes() {
    return Collections.unmodifiableSet(_prefixToUriMap.keySet());
  }

  /**
   * Get all the URIs with a prefix mapping in this context
   * 
   * @return{@link Set} of URI {@link String}s with a prefix mapping in this
   *         context
   */
  public Set<String> getUriSet() {
    return new HashSet<String>(_prefixToUriMap.values());
  }

  /**
   * @see Externalizable#readExternal(java.io.ObjectInput)
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    int numKeys = in.readInt();

    for (int i = 0; i < numKeys; ++i) {
      String prefix = in.readUTF();
      String uri = in.readUTF();
      _prefixToUriMap.put(prefix, uri);
    }

    if (__log.isTraceEnabled()) {
      __log.trace("readExternal: contents=" + _prefixToUriMap);
    }
  }

  /**
   * Add a prefix to URI mapping to this context.
   * 
   * @param prefix
   *          prefix
   * @param uri
   *          URI
   */
  public void register(String prefix, String uri) {
    if (__log.isTraceEnabled()) {
      __log.trace("register(prefix=" + prefix + ", uri=" + uri + ")");
    }

    _prefixToUriMap.put(prefix, uri);
  }

  /**
   * Register a set of URI mappings at once.
   * 
   * @param prefixMapping
   *          set (map rather) of prefix-to-URI mappings.
   */
  public void register(Map<String, String> prefixMapping) {
    if (__log.isTraceEnabled()) {
      __log.trace("register(prefixmappings=" + prefixMapping + ")");
    }

    _prefixToUriMap.putAll(prefixMapping);
  }

  /**
   * @see Externalizable#writeExternal(java.io.ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    if (__log.isTraceEnabled()) {
      __log.trace("writeExternal: contents=" + _prefixToUriMap);
    }

    out.writeInt(_prefixToUriMap.size());

    for (Iterator i = _prefixToUriMap.entrySet().iterator(); i.hasNext();) {
      Map.Entry me = (Map.Entry)i.next();
      out.writeUTF((String)me.getKey());
      out.writeUTF((String)me.getValue());
    }
  }

  public QName derefQName(String qname) {
    int idx = qname.indexOf(':');

    if (idx == -1) {
      return new QName(getNamespaceURI(null), qname);
    }
    else {
      String prefix = qname.substring(0, idx);
      String localname = qname.substring(idx + 1);
      String uri = getNamespaceURI(prefix);

      if (uri == null) {
        return null;
      }

      return new QName(uri, localname);
    }
  }
}
