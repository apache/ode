package org.apache.ode.scheduler.simple;

import java.sql.Connection;

import org.apache.ode.scheduler.simple.DatabaseDelegate;
import org.apache.ode.scheduler.simple.JdbcDelegate;


/**
 * Support class for creating a JDBC delegate (using local MYSQL db).
 * 
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 */
public class DelegateSupport {
/*
    private MysqlDataSource _ds;
    private JdbcDelegate _del;

    public DelegateSupport() throws Exception {
        _ds = new MysqlDataSource();
        _ds.setUser("root");
        _ds.setUrl("jdbc:mysql://localhost/ode");
        
        _del = new JdbcDelegate(_ds);
        truncate();
    }

    public DatabaseDelegate delegate() {
        return _del;
    }
    
    public void truncate() throws Exception {
        Connection c = _ds.getConnection();
        try {
            c.createStatement().execute("truncate table ode_job");
        } finally {
            c.close();
        }
        
    }
    */
}

