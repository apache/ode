package org.apache.ode.dao.jpa;

import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactoryJDBC;
import org.apache.openjpa.ee.ManagedRuntime;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BPELDAOConnectionFactoryImpl implements BpelDAOConnectionFactoryJDBC {

    private EntityManagerFactory _emf;

    private TransactionManager _tm;

    private DataSource _ds;

    private Object _dbdictionary;

    private DataSource _unmanagedDS;

    public BPELDAOConnectionFactoryImpl() {
    }

    public BpelDAOConnection getConnection() {
        return new BPELDAOConnectionImpl(_emf.createEntityManager());
    }

    public void init(Properties properties) {
        HashMap<String, Object> propMap = new HashMap<String,Object>();

        propMap.put("javax.persistence.nonJtaDataSource", _unmanagedDS == null ? _ds : _unmanagedDS);
        propMap.put("openjpa.Log", "DefaultLevel=TRACE");
//        propMap.put("openjpa.Log", "log4j");
        propMap.put("openjpa.jdbc.DBDictionary", "org.apache.openjpa.jdbc.sql.DerbyDictionary");

//        propMap.put("openjpa.ManagedRuntime", new TxMgrProvider());
//        propMap.put("openjpa.ConnectionDriverName", org.apache.derby.jdbc.EmbeddedDriver.class.getName());
//        propMap.put("javax.persistence.nonJtaDataSource", _unmanagedDS == null ? _ds : _unmanagedDS);
//        propMap.put("javax.persistence.DataSource", _ds);
//        propMap.put("openjpa.Log", "DefaultLevel=TRACE");
//        propMap.put("openjpa.jdbc.DBDictionary", "org.apache.openjpa.jdbc.sql.DerbyDictionary");
        if (_dbdictionary != null)
            propMap.put("openjpa.jdbc.DBDictionary", _dbdictionary);

        if (properties != null)
            for (Map.Entry me : properties.entrySet())
                propMap.put((String)me.getKey(),me.getValue());

        _emf = Persistence.createEntityManagerFactory("ode-dao", propMap);
    }

    public void setTransactionManager(TransactionManager tm) {
        _tm = tm;
    }

    public void setDataSource(DataSource datasource) {
        _ds = datasource;

    }

    public void setDBDictionary(String dbd) {
        _dbdictionary = dbd;
    }

    public void setTransactionManager(Object tm) {
        _tm = (TransactionManager) tm;

    }

    public void setUnmanagedDataSource(DataSource ds) {
        _unmanagedDS = ds;
    }

    public void shutdown() {
        _emf.close();
    }


    private class TxMgrProvider implements ManagedRuntime {
        public TxMgrProvider() {
        }

        public TransactionManager getTransactionManager() throws Exception {
            return _tm;
        }
    }

}
