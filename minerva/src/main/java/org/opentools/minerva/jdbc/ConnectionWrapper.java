/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.opentools.minerva.jdbc;

import java.sql.*;

/**
 * Wrapper for database connections.  Tracks open statements, last used time,
 * and records errors.  In practice, this is used both as a wrapper for
 * connections in a pool and as a wrapper for connections handed out by an
 * XAConnection.
 * @see javax.sql.XAConnection
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public interface ConnectionWrapper extends Connection {
    /**
     * Sets the time this connection (or a statement or result set derived from
     * it) was used.
     */
    public void setLastUsed();
    /**
     * Indicates to the connection that an error occured.  This is typically
     * used by statements and result sets derived from this connection.
     */
    public void setError(SQLException e);
    /**
     * Indicates that a statement derived from this connection was closed.
     * Statements are tracked so that any open statements can be closed when
     * the connection is closed (or reused in a pool).
     */
    public void statementClosed(Statement st);
}
