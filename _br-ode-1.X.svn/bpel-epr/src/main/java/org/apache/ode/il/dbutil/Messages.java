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
package org.apache.ode.il.dbutil;

import java.io.File;

import org.apache.ode.utils.msg.MessageBundle;

public class Messages extends MessageBundle {

    public String msgOdeInitHibernatePropertiesNotFound(File expected) {
        return format("Hibernate configuration file \"{0}\" not found, defaults will be used.", expected);
    }

    public String msgOdeUsingExternalDb(String dbDataSource) {
        return format("ODE using external DataSource \"{0}\".", dbDataSource);
    }

    public Object msgOdeUsingInternalDb(String dbIntenralJdbcUrl, String dbInternalJdbcDriverClass) {
        return format("ODE using internal database \"{0}\" with driver {1}.", dbIntenralJdbcUrl, dbInternalJdbcDriverClass);

    }

    public String msgOdeInitExternalDbFailed(String dbDataSource) {
        return format("Failed to resolved external DataSource at \"{0}\".", dbDataSource);
    }

    public String msgOdeInitDAOErrorReadingProperties(File propfile) {
        return format("Error reading DAO properties file \"{0}\".", propfile);
    }

    public String msgOdeDbPoolStartupFailed(String url) {
        return format("Error starting connection pool for \"{0}\".", url);
    }

    public String msgOdeUsingDAOImpl(String className) {
        return format("Using DAO Connection Factory class {0}.", className);
    }

    public String msgDAOInstantiationFailed(String className) {
        return format("Error instantiating DAO Connection Factory class {0}.", className);

    }

}
