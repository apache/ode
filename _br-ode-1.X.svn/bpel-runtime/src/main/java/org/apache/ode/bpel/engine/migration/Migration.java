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
