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

import java.net.URL;
import java.sql.*;

/**
 * Wraps a result set to track the last used time for the owning connection. That time is updated every time a navigation action is
 * performed on the result set (next, previous, etc.).
 * 
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
@SuppressWarnings("unchecked")
public class ResultSetInPool implements ResultSet {
    private final static String CLOSED = "ResultSet has been closed!";

    private ResultSet impl;

    private StatementInPool st;

    /**
     * Creates a new wrapper from a source result set and statement wrapper.
     */
    ResultSetInPool(ResultSet source, StatementInPool owner) {
        impl = source;
        st = owner;
    }

    /**
     * Updates the last used time for the owning connection to the current time.
     */
    public void setLastUsed() {
        st.setLastUsed();
    }

    /**
     * Indicates that an error occured on the owning statement.
     */
    public void setError(SQLException e) {
        if (st != null)
            st.setError(e);
    }

    /**
     * Gets a reference to the "real" ResultSet. This should only be used if you need to cast that to a specific type to call a
     * proprietary method - you will defeat all the pooling if you use the underlying ResultSet directly.
     */
    public ResultSet getUnderlyingResultSet() {
        return impl;
    }

    // ---- Implementation of java.sql.ResultSet ----

    public boolean absolute(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        setLastUsed();
        try {
            return impl.absolute(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void afterLast() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        setLastUsed();
        try {
            impl.afterLast();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void beforeFirst() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        setLastUsed();
        try {
            impl.beforeFirst();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void cancelRowUpdates() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.cancelRowUpdates();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void clearWarnings() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.clearWarnings();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void close() throws SQLException {
        if (impl != null) {
            try {
                impl.close();
            } catch (SQLException e) {
            }
            impl = null;
        }
        st = null;
    }

    public void deleteRow() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        setLastUsed();
        try {
            impl.deleteRow();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public int findColumn(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.findColumn(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public boolean first() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        setLastUsed();
        try {
            return impl.first();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Array getArray(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getArray(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Array getArray(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getArray(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public java.io.InputStream getAsciiStream(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getAsciiStream(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public java.io.InputStream getAsciiStream(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getAsciiStream(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public java.math.BigDecimal getBigDecimal(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getBigDecimal(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    @SuppressWarnings("deprecation")
    public java.math.BigDecimal getBigDecimal(int arg0, int arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getBigDecimal(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public java.math.BigDecimal getBigDecimal(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getBigDecimal(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    @SuppressWarnings("deprecation")
    public java.math.BigDecimal getBigDecimal(String arg0, int arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getBigDecimal(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public java.io.InputStream getBinaryStream(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getBinaryStream(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public java.io.InputStream getBinaryStream(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getBinaryStream(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Blob getBlob(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getBlob(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Blob getBlob(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getBlob(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public boolean getBoolean(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getBoolean(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public boolean getBoolean(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getBoolean(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public byte getByte(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getByte(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public byte getByte(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getByte(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public byte[] getBytes(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getBytes(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public byte[] getBytes(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getBytes(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public java.io.Reader getCharacterStream(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getCharacterStream(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public java.io.Reader getCharacterStream(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getCharacterStream(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Clob getClob(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getClob(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Clob getClob(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getClob(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public int getConcurrency() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getConcurrency();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public String getCursorName() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getCursorName();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Date getDate(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getDate(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Date getDate(int arg0, java.util.Calendar arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getDate(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Date getDate(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getDate(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Date getDate(String arg0, java.util.Calendar arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getDate(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public double getDouble(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getDouble(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public double getDouble(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getDouble(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public int getFetchDirection() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getFetchDirection();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public int getFetchSize() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getFetchSize();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public float getFloat(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getFloat(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public float getFloat(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getFloat(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public int getInt(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getInt(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public int getInt(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getInt(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public long getLong(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getLong(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public long getLong(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getLong(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getMetaData();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Object getObject(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getObject(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Object getObject(int arg0, java.util.Map arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getObject(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Object getObject(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getObject(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Object getObject(String arg0, java.util.Map arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getObject(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Ref getRef(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getRef(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Ref getRef(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getRef(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public int getRow() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getRow();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public short getShort(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getShort(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public short getShort(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getShort(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Statement getStatement() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getStatement();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public String getString(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getString(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public String getString(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getString(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Time getTime(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getTime(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Time getTime(int arg0, java.util.Calendar arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getTime(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Time getTime(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getTime(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Time getTime(String arg0, java.util.Calendar arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getTime(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Timestamp getTimestamp(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getTimestamp(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Timestamp getTimestamp(int arg0, java.util.Calendar arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getTimestamp(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Timestamp getTimestamp(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getTimestamp(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Timestamp getTimestamp(String arg0, java.util.Calendar arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getTimestamp(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public int getType() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getType();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    @SuppressWarnings("deprecation")
    public java.io.InputStream getUnicodeStream(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getUnicodeStream(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    @SuppressWarnings("deprecation")
    public java.io.InputStream getUnicodeStream(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getUnicodeStream(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public SQLWarning getWarnings() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getWarnings();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void insertRow() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        setLastUsed();
        try {
            impl.insertRow();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public boolean isAfterLast() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.isAfterLast();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public boolean isBeforeFirst() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.isBeforeFirst();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public boolean isFirst() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.isFirst();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public boolean isLast() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.isLast();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public boolean last() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        setLastUsed();
        try {
            return impl.last();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void moveToCurrentRow() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        setLastUsed();
        try {
            impl.moveToCurrentRow();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void moveToInsertRow() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        setLastUsed();
        try {
            impl.moveToInsertRow();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public boolean next() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        setLastUsed();
        try {
            return impl.next();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public boolean previous() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        setLastUsed();
        try {
            return impl.previous();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void refreshRow() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        setLastUsed();
        try {
            impl.refreshRow();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public boolean relative(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        setLastUsed();
        try {
            return impl.relative(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public boolean rowDeleted() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.rowDeleted();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public boolean rowInserted() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.rowInserted();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public boolean rowUpdated() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.rowUpdated();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setFetchDirection(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.setFetchDirection(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setFetchSize(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.setFetchSize(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateAsciiStream(int arg0, java.io.InputStream arg1, int arg2) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateAsciiStream(arg0, arg1, arg2);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateAsciiStream(String arg0, java.io.InputStream arg1, int arg2) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateAsciiStream(arg0, arg1, arg2);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateBigDecimal(int arg0, java.math.BigDecimal arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateBigDecimal(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateBigDecimal(String arg0, java.math.BigDecimal arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateBigDecimal(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateBinaryStream(int arg0, java.io.InputStream arg1, int arg2) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateBinaryStream(arg0, arg1, arg2);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateBinaryStream(String arg0, java.io.InputStream arg1, int arg2) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateBinaryStream(arg0, arg1, arg2);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateBoolean(int arg0, boolean arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateBoolean(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateBoolean(String arg0, boolean arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateBoolean(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateByte(int arg0, byte arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateByte(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateByte(String arg0, byte arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateByte(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateBytes(int arg0, byte[] arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateBytes(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateBytes(String arg0, byte[] arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateBytes(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateCharacterStream(int arg0, java.io.Reader arg1, int arg2) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateCharacterStream(arg0, arg1, arg2);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateCharacterStream(String arg0, java.io.Reader arg1, int arg2) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateCharacterStream(arg0, arg1, arg2);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateDate(int arg0, Date arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateDate(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateDate(String arg0, Date arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateDate(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateDouble(int arg0, double arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateDouble(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateDouble(String arg0, double arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateDouble(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateFloat(int arg0, float arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateFloat(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateFloat(String arg0, float arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateFloat(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateInt(int arg0, int arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateInt(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateInt(String arg0, int arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateInt(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateLong(int arg0, long arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateLong(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateLong(String arg0, long arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateLong(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateNull(int arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateNull(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateNull(String arg0) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateNull(arg0);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateObject(int arg0, Object arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateObject(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateObject(int arg0, Object arg1, int arg2) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateObject(arg0, arg1, arg2);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateObject(String arg0, Object arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateObject(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateObject(String arg0, Object arg1, int arg2) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateObject(arg0, arg1, arg2);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateRow() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        setLastUsed();
        try {
            impl.updateRow();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateShort(int arg0, short arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateShort(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateShort(String arg0, short arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateShort(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateString(int arg0, String arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateString(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateString(String arg0, String arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateString(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateTime(int arg0, Time arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateTime(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateTime(String arg0, Time arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateTime(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateTimestamp(int arg0, Timestamp arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateTimestamp(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateTimestamp(String arg0, Timestamp arg1) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateTimestamp(arg0, arg1);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public boolean wasNull() throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.wasNull();
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public URL getURL(int columnIndex) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getURL(columnIndex);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateArray(int columnIndex, Array x) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateArray(columnIndex, x);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateBlob(columnIndex, x);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateClob(int columnIndex, Clob x) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateClob(columnIndex, x);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateRef(int columnIndex, Ref x) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateRef(columnIndex, x);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public URL getURL(String columnName) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            return impl.getURL(columnName);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateArray(String columnName, Array x) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateArray(columnName, x);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateBlob(String columnName, Blob x) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateBlob(columnName, x);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateClob(String columnName, Clob x) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateClob(columnName, x);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void updateRef(String columnName, Ref x) throws SQLException {
        if (impl == null)
            throw new SQLException(CLOSED);
        try {
            impl.updateRef(columnName, x);
        } catch (SQLException e) {
            setError(e);
            throw e;
        }
    }

    // ---- End Implementation of ResultSet ----
}
