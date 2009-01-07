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

    public static final int CURRENT_SCHEMA_VERSION = 3;

    private Contexts _contexts;
    private List<Object[]> migrations = new ArrayList<Object[]>() {{
        add(new Object[] { 2, new CorrelatorsMigration() });
        add(new Object[] { 2, new CorrelationKeyMigration() });
        add(new Object[] { 3, new CorrelationKeySetMigration() });
    }};

    public MigrationHandler(Contexts _contexts) {
        this._contexts = _contexts;
    }

    public boolean migrate(final Set<BpelProcess> registeredProcesses) {
        if (_contexts.dao.getDataSource() == null) {
            __log.debug("No datasource available, stopping migration. Probably running fully in-memory.");
            return false;
        }

        final int version = getDbVersion();
        if (version == -1) {
            __log.info("No schema version available from the database, migrations will be skipped.");
            return false;
        }

        try {
            boolean success = _contexts.scheduler.execTransaction(new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    boolean res = true;
                    boolean migrated = false;
                    for (Object[] me : migrations) {
                        if (((Integer)me[0]) > version) {
                            __log.debug("Running migration " + me[1]);
                            res = ((Migration)me[1]).migrate(registeredProcesses, _contexts.dao.getConnection()) && res;
                            migrated = true;
                        }
                    }
                    if (!res) _contexts.scheduler.setRollbackOnly();
                    else if (migrated) setDbVersion(CURRENT_SCHEMA_VERSION);
                    return res;
                }
            });
            return success;
        } catch (Exception e) {
            __log.error("An error occured while migrating your database to a newer version of ODE, changes have " +
                    "been aborted", e);
            throw new RuntimeException(e);
        }
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
