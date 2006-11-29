package org.apache.ode.store.hib;

import org.hibernate.Session;


public class HibObj  {

    Session getSession() {
        return ConfStoreConnectionHib._current.get();
    }
    
    protected void delete() {
        getSession().delete(this);
    }
}
