package org.apache.ode.dao.jpa.ojpa;


import java.util.Properties;

import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.dao.jpa.BPELDAOConnectionImpl;

public class BPELDAOConnectionFactoryImpl implements BpelDAOConnectionFactory {

	public BpelDAOConnection getConnection() {
		return new BPELDAOConnectionImpl();
	}

	public void init(Properties properties) {
	}

}
