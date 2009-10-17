package org.apache.ode.scheduler.simple;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.geronimo.connector.outbound.GenericConnectionManager;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.LocalTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.transaction.manager.RecoverableTransactionManager;
import org.apache.ode.utils.GUID;
import org.tranql.connector.jdbc.JDBCDriverMCF;

public class GeronimoDelegateSupport extends DelegateSupport {
    private static final int CONNECTION_MAX_WAIT_MILLIS = 30000;

    private static final int CONNECTION_MAX_IDLE_MINUTES = 5;

    private GenericConnectionManager _connectionManager;

    public GeronimoDelegateSupport(TransactionManager txm) throws Exception {
    	super(txm);
	}

    @Override
    protected void initialize(TransactionManager txm) throws Exception {
        _ds = createGeronimoDataSource(txm, "jdbc:hsqldb:mem:" + new GUID().toString(), "org.hsqldb.jdbcDriver", "sa", "");
        setup();
        _del = new JdbcDelegate(_ds);
    }

    private DataSource createGeronimoDataSource(TransactionManager txm, String url, String driverClass, String username,String password) {
        TransactionSupport transactionSupport = LocalTransactions.INSTANCE;
        ConnectionTracker connectionTracker = new ConnectionTrackingCoordinator();

        PoolingSupport poolingSupport = new SinglePool(1, 1, 
                CONNECTION_MAX_WAIT_MILLIS,
                CONNECTION_MAX_IDLE_MINUTES,
                true, // match one
                false, // match all
                false); // select one assume match

        _connectionManager = new GenericConnectionManager(
                    transactionSupport,
                    poolingSupport,
                    null,
                    connectionTracker,
                    (RecoverableTransactionManager) txm,
                    getClass().getName(),
                    getClass().getClassLoader());

        JDBCDriverMCF mcf = new JDBCDriverMCF();
        try {
            mcf.setDriver(driverClass);
            mcf.setConnectionURL(url);
            if (username != null) {
                mcf.setUserName(username);
            }
            if (password != null) {
                mcf.setPassword(password);
            }
            _connectionManager.doStart();
            return (DataSource) mcf.createConnectionFactory(_connectionManager);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
