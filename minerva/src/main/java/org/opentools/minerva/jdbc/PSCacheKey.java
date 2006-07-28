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
