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
