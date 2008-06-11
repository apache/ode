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

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Collections;
import java.util.Iterator;

/**
 * <h3>3-level Property File</h3>
 * This class loads a regular property file. The main feature is that property can
 * be chained in three levels. Which are, from highest to deepest:
 * <ol>
 * <li>{@code property}: defines the default value for the given property</li>
 * <li>{@code service-ns.service-localname.ode.property}: defines the value for all ports of the given service</li>
 * <li>{@code service-ns.service-localname.port.ode.property}: defines the value for the given port</li>
 * </ol>
 * Then, properties might be queried with a service and/or port. The corresponding level will be queried for the associated value,
 * if not found the level n-1 is queried, and so on until the default value.
 * <p/>
 * Properties must respect the following pattern: {@code [service-ns.service-localname.[port.]prefix.]property}
 * <p/>
 * Values may contain some environment variables. For instance, {@code message=You're using ${java.version}}.
 * <p/>
 * <h3>Namespaces Alias</h3>
 * To save some typing and make sure the property is valid, namespaces might be aliased.<br/>
 * To do so, add a property similar to: {@code alias.my-ns-nickname=http://mynamespace.org}.
 * <br/>Then instead of typing {@code http://mynamespace.org.mylocalname.myproperty=my_value} (which is not a valid a property btw}, write: {@code my-ns-nickname.mylocalname.myproperty=my_value} 
 *
 * <p/>
  * <h3>Examples</h3>
 * For instance, if the property file looks like this:
 * <pre>
 * alias.ex_ns=http://examples.org
 * 
 * max-redirects=30
 * timeout=40000
 *
 * ex_ns.film-service.port-of-cannes.ode.timeout=50000
 *
 * ex_ns.brel-service.ode.max-redirects=40
 * ex_ns.brel-service.port-of-amsterdam.ode.max-redirects=60
 * </pre>
 * The following values may be expected:
 * <pre>
 * getProperty("max-redirects")                                                              => 30
 * getProperty("http://examples.org", "brel-service", "max-redirects")                       => 40
 * getProperty("http://examples.org", "brel-service", "port-of-amsterdam", "max-redirects")  => 60
 * 
 * getProperty("http://examples.org", "film-service", "timeout")                       => 40000
 * getProperty("http://examples.org", "film-service", "port-of-cannes", "timeout")     => 50000
 * getProperty("http://examples.org", "brel-service", "port-of-amsterdam", "timeout")  => 40000
 *
 * getProperties("http://examples.org", "film-service")                     => Map{"timeout"=>"40000", "max-redirect"=>"30"}
 * getProperties("http://examples.org", "film-service", "port-of-cannes")   => Map{"timeout"=>"50000", "max-redirect"=>"30"}
 * </pre>
 * <p/>
 * This class is not thread-safe.
 *
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class HierarchicalProperties {

    private static final Log log = LogFactory.getLog(HierarchicalProperties.class);

    public static final String ODE_PREFFIX = "ode";

    // the raw properties as of loaded from the filesystem
    private Properties props = new Properties();
    // map <URI, alias>
    private Map<String, String> aliases = new HashMap<String, String>();
    private File file;
    private String prefix;
    private String dotted_prefix;
    /*
        This map contains ChainedMap instances chained according to the service and/or port they are associated with.
        All ChainedMap instances has a common parent.
        The ChainedMap instances are chained to each others so that if a property is not found for [service, port],
        the ChainedMap associated to [service] will be queried, and if still not found, then the common parent.

        The ChainedMap instance common to all services and ports is associated to the [null, null] key.
        ChainedMap instance common to all ports of a given service is associated to [service, null].
        ChainedMap instance of a given service, port couple is associated to [service, port].

        The ChainedMap instances contain string values as loaded from the filesystem.
     */
    private MultiKeyMap hierarchicalMap = new MultiKeyMap();

    // map used to cache immutable versions of the maps
    private transient MultiKeyMap cacheOfImmutableMaps = new MultiKeyMap();

    /**
     * @param file   the property file to be loaded. If the file does not exist, NO exception is thrown. Property map will be empty
     * @param prefix the property prefix
     * @throws IOException if the file exists but is a directory
     */
    public HierarchicalProperties(File file, String prefix) throws IOException {
        this.file = file;
        this.prefix = prefix;
        this.dotted_prefix = "." + prefix + ".";
        loadFile();
    }

    /**
     * Use {@link #ODE_PREFFIX} as the prefix
     *@see #HierarchicalProperties(java.io.File, String)
     */
    public HierarchicalProperties(File file) throws IOException {
        this(file, ODE_PREFFIX);
    }

    /**
     * Clear all existing content, re-read the file and parse each property. If the file does not exist, content is cleared and method returns (no exception will be thrown).
     * <br/>Keep in mind that this class is not thread-safe. It's the caller's responsability to make sure that one thread is not querying some properties
     * while another is re-loading the file, for instance.
     * @throws IOException if the file is a Directory
     */
    public void loadFile() throws IOException {
        // #1. clear all existing content
        clear();

        if (!file.exists()) {
            if (log.isDebugEnabled()) log.debug("File does not exist [" + file + "] Properties will be empty.");
            return;
        }

        // #2. read the file
        FileInputStream fis = new FileInputStream(file);
        try {
            if (log.isDebugEnabled()) log.debug("Loading property file: " + file);
            props.load(fis);
        } finally {
            fis.close();
        }

        // #3. put the root map
        hierarchicalMap.put(null, null, new ChainedMap());

        // #4. process each property

        for (Iterator it = props.entrySet().iterator(); it.hasNext();) {
            Map.Entry e = (Map.Entry) it.next();
            String key = (String) e.getKey();
            String value = (String) e.getValue();

            // replace any env variables by its value
            value = SystemUtils.replaceSystemProperties(value);
            props.put(key, value);

            if (key.startsWith("alias.")) {
                final String alias = key.substring("alias.".length(), key.length());
                if(log.isDebugEnabled()) log.debug("Alias found: "+alias+" -> "+value);
                aliases.put(value, alias);
            } else {
                // parse the property name
                String[] info = parseProperty((String) key);
                String nsalias = info[0];
                String service = info[1];
                String port = info[2];
                String targetedProperty = info[3];

                QName qname = nsalias != null ? new QName(nsalias, service) : null;
                // get the map associated to this port
                ChainedMap p = (ChainedMap) hierarchicalMap.get(qname, port);
                if (p == null) {
                    // create it if necessary
                    // get the associated service map
                    ChainedMap s = (ChainedMap) hierarchicalMap.get(qname, null);
                    if (s == null) {
                        // create the service map if necessary, the parent is the root map.
                        s = new ChainedMap(getRootMap());
                        // put it in the multi-map
                        hierarchicalMap.put(qname, null, s);
                    }

                    // create the map itself and link it to theservice map
                    p = new ChainedMap(s);
                    // put it in the multi-map
                    hierarchicalMap.put(qname, port, p);
                }

                // save the key/value in its chained map
                p.put(targetedProperty, value);
            }
        }
    }

    /**
     * Clear all content.
     */
    public void clear() {
        props.clear();
        aliases.clear();
        hierarchicalMap.clear();
        cacheOfImmutableMaps.clear();
    }

    protected ChainedMap getRootMap() {
        Object o = hierarchicalMap.get(null, null);
        if (o == null) {
            o = new ChainedMap();
            hierarchicalMap.put(null, null, o);
        }
        return (ChainedMap) o;
    }

    /**
     * @see #getProperties(javax.xml.namespace.QName)
     */
    public Map getProperties(String serviceNamespaceURI, String serviceLocalPart) {
        return getProperties(new QName(serviceNamespaceURI, serviceLocalPart));
    }

    /**
     * @param service
     * @return a map containing all the properties for the given service.
     * @see #getProperties(javax.xml.namespace.QName, String)
     */
    public Map getProperties(QName service) {
        return getProperties(service, null);
    }

    /**
     * @see #getProperties(javax.xml.namespace.QName, String)
     */
    public Map getProperties(String serviceNamespaceURI, String serviceLocalPart, String port) {
        return getProperties(new QName(serviceNamespaceURI, serviceLocalPart), port);
    }

    /**
     * Return a map containing all the properties for the given service/port. The map is an immutable snapshot of the properties.
     * <br/>These immutable maps are cached to avoid too many map instances.
     * <br/>If {@code port} is null then properties defined at the service level are returned.
     * @param service
     * @param port
     * @return an immutable map containing all the properties for the given port
     */
    public Map getProperties(QName service, String port) {
        // no need to go further if no properties
        if (hierarchicalMap.isEmpty()) return Collections.EMPTY_MAP;

        service = resolveNamespace(service);
        // check if the cache of immutable maps contains this key
        Map cachedMap = (Map) this.cacheOfImmutableMaps.get(service, port);
        if (cachedMap != null) {
            return cachedMap;
        }

        // if not, get the corresponding ChainedMap and convert it into a Map
        ChainedMap cm = (ChainedMap) hierarchicalMap.get(service, port);
        if (cm == null) {
            cm = (ChainedMap) hierarchicalMap.get(service, null);
            if (cm == null) {
                // return the cached version of the root map
                return getProperties((QName) null, null);
            }
        }

        // convert the ChainedMap into a Map and cache it
        Map snapshotMap = new HashMap(cm.size() * 15 / 10);
        for (Object key : cm.keySet()) {
            snapshotMap.put(key, cm.get(key));
        }
        snapshotMap = Collections.unmodifiableMap(snapshotMap);
        // put it in cache to avoid creating one map on each invocation
        this.cacheOfImmutableMaps.put(service, port, snapshotMap);
        return snapshotMap;
    }

    /**
     *
     * @param property the property to be queried
     * @return the default value for this property
     */
    public String getProperty(String property) {
        return (String) getRootMap().get(property);
    }

    /**
     * @see #getProperty(javax.xml.namespace.QName, String) 
     */
    public String getProperty(String serviceNamespaceURI, String serviceLocalPart, String property) {
        return getProperty(new QName(serviceNamespaceURI, serviceLocalPart), property);
    }

    /**
     * @return the value associated to this property for the given service
     */
    public String getProperty(QName service, String property) {
        return getProperty(service, null, property);
    }

    /**
     * @see #getProperty(javax.xml.namespace.QName, String, String) 
     */
    public String getProperty(String serviceNamespaceURI, String serviceLocalPart, String port, String property) {
        return getProperty(new QName(serviceNamespaceURI, serviceLocalPart), port, property);
    }

    /**
     * Equivalent {@code getProperties(service,port).get(property)}
     * @return the value associated to this property for the given service/port
     */
    public String getProperty(QName service, String port, String property) {
        return (String) getProperties(service,port).get(property);
    }

    /**
     * Resolved the service qname associated to the given aliased service name.
     * <p/>For instance, {@code resolveAlias(new QName("my-ns", "a-name"))} will return
     * {@code new QName("http://examples.com", "a-name")} if the alias {@code my-ns => http://examples.com} exists.
     * @param aliasedServiceName a service name using an alias
     * @return the qname of the service associated to this alias if defined in the alias map, or aliasedServiceName itself.
     */
    private QName resolveNamespace(QName aliasedServiceName) {
        if (aliasedServiceName != null && aliases.containsKey(aliasedServiceName.getNamespaceURI())) {
            return new QName(aliases.get(aliasedServiceName.getNamespaceURI()), aliasedServiceName.getLocalPart());
        }
        return aliasedServiceName;
    }

    /**
     * @return an array of strings containing: namespace alias, service, port, targeted property
     */
    private String[] parseProperty(String property) {
        // namespace alias, service, port, targeted property
        String[] res = new String[4];

        int index = property.indexOf(dotted_prefix);
        if (index <= 0) {
            // assume there is no service/port prefixed, no need to go further
            res[3] = property;
        } else {
            res[3] = property.substring(index + dotted_prefix.length()); // targeted property
            String prefix = property.substring(0, index);
            String[] t = prefix.split("\\.");
            if (t.length != 2 && t.length != 3) {
                throw new IllegalArgumentException("Invalid property name:" + property + " Expected pattern: [nsalias.service.[port.]" + prefix + ".]property");
            }
            if (t.length >= 2) {
                res[0] = t[0]; // ns alias
                res[1] = t[1]; // service name
            }
            if (t.length > 2) {
                res[2] = t[2]; // port name
            }
        }
        return res;
    }

    /**
     * Link two Maps instances in a parent-child relation. Meaning that if a key is looked up but not found on the child,
     * then the key will be looked up on the parent map.
     * <br/>The raison d'etre of this class is to the {@link #keySet()} method. This methods returns a set of <strong>all</strong> the keys contained in the child and the parent.
     * That's the main reason to not used the {@link java.util.Properties} class (which offers access to child keys only).
     * <p/>The child has an immutable view of the parent map. Methods {@link #clear()} and {@link #remove(Object)}
     * throw {@link UnsupportedOperationException}. Methods {@link #put(Object, Object)} and  {@link #putAll(java.util.Map)} impacts only the child map.
     * <br/>Methods  {@link #clearLocally(Object)}
     * <p/>
     * This class does NOT implement the {@link java.util.Map} interface because methods {@link java.util.Map#entrySet()} },
     * {@link java.util.Map#values()} and {@link java.util.Map#keySet()} would NOT be backed by the Map itself.
     * <br/> Contributions welcome to implement that part.
     *
     * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
     */
    private static class ChainedMap {

        private ChainedMap parent;
        private Map child;

        public ChainedMap() {
            parent = null;
            child = new HashMap();
        }

        public ChainedMap(ChainedMap parent) {
            this.parent = parent;
            this.child = new HashMap();
        }

        public ChainedMap getParent() {
            return parent;
        }

        public void setParent(ChainedMap parent) {
            this.parent = parent;
        }

        /**
         * Perfom a look up on the child map only.
         */
        public Object getLocally(Object key) {
            return child.get(key);
        }

        /**
         * Clear the child map only, the parent map is not altered.
         */
        public void clearLocally() {
            child.clear();
        }

        /**
         * Perform a look up for the given key on the child map, and if not found then perform the look up on the parent map.
         *
         * @param key
         * @return
         */
        public Object get(Object key) {
            Object lv = getLocally(key);
            if (lv != null) return lv;
            else if (parent != null) return parent.get(key);
            return null;
        }

        /**
         * Put this pair in the child map.
         */
        public Object put(Object key, Object value) {
            if (key == null) throw new NullPointerException("Null keys forbidden!");
            return child.put(key, value);
        }


        /**
         * Put these pairs in the child map.
         */
        public void putAll(Map t) {
            for (Object e : t.entrySet()) {
                put(((Map.Entry) e).getKey(), ((Map.Entry) e).getValue());
            }
        }

        /**
         * @throws UnsupportedOperationException
         * @see #clearLocally()
         */
        public void clear() {
            throw new UnsupportedOperationException();
        }

        /**
         * @throws UnsupportedOperationException
         */
        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }

        /**
         * @return true if the child map is empty AND the parent map is null or empty as well.
         *         <pre>child.isEmpty() && (parent == null || parent.isEmpty());</pre>
         */
        public boolean isEmpty() {
            return child.isEmpty() && (parent == null || parent.isEmpty());
        }

        /**
         * @return true if the child map contains this key OR the parent map is not null and contains this key.
         *         <pre>child.containsKey(key) || (parent != null && parent.containsKey(key));</pre>
         */
        public boolean containsKey(Object key) {
            if (key == null) throw new NullPointerException("Null keys forbidden!");
            return child.containsKey(key) || (parent != null && parent.containsKey(key));
        }

        /**
         * @return true if the child map contains this value OR the parent is not null
         *         <pre>child.containsValue(value) || (parent != null && parent.containsValue(value));</pre>
         */
        public boolean containsValue(Object value) {
            return child.containsValue(value) || (parent != null && parent.containsValue(value));
        }

        public int size() {
            return keySet().size();
        }

        /**
         * @return a new set instance merging all keys contained in the child and parent maps. <strong>The returned set is not backed by the maps.</strong>
         *         Any references to the returned sets are hold at the holder's own risks. This breaks the general {@link java.util.Map#entrySet()} contract.
         */
        public Set keySet() {
            HashSet s = new HashSet(child.keySet());
            if (parent != null) s.addAll(parent.keySet());
            return s;
        }
    }
}
