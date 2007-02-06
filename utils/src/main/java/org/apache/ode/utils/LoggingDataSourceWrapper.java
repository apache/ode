package org.apache.ode.utils;

import org.apache.commons.logging.Log;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class LoggingDataSourceWrapper implements DataSource {

    private DataSource _wrapped;
    private Log _log;

    public LoggingDataSourceWrapper(DataSource wrapped, Log log) {
        _wrapped = wrapped;
        _log = log;
    }

    public Connection getConnection() throws SQLException {
        Connection conn = new LoggingConnectionWrapper(_wrapped.getConnection(), _log);
        print("getConnection (tx=" + conn.getTransactionIsolation() + ")");
        return conn;
    }

    public Connection getConnection(String username, String password) throws SQLException {
        Connection conn =  new LoggingConnectionWrapper(_wrapped.getConnection(username, password), _log);
        print("getConnection (tx=" + conn.getTransactionIsolation() + ")");
        return conn;
    }

    public int getLoginTimeout() throws SQLException {
        return _wrapped.getLoginTimeout();
    }

    public PrintWriter getLogWriter() throws SQLException {
        return _wrapped.getLogWriter();
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        _wrapped.setLoginTimeout(seconds);
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        _wrapped.setLogWriter(out);
    }

    private void print(String str) {
        if (_log != null)
            _log.debug(str);
        else System.out.println(str);
    }

}
