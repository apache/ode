package org.apache.ode.store.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.store.Messages;
import org.apache.ode.store.hobj.HProcessConf;
import org.apache.ode.store.hobj.HProcessProperty;
import org.apache.ode.utils.msg.MessageBundle;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.criterion.Expression;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.DialectFactory;
import org.hibernate.transaction.TransactionManagerLookup;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * @author mriou <mriou at apache dot org>
 */
public class ConfStoreConnectionHib implements ConfStoreConnection {

    private static final Log __log = LogFactory.getLog(ConfStoreConnectionHib.class);
    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    private static DataSource _ds;
    private final SessionFactory _sessionFactory;
    private static TransactionManager _txMgr = null;

    public ConfStoreConnectionHib(DataSource _ds, File appRoot, TransactionManager txMgr) {
        org.apache.ode.store.dao.ConfStoreConnectionHib._ds = _ds;
        _txMgr = txMgr;
        Properties properties = new Properties();
        properties.put(Environment.CONNECTION_PROVIDER, DataSourceConnectionProvider.class.getName());
        if (_txMgr != null) {
            properties.put(Environment.TRANSACTION_MANAGER_STRATEGY,
                    HibernateTransactionManagerLookup.class.getName());
            properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "jta");
        } else {
            properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        }

        try {
            properties.put(Environment.DIALECT, guessDialect(_ds));
        } catch (Exception ex) {
            String errmsg = __msgs.msgOdeInitHibernateDialectDetectFailed();
            __log.error(errmsg,ex);
            throw new BpelEngineException(errmsg,ex);
        }

        File hibernatePropFile;
        String confDir = System.getProperty("org.apache.ode.configDir");
        if (confDir != null) hibernatePropFile = new File(confDir, "hibernate.properties");
        else hibernatePropFile = new File(appRoot, "conf" + File.separatorChar + "hibernate.properties");

