package org.apache.ode.bpel.rapi;

import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

public interface ContextData {

    /**
     * Puts a key/value pair into the context.
     * 
     * @param key
     * @param value
     */
    public abstract void put(String context, String key, String value);

    public abstract void put(ContextName name, String value);

    /**
     * Returns the value for a certain key.
     * 
     * @param key
     * @return
     */
    public abstract String get(String context, String key);

    public abstract String get(ContextName name);

    /**
     * Returns 'true' if a value for a given key is present.
     * 
     * @param key
     * @return
     */
    public abstract boolean isSet(String context, String key);

    public abstract boolean isSet(ContextName name);

    /**
     * Return a list of keys.
     * 
     * @return list of keys
     */
    public abstract String[] getKeys(String context);

    public abstract String[] getContexts();

    public abstract List<ContextName> getContextNames();

    public abstract void removeContext(String name);

    public abstract Element toXML();
    public abstract Element toXML(Set<String> contextFilter);

    public static class ContextName {

        private String namespace;
        private String key;
        
        public ContextName(String namespace, String key) {
            this.namespace = namespace;
            this.key = key;
        }
        
        public String getNamespace() {
            return namespace;
        }
        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }
        public String getKey() {
            return key;
        }
        public void setKey(String key) {
            this.key = key;
        }

        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result
                    + ((namespace == null) ? 0 : namespace.hashCode());
            return result;
        }

        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ContextName other = (ContextName) obj;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            if (namespace == null) {
                if (other.namespace != null)
                    return false;
            } else if (!namespace.equals(other.namespace))
                return false;
            return true;
        }
    }
}