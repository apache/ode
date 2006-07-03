/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 * 
 */

package com.fs.pxe.kernel.modhibernatedao;

import com.fs.utils.msg.MessageBundle;

public class Messages extends MessageBundle {

  public String msgInvalidHibernatePropertiesURL(String url) {
    return this.format("Invalid Hibernate .properties URL \"{0}\".", url);
  }

  public String msgErrorReadingHibernateProperties(String url) {
    return this.format("Error reading Hibernate property file \"{0}\".", url);
  }

  public String msgStartedHibernate() {
    return this.format("Started Hibernate session manager.");
  }

  public String msgErrorStartingHibernate() {
    return this.format("Error starting Hibernate session manager.");
  }

  public String msgErrorBindingReferences() {
    return this.format("Error binding Connection Factory references in JNDI.");
  }

  public String msgErrorLookingUpTransactionManager(String transactionManager) {
    return this.format("Error looking up transaction manager named \"{0}\" in JNDI.",
        transactionManager);
  }

  public String msgErrorLookingUpDataSource(String dsname) {
    return this.format("Error looking up JDBC DataSource named \"{0}\" in JNDI.",
        dsname);
  }

}
