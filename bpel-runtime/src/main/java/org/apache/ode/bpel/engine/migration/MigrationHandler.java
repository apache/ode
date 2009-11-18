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

import org.apache.ode.bpel.engine.Contexts;
import org.apache.ode.bpel.engine.BpelDatabase;
import org.apache.ode.bpel.engine.BpelProcess;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Checks database schema versions and migrates when necessary.
 */
public class MigrationHandler {
    private static final Log __log = LogFactory.getLog(MigrationHandler.class);

    public static final int CURRENT_SCHEMA_VERSION = 6;

    private Contexts _contexts;
    private List<MigrationLink> migrationLinks = new ArrayList<MigrationLink>() {{
        add(new MigrationLink(1, 2, new Migration[] { new CorrelatorsMigration(), new CorrelationKeyMigration() } ));
        add(new MigrationLink(2, 3, new Migration[] { new CorrelationKeySetMigration() } ));
        add(new MigrationLink(4, 3, new Migration[] { new CorrelationKeySetMigration() } ));
        add(new MigrationLink(3, 5, new Migration[] { new CorrelationKeySetDataMigration() } ));
        add(new MigrationLink(5, 6, new Migration[] { new OutstandingRequestsMigration() } ));
    }};


    public MigrationHandler(Contexts _contexts) {
        this._contexts = _contexts;
    }

    public boolean migrate(final Set<BpelProcess> registeredProcesses) {
        if (_contexts.dao.getDataSource() == null) {
            __log.debug("No datasource available, stopping migration. Probably running fully in-memory.");
            return false;
        }

        final int version;
        try {
            version = getDbVersion();
        } catch (Throwable e) {
            __log.info("The ODE_SCHEMA_VERSION database table doesn't exist. Unless you need to migrate your data" +
                    "from a past version, this message can be safely ignored.");
            return false;
        }
        if (version == -1) {
            __log.info("No schema version available from the database, migrations will be skipped.");
            return false;
        }
        if (version == CURRENT_SCHEMA_VERSION) return true;

        try {
            boolean success = _contexts.scheduler.execTransaction(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    ArrayList<Migration> migrations = new ArrayList<Migration>();
                    findMigrations(version, CURRENT_SCHEMA_VERSION, migrations);
                    if (migrations.size() == 0) {
                        __log.error("Don't know how to migrate from " + version + " to " + CURRENT_SCHEMA_VERSION + ", aborting");
                        return false;
                    } else {
                        boolean success = true;
                        for (Migration mig : migrations) {
                            __log.debug("Running migration " + mig);
                            success = mig.migrate(registeredProcesses, _contexts.dao.getConnection()) && success;
                        }

                        if (!success) _contexts.scheduler.setRollbackOnly();
                        else setDbVersion(CURRENT_SCHEMA_VERSION);
                        return success;
                    }
                }
            });
            return success;
        } catch (Exception e) {
            __log.error("An error occured while migrating your database to a newer version of ODE, changes have " +
                    "been aborted", e);
            throw new RuntimeException(e);
        }
    }

    private static class MigrationLink {
        int source;
        int target;
        Migration[] migrations;
        public MigrationLink(int source, int target, Migration[] migrations) {
            this.source = source;
            this.target = target;
            this.migrations = migrations;
        }
    }

    /**
     * Attempts to find a way from a source to a target and collects the migrations found along. Assumes
     * a directed graph with no loops. Guarantees that migrations are collected in the proper start-to-end
     * order.
     */
    private boolean findMigrations(int source, int target, List<Migration> ms) {
        List<MigrationLink> l = findLinksTo(target);
        for (MigrationLink link : l) {
            if (link.source == source || findMigrations(source, link.source, ms)) {
                ms.addAll(Arrays.asList(link.migrations));
                return true;
            }
        }
        return false;
    }

    /**
     * Finds all the links with a given target.
     */
    private List<MigrationLink> findLinksTo(int target) {
        ArrayList<MigrationLink> mls = new ArrayList<MigrationLink>();
        for (MigrationLink ml : migrationLinks) {
            if (ml.target == target) mls.add(ml);
        }
        return mls;
    }

    private int getDbVersion() {
        int version = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = _contexts.dao.getDataSource().getConnection();
            stmt = conn.prepareStatement("SELECT VERSION FROM ODE_SCHEMA_VERSION");
            rs = stmt.executeQuery();
            if (rs.next()) version = rs.getInt("VERSION");
        } catch (Exception e) {
            // Swallow, we'll just abort based on the version number
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return version;
    }

    private void setDbVersion(int version) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = _contexts.dao.getDataSource().getConnection();
            stmt = conn.createStatement();
            int res = stmt.executeUpdate("UPDATE ODE_SCHEMA_VERSION SET VERSION = " + version);
            // This should never happen but who knows?
            if (res == 0) throw new RuntimeException("Couldn't update schema version.");
        } catch (Exception e) {
            // Swallow, we'll just abort based on the version number
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
