/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.opentools.minerva.connector;

import javax.resource.spi.ConnectionRequestInfo;
import javax.security.auth.Subject;

/**
 * Wrapper class to encapsulate security and/or connection parameters
 * so the creation of new ManagedConnections can be handled in
 * a PoolObjectFactory.
 * @see org.opentools.minerva.pool.PoolObjectFactory
 * @see org.opentools.minerva.connector.ManagedConnectionPoolFactory
 * @author Aaron Mulder ammulder@alumni.princeton.edu
 */
public class ConnectionParameters {
    public Subject subject;
    public ConnectionRequestInfo info;

    public ConnectionParameters(Subject subject, ConnectionRequestInfo info) {
        this.subject = subject;
        this.info = info;
    }

    /**
     * Compares subject and request info.
     */
    public boolean equals(Object o) {
        if(o == null)
            return false;
        ConnectionParameters p = (ConnectionParameters)o;
        if(subject == null) {
            if(p.subject != null) {
                return false;
            }
            if(info == null) {
                return p.info == null;
            } else {
                return p.info != null && p.info.equals(info);
            }
        } else {
            if(p.subject == null || !p.subject.equals(subject)) {
                return false;
            }

            if(info == null) {
                return p.info == null;
            } else {
                return p.info != null && p.info.equals(info);
            }
        }
    }

    public int hashCode() {
        return subject.hashCode() ^ info.hashCode();
    }
}
