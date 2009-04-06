package org.apache.ode.axis2.instancecleanup;

import java.util.Properties;

import javax.persistence.EntityManager;

import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ProcessInstanceProfileDAO;
import org.apache.ode.bpel.dao.ProcessProfileDAO;
import org.apache.ode.dao.jpa.BPELDAOConnectionFactoryImpl;
import org.apache.ode.dao.jpa.BPELDAOConnectionImpl;
import org.apache.ode.dao.jpa.ProcessDAOImpl;
import org.apache.ode.dao.jpa.ProcessInstanceDAOImpl;
import org.apache.ode.dao.jpa.ProcessInstanceProfileDAOImpl;
import org.apache.ode.dao.jpa.ProcessProfileDAOImpl;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.event.PersistListener;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;

public class JpaDaoConnectionFactoryImpl extends BPELDAOConnectionFactoryImpl implements PersistListener {
    private static ProcessInstanceDAO instance;
    private static ProcessDAO process;
    
    public static ProcessInstanceDAO getInstance() {
        return instance;
    }

    public static ProcessDAO getProcess() {
        return process;
    }

    @Override
    public void init(Properties properties) {
        super.init(properties);
        if( _emf instanceof OpenJPAEntityManagerFactorySPI ) {
            ((OpenJPAEntityManagerFactorySPI)_emf).addLifecycleListener(this, ProcessInstanceDAOImpl.class, ProcessDAOImpl.class);
        }
    }
    
    @Override
    protected BPELDAOConnectionImpl createBPELDAOConnection(EntityManager em) {
        return new ProfilingBPELDAOConnectionImpl(em);
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
    
    public static class ProfilingBPELDAOConnectionImpl extends BPELDAOConnectionImpl implements ProfilingBpelDAOConnection {
        public ProfilingBPELDAOConnectionImpl(EntityManager em) {
            super(em);
        }
        
        public ProcessProfileDAO createProcessProfile(ProcessDAO process) {
            return new ProcessProfileDAOImpl(_em, (ProcessDAOImpl)process);
        }

        public ProcessInstanceProfileDAO createProcessInstanceProfile(ProcessInstanceDAO instance) {
            return new ProcessInstanceProfileDAOImpl(_em, (ProcessInstanceDAOImpl)instance);
        }
    }
}