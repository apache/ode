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

package org.apache.ode.bpel.engine.migration;

import org.apache.ode.bpel.engine.BpelProcess;
import org.apache.ode.bpel.dao.BpelDAOConnection;

import java.util.Set;

/**
 * Implement and add to the list of migrations in MigrationHandler to allow database
 * level migration.
 */
public interface Migration {

    /**
     * All database migrations are run in the same transaction so if one fails, they will
     * all be rollbacked. There are two ways to fail: either return false or throw an
     * exception. The difference is that throwing an exception will stop the server
     * startup whereas returning false will let the server continue its starting
     * cycle and run on the non-migrated data.
     */
    boolean migrate(Set<BpelProcess> registeredProcesses, BpelDAOConnection connection);
}
