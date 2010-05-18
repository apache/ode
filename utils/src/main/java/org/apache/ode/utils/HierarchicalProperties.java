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
import org.apache.commons.lang.StringUtils;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * This class load a list of regular property files (order matters). The main feature is that property can
 * be chained in three levels. Then when querying for a property, if it's not found in the deepest level,
 * the parent will be queryed and so on.
 * <p/>
 * A prefix must be defined to discriminate the property name and the level-1, level-2 names. The default prefix is {@link #ODE_PREFFIX}.
 * <p/>
 * Properties must respect the following pattern: [level1.[level2.]prefix.]property
 * <p/>
 * A concrete use case could be the definition of properties for wsdl services and ports.
 * <br/>Level 0 would be: values common to all services and ports.
 * <br/>Level 1: values common to a given service.
 * <br/>Level 2: values common to a given port.
 * <p/>
 * For instance, if the property file looks like this:
 * <pre>
 *alias.foo_ns=http://foo.com
 *
 * timeout=40000
 * a_namespace_with_no_alias_defined.film-service.port-of-cannes.ode.timeout=50000
 * <p/>
 * max-redirects=30
 * foo_ns.brel-service.ode.max-redirects=40
 * foo_ns.brel-service.port-of-amsterdam.ode.max-redirects=60
 * </pre>
 * The following values may be expected:
 * <pre>
 * getProperty("max-redirects")                                                         => 30
 * getProperty("http://foo.com", "brel-service", "max-redirects")                       => 40
 * getProperty("http://foo.com", "brel-service", "port-of-amsterdam", "max-redirects")  => 60
 * <p/>
 * getProperty("a_namespace_with_no_alias_defined", "film-service", "timeout")                       => 40000
 * getProperty("a_namespace_with_no_alias_defined", "film-service", "port-of-cannes", "timeout")     => 50000
 * getProperty("http://foo.com", "port-of-amsterdam", "timeout")                                     => 40000
 * </pre>
 * <p/>
 * <p>
 * Values may contain some environment variables. For instance, message=You're using ${java.version}.
 * <p/>
 * <p>
 * If a property name ends with ".file" or ".path", the assumption is made that the associated value is a path and as such is resolved against the path of the file it was loaded from.
 * </p>
 * Assigned properties must not start with 'system.' or 'env.'. These prefix are reserved to access system properties and environment variables.
 *
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class HierarchicalProperties {

    private static final Log log = LogFactory.getLog(HierarchicalProperties.class);

    public static final String ODE_PREFFIX = "ode";

    private File[] files;
    private String prefix;
    private String dotted_prefix;
    /*
        This map contains ChainedMap instances chained according to the (qualified) service and/or port they are associated with.
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
     * @param files  the property file to be loaded. The file may not exist.
     *               But if the file exists it has to be a file (not a directory), otherwhise an IOException is thrown. Files will be loaded in the given order.
     * @param prefix the property prefix
     * @throws IOException
     */
    public HierarchicalProperties(File[] files, String prefix) throws IOException {
        this.files = files;
        this.prefix = prefix;
        this.dotted_prefix = "." + prefix + ".";
        loadFiles();
    }

    public HierarchicalProperties(File[] files) throws IOException {
        this(files, ODE_PREFFIX);
    }

    public HierarchicalProperties(File file, String prefix) throws IOException {
        this(new File[]{file}, prefix);
    }

    public HierarchicalProperties(File file) throws IOException {
        this(new File[]{file}, ODE_PREFFIX);
    }

    public HierarchicalProperties(List<File> propFiles) throws IOException {
        this(propFiles.toArray(new File[propFiles.size()]), ODE_PREFFIX);
    }

    /**
     * Clear all existing content, then read the file and parse each property. Simply logs a message and returns if the file does not exist.
     *
     * @throws IOException if the file is a Directory
     */
    public void loadFiles() throws IOException {
        // #1. clear all existing content
        clear();

        // #3. put the root map
        initRoot();

        for (File file : files) {
            Properties props = loadFile(file);
            if(!props.isEmpty()) processProperties(props, file);
        }
        replacePlaceholders();
    }

    private ChainedMap initRoot() {
        ChainedMap root = new ChainedMap();
        hierarchicalMap.put(null, null, root);
        return root;
    }

    private void processProperties(Properties props, File file) throws IOException {

        validatePropertyNames(props, file);

        Map<String, String> nsByAlias = collectAliases(props, file);

        // #4. process each property

        for (Iterator it = props.entrySet().iterator(); it.hasNext();) {
            Map.Entry e = (Map.Entry) it.next();
            String key = (String) e.getKey();
            String value = (String) e.getValue();


            // parse the property name
            String[] info = parseProperty(key);
            String nsalias = info[0];
            String service = info[1];
            String port = info[2];
            String targetedProperty = info[3];

            QName qname = null;
            if (nsalias != null) {
                qname = new QName(nsByAlias.get(nsalias) != null ? nsByAlias.get(nsalias) : nsalias, service);
            }
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

                // create the map itself and link it to the service map
                p = new ChainedMap(s);
                // put it in the multi-map
                hierarchicalMap.put(qname, port, p);
            }

            if(targetedProperty.endsWith(".file") || targetedProperty.endsWith(".path")){
                String absolutePath = file.toURI().resolve(value).getPath();
                if(log.isDebugEnabled()) log.debug("path: "+value+" resolved into: "+absolutePath);
                value = absolutePath;
            }

            // save the key/value in its chained map
            if(log.isDebugEnabled()) log.debug("New property: "+targetedProperty+" -> "+value);
            p.put(targetedProperty, value);
        }
    }

    private Properties loadFile(File file) throws IOException {
        Properties props = new Properties();
        if (!file.exists()) {
            if (log.isDebugEnabled()) log.debug("File does not exist [" + file + "]");
            return props;
        }
        // #2. read the file
        FileInputStream fis = new FileInputStream(file);
        try {
            if (log.isDebugEnabled()) log.debug("Loading property file: " + file);
            props.load(fis);
        } finally {
            fis.close();
        }
        return props;
    }

    private Map<String, String> collectAliases(Properties props, File file) {
        // gather all aliases
        Map<String, String> nsByAlias = new HashMap<String, String>();

        // replace env variable by their values and collect namespace aliases
        for (Iterator it = props.entrySet().iterator(); it.hasNext();) {
            Map.Entry e = (Map.Entry) it.next();
            String key = (String) e.getKey();
            String value = (String) e.getValue();

            if (key.startsWith("alias.")) {
                // we found an namespace alias
                final String alias = key.substring("alias.".length(), key.length());
                if (log.isDebugEnabled()) log.debug("Alias found: " + alias + " -> " + value);
                if (nsByAlias.containsKey(alias) && value.equals(nsByAlias.get(alias)))
                    throw new RuntimeException("Same alias used twice for 2 different namespaces! file=" + file + ", alias=" + alias);
                nsByAlias.put(alias, value);
                // remove the pair from the Properties
                it.remove();
            }
        }
        return nsByAlias;
    }

    private void validatePropertyNames(Properties props, File file) {
        List invalids = new ArrayList();
        for (Iterator<Object> it = props.keySet().iterator(); it.hasNext();) {
            String name = (String) it.next();
            if(name.startsWith("system.") || name.startsWith("env.")) invalids.add(name);
        }
        if(!invalids.isEmpty()){
            throw new IllegalArgumentException("Property files cannot define properties starting with 'system.' nor 'env.' File="+file+". Invalid names="+StringUtils.join(invalids, ","));
        }
    }


    private void replacePlaceholders() {
        Pattern systemProperty = Pattern.compile("\\$\\{system\\.([^\\}]+)\\}");
        Pattern environmentVariable = Pattern.compile("\\$\\{env\\.([^\\}]+)\\}");
        Pattern localPlaceholder = Pattern.compile("\\$\\{([^\\}]+)\\}");
        for (Iterator it = hierarchicalMap.values().iterator(); it.hasNext();) {
            Map properties = ((ChainedMap) it.next()).child;
            for (Iterator it1 = properties.entrySet().iterator(); it1.hasNext();) {
                Map.Entry e = (Map.Entry) it1.next();
                // /!\ replacement values themselves might contain placeholders. So always retrieve the value from the map entry
                e.setValue(SystemUtils.replaceProperties((String) e.getValue(), localPlaceholder, getRootMap().child));
                e.setValue(SystemUtils.replaceProperties((String) e.getValue(), systemProperty, System.getProperties()));
                e.setValue(SystemUtils.replaceProperties((String) e.getValue(), environmentVariable, System.getenv()));
            }
        }
    }

    /**
     * Clear all content. If {@link #loadFiles()} is not invoked later, all returned values will be null.
     */
    public void clear() {
        hierarchicalMap.clear();
        cacheOfImmutableMaps.clear();
    }

    protected ChainedMap getRootMap() {
        Object o = hierarchicalMap.get(null, null);
        if (o == null) {
            o = initRoot();
        }
        return (ChainedMap) o;
    }

    public Map getProperties(String serviceNamespaceURI, String serviceLocalPart) {
        return getProperties(new QName(serviceNamespaceURI, serviceLocalPart));
    }

    /**
     * @param service
     * @return a map containing all the properties for the given service.
     * @see #getProperties(String, String)
     */
    public Map getProperties(QName service) {
        return getProperties(service, null);
    }

    public Map getProperties(String serviceNamespaceURI, String serviceLocalPart, String port) {
        return getProperties(new QName(serviceNamespaceURI, serviceLocalPart), port);
    }

    /**
     * Return a map containing all the properties for the given port. The map is an immutable snapshot of the properties.
     * Meaning that futur changes to the properties will NOT be reflected in the returned map.
     *
     * @param service
     * @param port
     * @return a map containing all the properties for the given port
     */
    public Map getProperties(QName service, String port) {
        // no need to go further if no properties
        if (hierarchicalMap.isEmpty()) return Collections.EMPTY_MAP;

        // else check the cache of ChainedMap already converted into immutable maps
        Map cachedMap = (Map) this.cacheOfImmutableMaps.get(service, port);
        if (cachedMap != null) {
            return cachedMap;
        }

        // else get the corresponding ChainedMap and convert it into a Map
        ChainedMap cm = (ChainedMap) hierarchicalMap.get(service, port);
        // if this port is not explicitly mentioned in the multimap, get the default values.
        if (cm == null) {
            cm = (ChainedMap) hierarchicalMap.get(service, null);
            if (cm == null) {
                // return the cached version of the root map
                return getProperties((QName) null, null);
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

    public String getProperty(String serviceNamespaceURI, String serviceLocalPart, String property) {
        return getProperty(new QName(serviceNamespaceURI, serviceLocalPart), property);
    }

    public String getProperty(QName service, String property) {
        return getProperty(service, null, property);
    }

    public String getProperty(String serviceNamespaceURI, String serviceLocalPart, String port, String property) {
        return getProperty(new QName(serviceNamespaceURI, serviceLocalPart), port, property);
    }

    public String getProperty(QName service, String port, String property) {
        return (String) getProperties(service, port).get(property);
    }

    public String getPrefix() {
        return prefix;
    }


    private String[] parseProperty(String property) {
        // aliaas ns, service, port, targeted property
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
     * <br/>Methods  {@link #clearLocally()}
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
