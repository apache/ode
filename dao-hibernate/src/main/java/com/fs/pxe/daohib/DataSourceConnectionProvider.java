package com.fs.pxe.daohib;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.HibernateException;
import org.hibernate.connection.ConnectionProvider;

public class DataSourceConnectionProvider implements ConnectionProvider {

  private Properties _props;
  
  public DataSourceConnectionProvider() {
  }
  
  public void configure(Properties props) throws HibernateException {
    _props = props;
  }

  public Connection getConnection() throws SQLException {
    return SessionManager.getConnection(_props);
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
