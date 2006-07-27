package org.apache.ode.bpel.scheduler.quartz;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.quartz.utils.ConnectionProvider;

class DataSourceConnectionProvider implements ConnectionProvider {
  private DataSource _ds;
  
  DataSourceConnectionProvider(DataSource ds) {
    _ds = ds;
  }
  
  public Connection getConnection() throws SQLException {
    return _ds.getConnection();
  }

  public void shutdown() throws SQLException {

  }

}
