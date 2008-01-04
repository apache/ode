package org.apache.ode.test;

import java.sql.Connection;
import java.sql.Statement;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.extvar.jdbc.JdbcExternalVariableModule;
import org.apache.ode.utils.GUID;
import org.hsqldb.jdbc.jdbcDataSource;
import org.junit.Test;

/**
 * Simple test of external variables.
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 * 
 */
public class ExternalVariableTest extends BPELTestAbstract {

    private JdbcExternalVariableModule _jdbcext;

    private jdbcDataSource _ds;

    public void setUp() throws Exception {
        super.setUp();
        _ds = new org.hsqldb.jdbc.jdbcDataSource();
        _ds.setDatabase("jdbc:hsqldb:mem:" + new GUID().toString());
        _ds.setUser("sa");

        _jdbcext = new JdbcExternalVariableModule();
        _jdbcext.registerDataSource("testds", _ds);
        _server.registerExternalVariableEngine(_jdbcext);

        Connection conn = _ds.getConnection();
        Statement s = conn.createStatement();
        s.execute("create table extvartable1 (" + "id1 VARCHAR PRIMARY KEY," + "_id2_ VARCHAR," + "pid VARCHAR, " + "iid INT,"
                + "cts DATETIME," + "uts DATETIME," + "foo VARCHAR," + "bar VARCHAR );");

        s.execute("insert into extvartable1(id1,pid,foo) values ('123','"
                + new QName("http://ode/bpel/unit-test","HelloWorld2-1").toString()
                + "','thefoo');");
        conn.close();

    }

    @Test
    public void testHelloWorld2() throws Throwable {
        go("/bpel/2.0/ExtVar");
    }

}
