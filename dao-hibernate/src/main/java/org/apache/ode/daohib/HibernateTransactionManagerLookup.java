/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.daohib;

import java.util.Properties;

import javax.transaction.TransactionManager;

import org.hibernate.HibernateException;
import org.hibernate.transaction.TransactionManagerLookup;

/**
 * Implementation of the {@link org.hibernate.transaction.TransactionManagerLookup} interface that
 * uses {@link SessionManager} to obtain the JTA {@link TransactionManager} object.
 */
public class HibernateTransactionManagerLookup implements TransactionManagerLookup {

	/** Constructor. */
	public HibernateTransactionManagerLookup() {
		super();
	}

	public TransactionManager getTransactionManager(Properties props)
			throws HibernateException {
		return SessionManager.getTransactionManager(props);
	}

	public String getUserTransactionName() {
		return null;
	}
}