        if (hibernatePropFile.exists()) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(hibernatePropFile);
                properties.load(new BufferedInputStream(fis));
            } catch (IOException e) {
                String errmsg = __msgs
                        .msgOdeInitHibernateErrorReadingHibernateProperties(hibernatePropFile);
                __log.error(errmsg, e);
                throw new BpelEngineException(errmsg, e);
            }
        } else {
            __log.info(__msgs.msgOdeInitHibernatePropertiesNotFound(hibernatePropFile));
        }

        _sessionFactory = getDefaultConfiguration().setProperties(properties).buildSessionFactory();

    }

    public ProcessConfDAO getProcessConf(QName pid) {
        try {
            Criteria criteria = _sessionFactory.getCurrentSession().createCriteria(HProcessConf.class);
            criteria.add(Expression.eq("processId", pid.toString()));
            // For the moment we are expecting only one result.
            HProcessConf hprocess = (HProcessConf) criteria.uniqueResult();
            return hprocess == null ? null : new ProcessConfDAOHib(_sessionFactory, hprocess);
        } catch (HibernateException e) {
            __log.error("DbError", e);
            throw e;
        }
    }

    public List<ProcessConfDAO> getActiveProcesses() {
        try {
            Criteria criteria = _sessionFactory.getCurrentSession().createCriteria(HProcessConf.class);
            criteria.add(Expression.eq("active", Boolean.TRUE));
            // For the moment we are expecting only one result.
            List hprocesses = criteria.list();
            ArrayList<ProcessConfDAO> result = new ArrayList<ProcessConfDAO>(hprocesses.size());
            for (Object hprocess : hprocesses) {
                HProcessConf hpc = (HProcessConf)hprocess;
                result.add(new ProcessConfDAOHib(_sessionFactory, hpc));
            }
            return result;
        } catch (HibernateException e) {
            __log.error("DbError", e);
            throw e;
        }
    }

    public ProcessConfDAO createProcess(QName pid, QName type) {
        HProcessConf process = new HProcessConf();
        process.setProcessId(pid.toString());
        process.setTypeName(type.getLocalPart());
        process.setTypeNamespace(type.getNamespaceURI());
        process.setDeployDate(new Date());
        _sessionFactory.getCurrentSession().save(process);
        return new ProcessConfDAOHib(_sessionFactory, process);
    }


    public static Configuration getDefaultConfiguration() throws MappingException {
        return new Configuration().addClass(HProcessConf.class).addClass(HProcessProperty.class);
    }

    private String guessDialect(DataSource dataSource) throws Exception {
        String dialect = null;
        // Open a connection and use that connection to figure out database
        // product name/version number in order to decide which Hibernate
        // dialect to use.
        Connection conn = dataSource.getConnection();
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            if (metaData != null) {
                String dbProductName = metaData.getDatabaseProductName();
                int dbMajorVer = metaData.getDatabaseMajorVersion();
                __log.info("Using database " + dbProductName + " major version "
                        + dbMajorVer);
                DialectFactory.DatabaseDialectMapper mapper = HIBERNATE_DIALECTS.get(dbProductName);
                if (mapper != null) {
                    dialect = mapper.getDialectClass(dbMajorVer);
                } else {
                    Dialect hbDialect = DialectFactory.determineDialect(dbProductName, dbMajorVer);
                    if (hbDialect != null)
                        dialect = hbDialect.getClass().getName();
                }
            }
        } finally {
            conn.close();
        }

        if (dialect == null) {
            __log
                    .info("Cannot determine hibernate dialect for this database: using the default one.");
            dialect = DEFAULT_HIBERNATE_DIALECT;
        }

        return dialect;

    }

    public static class DataSourceConnectionProvider implements ConnectionProvider {
        public DataSourceConnectionProvider() {
        }

        public void configure(Properties props) throws HibernateException {
        }

        public Connection getConnection() throws SQLException {
            return _ds.getConnection();
        }

        public void closeConnection(Connection arg0) throws SQLException {
            arg0.close();
        }

        public void close() throws HibernateException {
        }

        public boolean supportsAggressiveRelease() {
            return true;
        }
    }

    public static class HibernateTransactionManagerLookup implements TransactionManagerLookup {
        /** Constructor. */
        public HibernateTransactionManagerLookup() {
            super();
        }

        public TransactionManager getTransactionManager(Properties props)
                throws HibernateException {
            return _txMgr;
        }

        public String getUserTransactionName() {
            return null;
        }
    }

    private static final String DEFAULT_HIBERNATE_DIALECT = "org.hibernate.dialect.DerbyDialect";
    private static final HashMap<String, DialectFactory.VersionInsensitiveMapper> HIBERNATE_DIALECTS = new HashMap<String, DialectFactory.VersionInsensitiveMapper>();

    static {
        // Hibernate has a nice table that resolves the dialect from the database
        // product name,
        // but doesn't include all the drivers. So this is supplementary, and some
        // day in the
        // future they'll add more drivers and we can get rid of this.
        // Drivers already recognized by Hibernate:
        // HSQL Database Engine
        // DB2/NT
        // MySQL
        // PostgreSQL
        // Microsoft SQL Server Database, Microsoft SQL Server
        // Sybase SQL Server
        // Informix Dynamic Server
        // Oracle 8 and Oracle >8
        HIBERNATE_DIALECTS.put("Apache Derby",
                new DialectFactory.VersionInsensitiveMapper(
                        "org.hibernate.dialect.DerbyDialect"));
    }

    /**
     * Execute a self-contained database transaction.
     * @param callable database transaction
     * @return callable result
     */
    public <T> T exec(final Callable<T> callable) throws Exception {
        boolean txStarted = _txMgr != null && _txMgr.getTransaction() != null;
        if (_txMgr.getTransaction() != null) System.out.println("### " + _txMgr.getTransaction().getStatus());
        try {
            if (!txStarted) {
                if (_txMgr == null) _sessionFactory.getCurrentSession().beginTransaction();
                else _txMgr.begin();
            }

            T result =  callable.run();
            if (!txStarted) {
                if (_txMgr == null) _sessionFactory.getCurrentSession().getTransaction().commit();
                else _txMgr.commit();
            }
            return result;
        } catch (Exception e) {
            if (!txStarted) {
                if (_txMgr == null) _sessionFactory.getCurrentSession().getTransaction().rollback();
                else _txMgr.rollback();
            }
            throw e;
        }
    }

}
