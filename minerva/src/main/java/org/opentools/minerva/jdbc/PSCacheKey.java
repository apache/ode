/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.opentools.minerva.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;

/**
 * Temporarily not used.  Identifies a PreparedStatement by
 * SQL, type, and concurrency.
 *
 * @author Aaron Mulder ammulder@alumni.princeton.edu
 */
public class PSCacheKey {
    public Connection con;
    public String sql;
    public int rsType;
    public int rsConcur;

    public PSCacheKey(Connection con, String sql) {
        this.con = con;
        this.sql = sql;
        this.rsType = ResultSet.TYPE_FORWARD_ONLY;
        this.rsConcur = ResultSet.CONCUR_READ_ONLY;
    }

    public PSCacheKey(Connection con, String sql, int rsType, int rsConcur) {
        this.con = con;
        this.sql = sql;
        this.rsType = rsType;
        this.rsConcur = rsConcur;
    }

    public boolean equals(Object o) {
        PSCacheKey key = (PSCacheKey)o;
        return key.con.equals(con) && key.sql.equals(sql) &&
               key.rsType == rsType && key.rsConcur == rsConcur;
    }

    public int hashCode() {
        return con.hashCode() ^ sql.hashCode() ^ rsType ^ rsConcur;
    }
}
