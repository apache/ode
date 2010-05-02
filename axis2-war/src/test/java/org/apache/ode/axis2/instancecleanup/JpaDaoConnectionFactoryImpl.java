package org.apache.ode.axis2.instancecleanup;


import java.util.Properties;
import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;

import org.apache.ode.dao.bpel.ProcessDAO;
import org.apache.ode.dao.bpel.ProcessInstanceDAO;
import org.apache.ode.dao.bpel.ProcessInstanceProfileDAO;
import org.apache.ode.dao.bpel.ProcessProfileDAO;
import org.apache.ode.dao.jpa.JpaOperator;
import org.apache.ode.dao.jpa.openjpa.BpelDAOConnectionFactoryImpl;
import org.apache.ode.dao.jpa.bpel.BpelDAOConnectionImpl;
import org.apache.ode.dao.jpa.bpel.ProcessDAOImpl;
import org.apache.ode.dao.jpa.bpel.ProcessInstanceDAOImpl;
import org.apache.ode.dao.jpa.ProcessInstanceProfileDAOImpl;
import org.apache.ode.dao.jpa.ProcessProfileDAOImpl;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.event.PersistListener;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;

public class JpaDaoConnectionFactoryImpl extends BpelDAOConnectionFactoryImpl implements PersistListener {
    private static ProcessInstanceDAO instance;
    private static ProcessDAO process;
    
    public static ProcessInstanceDAO getInstance() {
        return instance;
    }

    public static ProcessDAO getProcess() {
        return process;
    }

    @Override
    public void init(Properties properties, TransactionManager mgr, Object env) {
        super.init(properties,mgr,env);
        if( _emf instanceof OpenJPAEntityManagerFactorySPI ) {
            ((OpenJPAEntityManagerFactorySPI)_emf).addLifecycleListener(this, ProcessInstanceDAOImpl.class, ProcessDAOImpl.class);
        }
    }

    @Override
    protected BpelDAOConnectionImpl createBPELDAOConnection(EntityManager em, TransactionManager mgr, JpaOperator operator) {
        return new ProfilingBPELDAOConnectionImpl(em, mgr, operator);
    }
    
    public void afterPersist(LifecycleEvent event) {
        if( event.getSource() instanceof ProcessInstanceDAOImpl ) {
            instance = (ProcessInstanceDAOImpl)event.getSource();
        } else {
            process = (ProcessDAOImpl)event.getSource();
        }
    }

    public void beforePersist(LifecycleEvent event) {
    }
    
    public static class ProfilingBPELDAOConnectionImpl extends BpelDAOConnectionImpl implements ProfilingBpelDAOConnection {
        public ProfilingBPELDAOConnectionImpl(EntityManager em, TransactionManager mgr, JpaOperator operator) {
            super(em, mgr, operator);
        }
        
        public ProcessProfileDAO createProcessProfile(ProcessDAO process) {
            return new ProcessProfileDAOImpl(_em, (ProcessDAOImpl)process);
        }

        public ProcessInstanceProfileDAO createProcessInstanceProfile(ProcessInstanceDAO instance) {
            return new ProcessInstanceProfileDAOImpl(_em, (ProcessInstanceDAOImpl)instance);
        }
    }
}