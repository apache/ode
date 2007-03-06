package org.apache.ode.dao.jpa;

import org.apache.openjpa.persistence.OpenJPAPersistence;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class OpenJPADAO {

    protected BPELDAOConnectionImpl getConn() {
        return BPELDAOConnectionFactoryImpl._connections.get();
    }

    protected EntityManager getEM() {
        return OpenJPAPersistence.getEntityManager(this);
    }

    /**
     * javax.persistence.Query either let you query for a collection or a single
     * value throwing an exception if nothing is found. Just a convenient shortcut
     * for single results allowing null values
     * @param qry query to execute
     * @return whatever you assign it to
     */
    protected <T> T getSingleResult(Query qry) {
        List res = qry.getResultList();
        if (res.size() == 0) return null;
        return (T) res.get(0);

    }
}
