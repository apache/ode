/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.opentools.minerva.connector.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import javax.resource.ResourceException;
import javax.resource.spi.LocalTransaction;

/**
 * LocalTransaction implementation for JDBC connections.
 * @author Aaron Mulder <ammulder@alumni.princeton.edu>
 * @version $Revision: 4 $
 */
public class JDBCLocalTransaction implements LocalTransaction {
    private Connection con;

    public JDBCLocalTransaction(Connection con) {
        this.con = con;
    }

    public void begin() throws ResourceException {
    }

    public void commit() throws ResourceException {
        try {
            con.commit();
        } catch(SQLException e) {
            throw new ResourceException("Unable to commit DB connection: "+e);
        } finally {
            con = null;
        }
    }

    public void rollback() throws ResourceException {
        try {
            con.rollback();
        } catch(SQLException e) {
            throw new ResourceException("Unable to rollback DB connection: "+e);
        } finally {
            con = null;
        }
    }
}
