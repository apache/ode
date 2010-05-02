package org.apache.ode.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.extvar.jdbc.JdbcExternalVariableModule;
import org.apache.ode.il.config.OdeConfigProperties;
import org.apache.ode.il.dbutil.Database;
import org.junit.Test;

/**
 * Simple test of external variables.
 */
public class ExternalVariableTest extends BPELTestAbstract {

    private JdbcExternalVariableModule _jdbcext;
    private Database _db;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        OdeConfigProperties props = new OdeConfigProperties(new Properties(),"");
		_db = new Database(props);
        _db.start();

        _jdbcext = new JdbcExternalVariableModule();
        _jdbcext.registerDataSource("testds", _db.getDataSource());
        _server.registerExternalVariableEngine(_jdbcext);

        Connection conn = _db.getDataSource().getConnection();
        Statement s = conn.createStatement();
        
        dropTable(s, "extvartable1");
        s.execute("create table extvartable1 (" + "id1 VARCHAR(200) PRIMARY KEY," +  " \"_id2_\" VARCHAR(200)," +  "pid VARCHAR(250), " + "iid INT,"
                + "cts TIMESTAMP," + "uts TIMESTAMP," + "foo VARCHAR(250)," + "bar VARCHAR(250))");

        s.execute("insert into extvartable1(id1,pid,foo) values ('123','"
                + new QName("http://ode/bpel/unit-test","HelloWorld2-1").toString()
                + "','thefoo')");

        dropTable(s, "costPerCustomer");
        s.execute("CREATE TABLE costPerCustomer (value0 varchar(250), key1 varchar(250) primary key)");
        
        dropTable(s, "DataTypesTest");
        s.execute("CREATE TABLE DataTypesTest (KEYSTRING VARCHAR(255), STRINGCOL VARCHAR(255), FLOATCOL FLOAT, " 
                + "INTCOL INTEGER, NUMBERCOL NUMERIC, TIMESTAMPCOL TIMESTAMP, BOOLEANCOL SMALLINT)");

        dropTable(s, "GenKey");
        s.execute("CREATE TABLE GenKey (KEYSTRING INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY, STRINGCOL VARCHAR(255))");

        conn.close();
    }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    _db.shutdown();
  }


    
    private static void dropTable(Statement s, String name) {
        try {
            s.execute("drop table "+name);
        } catch (SQLException e) {
            // ignore
        }
    }
    
    @Test
    public void testHelloWorld2() throws Throwable {
        go("/bpel/2.0/ExtVar");
    }

    @Test
    public void testExtVar2() throws Throwable {
        go("/bpel/2.0/ExtVar2");
    }

    @Test
    public void testExtVar3() throws Throwable {
        go("/bpel/2.0/ExtVar3");
    }

    @Test
    public void testExtVarKeyGen() throws Throwable {
        go("/bpel/2.0/ExtVar-GenKey");
    }
    
    /**
     * Test inline variable initialization for external variables
     * @author madars.vitolins _at gmail.com, 2009.04.11
     * @throws Throwable
     */
    @Test
    public void testExtVarInlineInit() throws Throwable {
        go("/bpel/2.0/ExtVarInlineInit");
    }
}
