package org.apache.ode.il.dbutil;

import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.utils.LoggingDataSourceWrapper;
import org.opentools.minerva.MinervaPool;

/**
 * Does the dirty work of setting up / obtaining a DataSource based on the configuration in the {@link OdeConfigProperties} object.
 * 
 * @author mszefler
 * 
 */
public class Database {
    private static final Log __log = LogFactory.getLog(Database.class);

    private static final Log __logSql = LogFactory.getLog("org.apache.ode.sql");

    private static final Messages __msgs = Messages.getMessages(Messages.class);

    private OdeConfigProperties _odeConfig;

    private boolean _started;

    private MinervaPool _minervaPool;

    private TransactionManager _txm;

    private DataSource _datasource;

    private File _workRoot;

    private boolean _needDerbyShutdown;

    private String _derbyUrl;

    public Database(OdeConfigProperties props) {
        if (props == null)
            throw new NullPointerException("Must provide a configuration.");

        _odeConfig = props;
    }

    public void setWorkRoot(File workRoot) {
        _workRoot = workRoot;
    }

    public void setTransactionManager(TransactionManager txm) {
        _txm = txm;
    }

    public synchronized void start() throws DatabaseConfigException {
        if (_started)
            return;
        
        _needDerbyShutdown = false;
        _datasource = null;
        _minervaPool = null;
        
        initDataSource();
        _started = true;
    }

    public synchronized void shutdown() {
        if (!_started)
            return;

        if (_minervaPool != null)
            try {
                __log.debug("shutting down minerva pool.");
                _minervaPool.stop();
                _minervaPool = null;
            } catch (Throwable t) {
                __log.debug("Exception in minervaPool.stop()");
            } finally {
                _minervaPool = null;
            }

        if (_needDerbyShutdown) {
            __log.debug("shutting down derby.");
            EmbeddedDriver driver = new EmbeddedDriver();
            try {
                driver.connect(_derbyUrl + ";shutdown=true", new Properties());
            } catch (SQLException ex) {
                // Shutdown will always return an exeption!
                if (ex.getErrorCode() != 45000)
                    __log.error("Error shutting down Derby: " + ex.getErrorCode(), ex);

            } catch (Throwable ex) {
                __log.debug("Error shutting down Derby.", ex);
            }
        }

        _needDerbyShutdown = false;
        _datasource = null;
        _started = false;
    }

    public DataSource getDataSource() {
        return __logSql.isDebugEnabled() ? new LoggingDataSourceWrapper(_datasource, __logSql) : _datasource;
    }

    private void initDataSource() throws DatabaseConfigException {
        switch (_odeConfig.getDbMode()) {
        case EXTERNAL:
            initExternalDb();
            break;
        case EMBEDDED:
            initEmbeddedDb();
            break;
        case INTERNAL:
            initInternalDb();
            break;
        default:
            break;
        }
    }

    private void initExternalDb() throws DatabaseConfigException {
        try {
            _datasource = (DataSource) lookupInJndi(_odeConfig.getDbDataSource());
            __log.info(__msgs.msgOdeUsingExternalDb(_odeConfig.getDbDataSource()));
        } catch (Exception ex) {
            String msg = __msgs.msgOdeInitExternalDbFailed(_odeConfig.getDbDataSource());
            __log.error(msg, ex);
            throw new DatabaseConfigException(msg, ex);
        }
    }

    private void initInternalDb() throws DatabaseConfigException {
        __log.info(__msgs.msgOdeUsingInternalDb(_odeConfig.getDbIntenralJdbcUrl(), _odeConfig.getDbInternalJdbcDriverClass()));
        initInternalDb(_odeConfig.getDbIntenralJdbcUrl(), _odeConfig.getDbInternalJdbcDriverClass());

    }

    private void initInternalDb(String url, String driverClass) throws DatabaseConfigException {

        __log.debug("Creating Minerva DataSource/Pool for " + url + " with driver " + driverClass);

        _minervaPool = new MinervaPool();
        _minervaPool.setTransactionManager(_txm);
        _minervaPool.getConnectionFactory().setConnectionURL(url);
        _minervaPool.getConnectionFactory().setUserName("sa");
        _minervaPool.getConnectionFactory().setDriver(driverClass);

        _minervaPool.getPoolParams().maxSize = _odeConfig.getPoolMaxSize();
        _minervaPool.getPoolParams().minSize = _odeConfig.getPoolMinSize();
        _minervaPool.getPoolParams().blocking = false;
        _minervaPool.setType(MinervaPool.PoolType.MANAGED);

        try {
            _minervaPool.start();
        } catch (Exception ex) {
            String errmsg = __msgs.msgOdeDbPoolStartupFailed(url);
            __log.error(errmsg, ex);
            throw new DatabaseConfigException(errmsg, ex);
        }

        _datasource = _minervaPool.createDataSource();

    }

    /**
     * Initialize embedded (DERBY) database.
     */
    private void initEmbeddedDb() throws DatabaseConfigException {

        String db;
        switch (_odeConfig.getDbDaoImpl()) {
        case HIBERNATE:
            db = "hibdb";
            break;
        case JPA:
            db = "jpadb";
            break;
        default:
            String errmsg = __msgs.msgUnrecoginizedDaoType(_odeConfig.getDbDaoImpl());
            __log.error(errmsg);
            throw new DatabaseConfigException(errmsg, null);
        }

        String url = "jdbc:derby:" + _workRoot + "/" + db ;
        __log.info("Using Embedded Derby: " + url);
        _derbyUrl = url;
        initInternalDb(url, org.apache.derby.jdbc.EmbeddedDriver.class.getName());

    }

    @SuppressWarnings("unchecked")
    private <T> T lookupInJndi(String objName) throws Exception {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            InitialContext ctx = null;
            try {
                ctx = new InitialContext();
                return (T) ctx.lookup(objName);
            } finally {
                if (ctx != null)
                    try {
                        ctx.close();
                    } catch (Exception ex1) {
                        __log.error("Error closing JNDI connection.", ex1);
                    }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

}
