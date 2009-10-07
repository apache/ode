/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.daohib.bpel;

import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Base class for our DAO objects.
 * <p>
 * All subclass methods that might trigger SQL queries should log a message in the log category 'org.apache.ode.bpel.DAO' when entered.
 * A typical message could be "className.methodName". <br/>
 * Typical candidates are setters, finders and getters of entities. Getters of simple properties won't provide relevant information. 
 */
public abstract class HibernateDao {

    // logger used by subclasses to track entered methods that may trigger sql query
    // we don't use the package name to avoid interferences with other logging info.
    static final Log logDao = LogFactory.getLog("org.apache.ode.bpel.DAO");

    protected final SessionManager _sm;
    protected final HObject _hobj;

	protected HibernateDao(SessionManager sessionManager, HObject hobj) {
    _sm = sessionManager;
		_hobj = hobj;
	}

    void entering(String msg){
        // add a prefix to be parser friendly 
        if(logDao.isDebugEnabled()) logDao.debug("entering "+msg);
    }

    void leaving(String msg){
        if(logDao.isDebugEnabled()) logDao.debug("leaving "+msg);
    }

	/**
	 * @see org.apache.ode.utils.dao.DAO#getDHandle()
	 */
	public Serializable getDHandle() {
        return new HibernateHandle(getClass(), _hobj.getClass(), getSession().getIdentifier(_hobj));
	}
  
    protected Session getSession(){
  	    return _sm.getSession();
    }
  
    public HObject getHibernateObj(){
  	    return _hobj;
    }
  
    public Serializable getId() {
        if( _hobj != null ) {
            return _hobj.getId();
        }
        return null;
    }

    public boolean equals(Object obj){
  	    assert obj instanceof HibernateDao;
        return _hobj.getId().equals(((HibernateDao)obj)._hobj.getId());
    }
  
    public int hashCode(){
  	    return _hobj.getId().hashCode();
    }
  
    protected void update() {
        _sm.getSession().update(_hobj);
    }

    public static Object[] toIdArray(Collection<? extends HibernateDao> daos) {
        Object[] ids = new Object[daos.size()];

        int index = 0;
        for(  HibernateDao dao : daos ) {
            ids[index++] = dao.getId();
        }

        return ids;
    }

    @SuppressWarnings("unchecked")
    protected void deleteByIds(Class entity, List<Long> ids) {
        deleteByColumn(entity, "id", ids);
    }

    @SuppressWarnings("unchecked")
    protected void deleteByColumn(Class entity, String column, List<Long> values) {
        if( values != null && values.size() > 0 ) {
            final String delete = "delete from "+entity.getName()+" as e where e."+column+" in (:values)";

            // some databases don't like long lists of values with IN operator
            // so we delete in batches.  Oracle 9i, for instance, doesn't support
            // more than 1000 -- we opt to be conservative.
            final int batchSize = 100;
            
            int index = 0;
            while (index < values.size()) {
                List<Long> subList = values.subList(index, Math.min(index+batchSize, values.size()));
                Query query = getSession().createQuery(delete);
                query.setParameterList("values", subList);
                query.executeUpdate();
                index += batchSize;
            }
        }
    }    
}
