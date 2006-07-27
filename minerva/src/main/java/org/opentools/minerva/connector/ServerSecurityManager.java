/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.opentools.minerva.connector;

import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

/**
 * Potential class to encapsulate security for Connectors.
 *
 * @author Aaron Mulder ammulder@alumni.princeton.edu
 */
public interface ServerSecurityManager {
    /**
     * Gets the current subject.  Given the factory (for the request)
     * and the pool name (specified at deployment time) in case the
     * implementation wants to perform some sort of mapping of
     * security information based on the factory.
     */
    public Subject getSubject(ManagedConnectionFactory factory,
                              String poolName);
}
