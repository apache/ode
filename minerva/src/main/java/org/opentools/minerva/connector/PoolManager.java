
package org.opentools.minerva.connector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.opentools.minerva.pool.ObjectPool;
import org.opentools.minerva.pool.PoolObjectFactory;
import org.opentools.minerva.pool.PoolParameters;

@SuppressWarnings("unchecked")
public class PoolManager {
    private Map pools;

    public PoolManager() {
        pools = new HashMap();
    }

    public void addPerFactoryPool(ManagedConnectionFactory conFactory, PoolObjectFactory poolFactory, PoolParameters params, String name) {
        pools.put(conFactory, new FactoryRecord(poolFactory, params, name));
    }

    public void addPerUserPool(ManagedConnectionFactory conFactory, PoolObjectFactory poolFactory, PoolParameters params, String name) {
        pools.put(conFactory, new UserRecord(poolFactory, params, name));
    }

    public ObjectPool getPool(ManagedConnectionFactory factory, Subject subject, Object connectionRequestInfo) throws ResourceException {
        Record rec = (Record)pools.get(factory);
        if(rec == null) {
            return null;
        } else {
            return rec.getPool(subject, connectionRequestInfo);
        }
    }

    public int getFactoryCount() {
        return pools.size();
    }

    public void clear() {
        for(Iterator it = pools.values().iterator(); it.hasNext();) {
            ((Record)it.next()).clear();
        }
        pools.clear();
    }

    public String getPoolName(ManagedConnectionFactory factory) {
        Record rec = (Record)pools.get(factory);
        return rec == null ? null : rec.getName();
    }

    private static ObjectPool createPool(PoolObjectFactory factory, PoolParameters params, String name) {
        ObjectPool pool = new ObjectPool(factory, name);
        pool.setBlocking(params.blocking);
        pool.setGCEnabled(params.gcEnabled);
        pool.setGCInterval(params.gcIntervalMillis);
        pool.setGCMinIdleTime(params.gcMinIdleMillis);
        pool.setIdleTimeout(params.idleTimeoutMillis);
        pool.setIdleTimeoutEnabled(params.idleTimeoutEnabled);
        pool.setInvalidateOnError(params.invalidateOnError);
        pool.setLogWriter(params.logger);
        pool.setMaxIdleTimeoutPercent(params.maxIdleTimeoutPercent);
        pool.setMaxSize(params.maxSize);
        pool.setMinSize(params.minSize);
        pool.setTimestampUsed(params.trackLastUsed);
        pool.initialize();
        return pool;
    }

    private static interface Record {
        ObjectPool getPool(Subject subject, Object info) throws ResourceException;
        void clear();
        String getName();
    }

    private class FactoryRecord implements Record {
        ObjectPool pool;

        public FactoryRecord(PoolObjectFactory factory, PoolParameters params, String name) {
            pool = createPool(factory, params, name);
        }

        public ObjectPool getPool(Subject subject, Object info) throws ResourceException {
            return pool;
        }

        public String getName() {
            return pool.getName();
        }

        public void clear() {
            pool.shutDown();
            pool = null;
        }
    }

    private class UserRecord implements Record {
        Map users;
        PoolObjectFactory factory;
        PoolParameters params;
        String name;

        public UserRecord(PoolObjectFactory factory, PoolParameters params, String name) {
            users = new HashMap();
            this.factory = factory;
            this.params = params;
            this.name = name;
        }

        public ObjectPool getPool(Subject sub, Object info) throws ResourceException {
            Object user = null;
            // Check the Subject for a user, or use the entire request info otherwise
            if(sub != null) {
                Set creds = sub.getPrivateCredentials(javax.resource.spi.security.PasswordCredential.class);
                for(Iterator it = creds.iterator(); it.hasNext(); ) {
                    PasswordCredential pc = (PasswordCredential)it.next();
                    user = pc.getUserName();
                    break;
                }
            } else if(info != null) {
                user = info;
            }
            // Find the pool for the specified user
            if(user == null) {
                throw new ResourceException("For per-user pooling, must set container-managed sign-on or provide connection parameters!");
            }
            ObjectPool pool = (ObjectPool)users.get(user);
            // Create a pool if necessary
            if(pool == null) {
                pool = createPool(factory, params, name+"-"+users.size()+":"+user);
                users.put(user, pool);
            }
            return pool;
        }

        public String getName() {
            return name;
        }

        public void clear() {
            for(Iterator it = users.values().iterator(); it.hasNext();) {
                ((ObjectPool)it.next()).shutDown();
            }
            users.clear();
            users = null;
            factory = null;
            params = null;
        }
    }
}