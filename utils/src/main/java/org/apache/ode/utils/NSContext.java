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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.CompositeUnaryFunction;
import org.apache.ode.utils.stl.EqualsUnaryFunction;
import org.apache.ode.utils.stl.FilterIterator;
import org.apache.ode.utils.stl.TransformIterator;

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
    private HashMap<String, String> _prefixToUriMap = new HashMap<String, String>() {
    	@Override
    	public String put(String prefix, String uri) {
            prefix = (String) InternPool.intern("namespace.prefixes", prefix);
            uri = (String) InternPool.intern("namespace.uris", uri);
            return super.put(prefix, uri);
    	}
    };
    
    public NSContext() {
    }

    public NSContext(NSContext map) {
        _prefixToUriMap.putAll(map._prefixToUriMap);
    }

    /**
     * @see NamespaceContext#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(String prefix) {
        return _prefixToUriMap.get(prefix == null ? "" : prefix);
    }

    /**
     * @see NamespaceContext#getPrefix(java.lang.String)
     */
    public String getPrefix(String uri) {
        Iterator i = getPrefixes(uri);

        if (i.hasNext()) {
            return (String) i.next();
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
     *            prefix
     * @param uri
     *            URI
     */
    public void register(String prefix, String uri) {
        if (uri == null)
            uri = "";
        if (prefix == null)
            prefix = "";
        
        if (__log.isTraceEnabled()) {
            __log.trace("register(prefix=" + prefix + ", uri=" + uri + ")");
        }

        _prefixToUriMap.put(prefix, uri);
    }

    /**
     * Register a set of URI mappings at once.
     * 
     * @param prefixMapping
     *            set (map rather) of prefix-to-URI mappings.
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
            Map.Entry me = (Map.Entry) i.next();
            out.writeUTF((String) me.getKey());
            out.writeUTF((String) me.getValue());
        }
    }

    public QName derefQName(String qname) {
        if (qname == null)
            return null;
        int idx = qname.indexOf(':');

        if (idx == -1) {
            return new QName(getNamespaceURI(null), qname);
        }
        String prefix = qname.substring(0, idx);
        String localname = qname.substring(idx + 1);
        String uri = getNamespaceURI(prefix);

        if (uri == null) {
            return null;
        }

        return new QName(uri, localname);

    }

    public Map<String, String> toMap() {
        return Collections.unmodifiableMap(_prefixToUriMap);
    }
}
