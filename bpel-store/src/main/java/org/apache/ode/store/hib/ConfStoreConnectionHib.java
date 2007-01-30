package org.apache.ode.store.hib;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.store.ConfStoreConnection;
import org.apache.ode.store.DeploymentUnitDAO;
import org.apache.ode.store.ProcessConfDAO;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Date;

/**
 * Connection to a Hibernate data store. Essentially a thin wrapper around Hibernate's
 * {@link org.hibernate.Session} interface.
 * @author mriou <mriou at apache dot org>
 */
public class ConfStoreConnectionHib implements ConfStoreConnection {

    private static final Log __log = LogFactory.getLog(ConfStoreConnectionHib.class);
    private Session _session;
    private Transaction _tx;

    static final ThreadLocal<Session> _current = new ThreadLocal<Session>();

    public ConfStoreConnectionHib(Session session) {
        _session = session;
        _current.set(session);
    }

    public ProcessConfDAO getProcess(QName pid) {
        try {
            return (ProcessConfDaoImpl) _session.get(ProcessConfDaoImpl.class,pid.toString());
        } catch (HibernateException e) {
            __log.error("DbError", e);
            throw e;
        }
    }


    public DeploymentUnitDAO createDeploymentUnit(String name) {
        DeploymentUnitDaoImpl du = new DeploymentUnitDaoImpl();
        du.setName(name);
        du.setDeployDate(new Date());
        _session.save(du);
        return du;
    }

    public DeploymentUnitDAO getDeploymentUnit(String name) {
        try {
            DeploymentUnitDaoImpl du = (DeploymentUnitDaoImpl) _session.get(DeploymentUnitDaoImpl.class,name);
            return du;
        } catch (HibernateException e) {
            __log.error("DbError", e);
            throw e;
        }
    }

    public long getNextVersion() {
        VersionTrackerDAOImpl vt = (VersionTrackerDAOImpl)
                _session.createQuery("from VersionTrackerDAOImpl v ").uniqueResult();
        if (vt == null) return 1;
        else return vt.getVersion() + 1;
    }

    public void setVersion(long version) {
        VersionTrackerDAOImpl vt = (VersionTrackerDAOImpl)
                _session.createQuery("from VersionTrackerDAOImpl v ").uniqueResult();
        if (vt == null) {
            vt = new VersionTrackerDAOImpl();
            vt.setId(1);
        }
        vt.setVersion(version);
        _session.save(vt);
    }

    @SuppressWarnings("unchecked")
    public Collection<DeploymentUnitDAO> getDeploymentUnits() {
        Criteria c = _session.createCriteria(DeploymentUnitDaoImpl.class);
        return c.list();
    }

    public void close() {
        _session.close();
    }

    public void begin() {
        _tx=_session.beginTransaction();
    }

    public void commit() {
        _tx.commit();
    }

    public void rollback() {
        _tx.rollback();
    }

}
