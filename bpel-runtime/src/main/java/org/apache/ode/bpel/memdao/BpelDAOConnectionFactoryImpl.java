/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.memdao;

import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Connection factory for the in-memory state store.
 */
public class BpelDAOConnectionFactoryImpl implements BpelDAOConnectionFactory {
    private static final Map<QName, ProcessDaoImpl> __StateStore = new HashMap<QName, ProcessDaoImpl>();

    public BpelDAOConnection getConnection() {
        return new BpelDAOConnectionImpl(__StateStore);
    }

    /**
     * @see org.apache.ode.bpel.dao.BpelDAOConnectionFactory#init(java.util.Properties)
     */
    public void init(Properties properties) {
    }

    public void shutdown() {
    }

}
