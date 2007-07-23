package org.apache.ode.scheduler.simple;

import java.sql.Connection;

import org.apache.ode.utils.GUID;
import org.hsqldb.jdbc.jdbcDataSource;


/**
 * Support class for creating a JDBC delegate (using in-mem HSQL db).
 * 
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 */
public class DelegateSupport {

    private jdbcDataSource _ds;
    private JdbcDelegate _del;

    public DelegateSupport() throws Exception {
        _ds = new jdbcDataSource();
        _ds.setDatabase("jdbc:hsqldb:mem:" + new GUID().toString());
        _ds.setUser("sa");
        _ds.setPassword("");
        
        setup();
        _del = new JdbcDelegate(_ds);
    }

    public DatabaseDelegate delegate() {
        return _del;
    }
    
    public void setup() throws Exception {
        Connection c = _ds.getConnection();
        try {
            c.createStatement().executeUpdate("CREATE ALIAS MOD FOR \"org.apache.ode.scheduler.simple.DelegateSupport.mod\";");
        	String sql = "CREATE TABLE \"ODE_JOB\" (\"JOBID\" CHAR(64) NOT NULL, \"TS\" NUMERIC  NOT NULL, \"NODEID\" char(64)  NULL, \"SCHEDULED\" int  NOT NULL, \"TRANSACTED\" int  NOT NULL, \"DETAILS\" BINARY(4096)  NULL, PRIMARY KEY(\"JOBID\"));";
            c.createStatement().executeUpdate(sql);
        } finally {
            c.close();
        }
        
    }
    
    public static long mod(long a, long b) {
    	return a % b;
    }
}

