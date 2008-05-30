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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Collections;

/**
 * This class load a regular property file in {@link java.util.Properties} instance. The main feature is that property can
 * be hierarchied/chained in three levels. Then when querying for a property, if it's not found in the deepest level,
 * the parent will be queryed and so on.
 * <p/>
 * A prefix must be defined to discriminate the property name and the level-1, level-2 names. The default prefix is {@link #ODE_PREFFIX}.
 * <p/>
 * Properties must respect the following pattern: [level1[.level2].prefix.]property
 * <p/>
 * A concrete use case could be the definition of properties for wsdl services and ports.
 * <br/>Level 0 would be: values common to all services and ports.
 * <br/>Level 1: values common to a given service.
 * <br/>Level 2: values common to a given port.
 * <p/>
 * For instance, if the property file looks like this:
 * <pre>
 * timeout=40000
 * film-service.port-of-cannes.ode.timeout=50000
 * <p/>
 * max-redirects=30
 * brel-service.ode.max-redirects=40
 * brel-service.port-of-amsterdam.ode.max-redirects=60
 * </pre>
 * The following values may be expected:
 * <pre>
 * getProperty("max-redirects")                                       => 30
 * getProperty("brel-service", "max-redirects")                       => 40
 * getProperty("brel-service", "port-of-amsterdam", "max-redirects")  => 60
 * <p/>
 * getProperty("film-service", "timeout")                       => 40000
 * getProperty("film-service", "port-of-cannes", "timeout")     => 50000
 * getProperty("brel-service", "port-of-amsterdam", "timeout")  => 40000
 * </pre>
 * <p/>
 * Values may contain some environment variables. For instance, message=You're using ${java.version}.
 * <p/>
 * This class is not thread-safe.
 *
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class HierarchiedProperties {

    private static final Log log = LogFactory.getLog(HierarchiedProperties.class);

    // Prefix used by all properties
    public static final String ODE_PREFFIX = "ode.";

    // the raw properties as of loaded from the filesystem
    private Properties props = new Properties();
    private File file;
    private String prefix;
    /*
        This map contains ChainedMap instances hierarchied according to the service and/or port they are associated with.
        All ChainedMap instances has a common parent.
        The ChainedMap instances are chained to each others so that if a property is not found for [service, port],
        the ChainedMap associated to [service] will be queried, and if still not found, then the common parent.

        The ChainedMap instance common to all services and ports is associated to the [null, null] key.
        ChainedMap instance common to all ports of a given service is associated to [service, null].
        ChainedMap instance of a given service, port couple is associated to [service, port].

        The ChainedMap instances contain string values as loaded from the filesystem.
     */
    private MultiKeyMap hierarchiedMap = new MultiKeyMap();

    // map used to cache immutable versions of the maps
    private transient MultiKeyMap cacheOfImmutableMaps = new MultiKeyMap();

    /**
     * @param file   the property file to be loaded. The file may not exist.
     *               But if the file exists it has to be a file (not a directory), otherwhise an IOException is thrown.
     * @param prefix the property prefix
     * @throws IOException
     */
    public HierarchiedProperties(File file, String prefix) throws IOException {
        this.file = file;
        this.prefix = prefix;
        loadFile();
    }

    public HierarchiedProperties(File file) throws IOException {
        this(file, ODE_PREFFIX);
    }

    /**
     * Clear all existing content, read the file and parse each property. Simply logs a message and returns if the file does not exist.
     *
     * @throws IOException if the file is a Directory
     */
    public void loadFile() throws IOException {
        if (!file.exists()) {
            if (log.isDebugEnabled()) log.debug("File does not exist [" + file + "] Properties will be empty.");
            return;
        }
        // #1. clear all existing content
        clear();

        // #2. read the file
        FileInputStream fis = new FileInputStream(file);
        try {
            if (log.isDebugEnabled()) log.debug("Loading property file: " + file);
            props.load(fis);
        } finally {
            fis.close();
        }

        // #3. put the root map
        hierarchiedMap.put(null, null, new ChainedMap());

        // #4. process each property
        for (Object key : props.keySet()) {
            String value = (String) props.get(key);

            // replace any env variables by its value
            value = SystemUtils.replaceSystemProperties(value);
            props.put(key, value);

            // parse the property name
            String[] info = parseProperty((String) key);
            String service = info[0];
            String port = info[1];
            String targetedProperty = info[2];

            // get the map associated to this port
            ChainedMap p = (ChainedMap) hierarchiedMap.get(service, port);
            if (p == null) {
                // create it if necessary
                // get the associated service map
                ChainedMap s = (ChainedMap) hierarchiedMap.get(service, null);
                if (s == null) {
                    // create the service map if necessary, the parent is the root map.
                    s = new ChainedMap(getRootMap());
                    // put it in the multi-map
                    hierarchiedMap.put(service, null, s);
                }

                // create the map itself and link it to theservice map
                p = new ChainedMap(s);
                // put it in the multi-map
                hierarchiedMap.put(service, port, p);
            }

            // save the key/value in its chained map
            p.put(targetedProperty, value);
        }
    }

    /**
     * Clear all content. If {@link #loadFile()} is not invoked later, all returned values will be null.
     */
    public void clear() {
        props.clear();
        hierarchiedMap.clear();
        cacheOfImmutableMaps.clear();
    }

    protected ChainedMap getRootMap() {
        Object o = hierarchiedMap.get(null, null);
        if (o == null) {
            o = new ChainedMap();
            hierarchiedMap.put(null, null, o);
        }
        return (ChainedMap) o;
    }

    /**
     * @param service
     * @return a map containing all the properties for the given service.
     * @see #getProperties(String, String)
     */
    public Map getProperties(String service) {
        return getProperties(service, null);
    }

    /**
     * Return a map containing all the properties for the given port. The map is an immutable snapshot of the properties.
     * Meaning that futur changes to the properties will NOT be reflected in the returned map.
     *
     * @param service
     * @param port
     * @return a map containing all the properties for the given port
     */
    public Map getProperties(String service, String port) {
        // no need to go further if no properties
        if (hierarchiedMap.isEmpty()) return Collections.EMPTY_MAP;

        // else check the cache of ChainedMap already converted into immutable maps
        Map cachedMap = (Map) this.cacheOfImmutableMaps.get(service, port);
        if (cachedMap != null) {
            return cachedMap;
        }

        // else get the corresponding ChainedMap and convert it into a Map
        ChainedMap cm = (ChainedMap) hierarchiedMap.get(service, port);
        // if this port is not explicitly mentioned in the multimap, get the default values.
        if (cm == null) {
            cm = (ChainedMap) hierarchiedMap.get(service, null);
            if (cm == null) {
                // return the cached version of the root map
                return getProperties(null, null);
            }
        }
        Map snapshotMap = new HashMap(cm.size() * 15 / 10);
        for (Object key : cm.keySet()) {
            snapshotMap.put(key, cm.get(key));
        }
        snapshotMap = Collections.unmodifiableMap(snapshotMap);
        // put it in cache to avoid creating one map at each invocation
        this.cacheOfImmutableMaps.put(service, port, snapshotMap);
        return snapshotMap;
    }

    public String getProperty(String property) {
        return (String) getRootMap().get(property);
    }

    public String getProperty(String service, String property) {
        return getProperty(service, null, property);
    }

    public String getProperty(String service, String port, String property) {
        ChainedMap cm = (ChainedMap) hierarchiedMap.get(service, port);
        // if this port is not explicitly mentioned in the multimap, get the default values.
        if (cm == null) cm = getRootMap();
        return (String) cm.get(property);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private String[] parseProperty(String property) {
        // service, port, targeted property
        String[] res = new String[3];

        int index = property.indexOf(prefix);
        if (index <= 0) {
            // assume there is no service/port prefixed, no need to go further
            res[2] = property;
        } else {
            res[2] = property.substring(index + prefix.length()); // targeted property
            String prefix = property.substring(0, index);
            String[] t = prefix.split("\\.");
            if (t.length > 2) {
                throw new IllegalArgumentException("'.' cannot be mentioned more than twice in the before the property prefix");
            }
            if (t.length >= 1) {
                res[0] = t[0]; // service name
            }
            if (t.length > 1) {
                res[1] = t[1]; // port name
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
