package org.apache.ode.il.dbutil;

import java.io.File;

import org.apache.ode.il.config.OdeConfigProperties.DaoType;
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
        return format("Error starting Minerva connection pool for \"{0}\".", url);
    }

    public String msgOdeUsingDAOImpl(String className) {
        return format("Using DAO Connection Factory class {0}.", className);
    }

    public String msgDAOInstantiationFailed(String className) {
        return format("Error instantiating DAO Connection Factory class {0}.", className);

    }

    public String msgUnrecoginizedDaoType(DaoType dbDaoImpl) {
        return format("Unsupported/Unrecoginized DAO type {0}. ", dbDaoImpl);
    }

}
