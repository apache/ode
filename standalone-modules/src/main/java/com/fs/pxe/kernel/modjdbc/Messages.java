/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 * 
 */

package com.fs.pxe.kernel.modjdbc;

import com.fs.utils.msg.MessageBundle;

import java.io.Serializable;

public class Messages extends MessageBundle {

  public String msgErrorBindingDataSource(String bindName) {
    return this.format("Error binding JDBC DataSource as \"{0}\" in JNDI.", bindName);
  }

  public String msgErrorRegisteringJdbcDriver(String driver) {
    return this.format("Error registering JDBC driver \"{0}\".", driver);
  }

  public String msgErrorUnbindingDataSource(String bindname) {
    return this.format("Error removing JNDI entry \"{0}\".", bindname);
  }

  public String msgStartedJDBCPool(Serializable dbDriver, String dbUrl, String jndi) {
    return this.format("Bound JDBC connection pool for \"{1}\""
        + "using driver \"{0}\" as \"{2}\".", dbDriver, dbUrl, jndi);
  }

  public String msgErrorTxManagerNotFound(String transactionManagerName) {
    return format("Transaction manager named \"{0}\" not found in JNDI.", transactionManagerName);
  }

  public String msgErrorStartingPool() {
    return "Error starting JDBC connection pool.";
  }

}
