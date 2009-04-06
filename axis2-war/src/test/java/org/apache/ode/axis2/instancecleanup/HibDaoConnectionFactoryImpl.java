package org.apache.ode.axis2.instancecleanup;

import java.util.Properties;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ProcessInstanceProfileDAO;
import org.apache.ode.bpel.dao.ProcessProfileDAO;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.BpelDAOConnectionFactoryImpl;
import org.apache.ode.daohib.bpel.BpelDAOConnectionImpl;
import org.apache.ode.daohib.bpel.ProcessDaoImpl;
import org.apache.ode.daohib.bpel.ProcessInstanceDaoImpl;
import org.apache.ode.daohib.bpel.ProcessInstanceProfileDaoImpl;
import org.apache.ode.daohib.bpel.ProcessProfileDaoImpl;
import org.apache.ode.daohib.bpel.hobj.HProcess;
import org.apache.ode.daohib.bpel.hobj.HProcessInstance;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;

@SuppressWarnings("serial")
public class HibDaoConnectionFactoryImpl extends BpelDAOConnectionFactoryImpl implements PostInsertEventListener {
    private static SessionManager _staticSessionManager;
    private static ProcessInstanceDaoImpl instance;
    private static ProcessDaoImpl process;
    
    @Override
    protected SessionManager createSessionManager(Properties properties, DataSource ds, TransactionManager tm) {
        _staticSessionManager = new SessionManager(properties, ds, tm) {
            @Override
            public Configuration getDefaultConfiguration() throws MappingException {
                Configuration conf = super.getDefaultConfiguration();
                conf.setListener("post-insert", HibDaoConnectionFactoryImpl.this);
                return conf;
            }
        };
        
        return _staticSessionManager;
    }

    public BpelDAOConnection getConnection() {
        return new ProfilingBpelDAOConnectionImpl(_sessionManager);
    }
    
    public static Session getSession() {
        return _staticSessionManager.getSession();
    }
    
    public static ProcessInstanceDAO getInstance() {
        return instance;
    }
    
    public static ProcessDaoImpl getProcess() {
        return process;
    }

    public void onPostInsert(PostInsertEvent e) {
        if( HProcessInstance.class.equals( e.getEntity().getClass() ) ) {
            instance = new ProcessInstanceDaoImpl(_sessionManager, (HProcessInstance)e.getEntity());
        } else if( HProcess.class.equals( e.getEntity().getClass() ) ) {
            process = new ProcessDaoImpl(_sessionManager, (HProcess)e.getEntity());
        }
    }

    public static class ProfilingBpelDAOConnectionImpl extends BpelDAOConnectionImpl implements ProfilingBpelDAOConnection {
        ProfilingBpelDAOConnectionImpl(SessionManager sm) {
            super(sm);
        }
        
        public ProcessProfileDAO createProcessProfile(ProcessDAO process) {
            return new ProcessProfileDaoImpl(_sm, (ProcessDaoImpl)process);
        }

        public ProcessInstanceProfileDAO createProcessInstanceProfile(ProcessInstanceDAO instance) {
            return new ProcessInstanceProfileDaoImpl(_sm, (ProcessInstanceDaoImpl)instance);
        }
    }
}