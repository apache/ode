/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.opentools.minerva.connector.jdbc;

import javax.resource.spi.ConnectionRequestInfo;

/**
 * JDBC parameters for creating connections.
 * @author Aaron Mulder <ammulder@alumni.princeton.edu>
 * @version $Revision: 4 $
 */
public class JDBCConnectionRequestInfo implements ConnectionRequestInfo {
    public String user;
    public String password;

    public JDBCConnectionRequestInfo() {
    }
    public JDBCConnectionRequestInfo(String user, String password) {
        this.user = user;
        this.password = password;
    }
}