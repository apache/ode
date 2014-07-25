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
package org.apache.ode.il.dbutil;

import java.io.File;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactoryJDBC;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.utils.LoggingInterceptor;

/**
 * Does the dirty work of setting up / obtaining a DataSource based on the configuration in the {@link OdeConfigProperties} object.
 *
 */
public abstract class Database {
    protected static final Log __log = LogFactory.getLog(Database.class);

    protected static final Log __logSql = LogFactory.getLog("org.apache.ode.sql");

    protected static final Messages __msgs = Messages.getMessages(Messages.class);

    protected OdeConfigProperties _odeConfig;

    protected boolean _started;

    protected TransactionManager _txm;

    protected DataSource _datasource;

    protected File _workRoot;

    public static Database create(OdeConfigProperties props) {
        if (props == null)
            throw new NullPointerException("Must provide a configuration.");
        
        switch (props.getDbMode()) {
        case EMBEDDED: 
            switch (props.getDbEmbeddedType()) {
                case DERBY: return new DerbyEmbeddedDB(props);
                case H2: return new H2EmbeddedDB(props);
            }
        case EXTERNAL: return new ExternalDB(props);
        case INTERNAL: return new InternalDB(props);
        default: throw new IllegalStateException();
        }
    }
    
    public Database(OdeConfigProperties props) {
        _odeConfig = props;
    }

    public void setWorkRoot(File workRoot) {
        _workRoot = workRoot;
    }

    public void setTransactionManager(TransactionManager txm) {
        _txm = txm;
    }

    public synchronized void start() throws DatabaseConfigException {
        if (_started)
            return;

        _datasource = null;

        initDataSource();
        _started = true;
    }

    public synchronized void shutdown() {
        if (!_started)
            return;

        _datasource = null;
        _started = false;
    }

    public DataSource getDataSource() {
        //return __logSql.isDebugEnabled() ? new LoggingDataSourceWrapper(_datasource, __logSql) : _datasource;
        return __logSql.isDebugEnabled() ? LoggingInterceptor.createLoggingDS(_datasource, __logSql) : _datasource;
    }

    protected abstract void initDataSource() throws DatabaseConfigException;

    public BpelDAOConnectionFactoryJDBC createDaoCF() throws DatabaseConfigException  {
        String pClassName = _odeConfig.getDAOConnectionFactory();

        __log.info(__msgs.msgOdeUsingDAOImpl(pClassName));

        BpelDAOConnectionFactoryJDBC cf;
        try {
            Class<?> clazz = this.getClass().getClassLoader().loadClass(pClassName);
            cf = (BpelDAOConnectionFactoryJDBC) clazz.newInstance();
        } catch (Exception ex) {
            String errmsg = __msgs.msgDAOInstantiationFailed(pClassName);
            __log.error(errmsg, ex);
            throw new DatabaseConfigException(errmsg, ex);
        }

        cf.setDataSource(getDataSource());
        cf.setTransactionManager(_txm);
        cf.init(_odeConfig.getProperties());
        return cf;
    }

}
