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
