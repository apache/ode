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
package org.apache.ode.utils;

import org.apache.commons.logging.Log;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class LoggingStatementWrapper implements CallableStatement {

    private PreparedStatement _stmt;
    private Log _log;
    private HashMap<String,Object> _paramsStr  = new HashMap<String,Object>();
    private HashMap<Integer,Object> _paramsIdxr  = new HashMap<Integer,Object>();

    public LoggingStatementWrapper(CallableStatement stmt, Log log) {
        _stmt = stmt;
        _log = log;
    }
    public LoggingStatementWrapper(PreparedStatement stmt, Log log) {
        _stmt = stmt;
        _log = log;
    }

    public Array getArray(int i) throws SQLException {
        return ((CallableStatement)_stmt).getArray(i);
    }

    public Array getArray(String parameterName) throws SQLException {
        return ((CallableStatement)_stmt).getArray(parameterName);
    }

    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        return ((CallableStatement)_stmt).getBigDecimal(parameterIndex);
    }

    @SuppressWarnings("deprecation")
    public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
        return ((CallableStatement)_stmt).getBigDecimal(parameterIndex, scale);
    }

    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        return ((CallableStatement)_stmt).getBigDecimal(parameterName);
    }

    public Blob getBlob(int i) throws SQLException {
        return ((CallableStatement)_stmt).getBlob(i);
    }

    public Blob getBlob(String parameterName) throws SQLException {
        return ((CallableStatement)_stmt).getBlob(parameterName);
    }

    public boolean getBoolean(int parameterIndex) throws SQLException {
        return ((CallableStatement)_stmt).getBoolean(parameterIndex);
    }

    public boolean getBoolean(String parameterName) throws SQLException {
        return ((CallableStatement)_stmt).getBoolean(parameterName);
    }

    public byte getByte(int parameterIndex) throws SQLException {
        return ((CallableStatement)_stmt).getByte(parameterIndex);
    }

    public byte getByte(String parameterName) throws SQLException {
        return ((CallableStatement)_stmt).getByte(parameterName);
    }

    public byte[] getBytes(int parameterIndex) throws SQLException {
        return ((CallableStatement)_stmt).getBytes(parameterIndex);
    }

    public byte[] getBytes(String parameterName) throws SQLException {
        return ((CallableStatement)_stmt).getBytes(parameterName);
    }

    public Clob getClob(int i) throws SQLException {
        return ((CallableStatement)_stmt).getClob(i);
    }

    public Clob getClob(String parameterName) throws SQLException {
        return ((CallableStatement)_stmt).getClob(parameterName);
    }

    public Date getDate(int parameterIndex) throws SQLException {
        return ((CallableStatement)_stmt).getDate(parameterIndex);
    }

    public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
        return ((CallableStatement)_stmt).getDate(parameterIndex, cal);
    }

    public Date getDate(String parameterName) throws SQLException {
        return ((CallableStatement)_stmt).getDate(parameterName);
    }

    public Date getDate(String parameterName, Calendar cal) throws SQLException {
        return ((CallableStatement)_stmt).getDate(parameterName, cal);
    }

    public double getDouble(int parameterIndex) throws SQLException {
        return ((CallableStatement)_stmt).getDouble(parameterIndex);
    }

    public double getDouble(String parameterName) throws SQLException {
        return ((CallableStatement)_stmt).getDouble(parameterName);
    }

    public float getFloat(int parameterIndex) throws SQLException {
        return ((CallableStatement)_stmt).getFloat(parameterIndex);
    }

    public float getFloat(String parameterName) throws SQLException {
        return ((CallableStatement)_stmt).getFloat(parameterName);
    }

    public int getInt(int parameterIndex) throws SQLException {
        return ((CallableStatement)_stmt).getInt(parameterIndex);
    }

    public int getInt(String parameterName) throws SQLException {
        return ((CallableStatement)_stmt).getInt(parameterName);
    }

    public long getLong(int parameterIndex) throws SQLException {
        return ((CallableStatement)_stmt).getLong(parameterIndex);
    }

    public long getLong(String parameterName) throws SQLException {
        return ((CallableStatement)_stmt).getLong(parameterName);
    }

    public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {
        return ((CallableStatement)_stmt).getObject(i, map);
    }

    public Object getObject(int parameterIndex) throws SQLException {
        return ((CallableStatement)_stmt).getObject(parameterIndex);
    }

    public Object getObject(String parameterName) throws SQLException {
        return ((CallableStatement)_stmt).getObject(parameterName);
    }

    public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException {
        return ((CallableStatement)_stmt).getObject(parameterName, map);
    }

    public Ref getRef(int i) throws SQLException {
        return ((CallableStatement)_stmt).getRef(i);
    }

    public Ref getRef(String parameterName) throws SQLException {
        return ((CallableStatement)_stmt).getRef(parameterName);
    }

    public short getShort(int parameterIndex) throws SQLException {
        return ((CallableStatement)_stmt).getShort(parameterIndex);
    }

    public short getShort(String parameterName) throws SQLException {
        return ((CallableStatement)_stmt).getShort(parameterName);
    }

    public String getString(int parameterIndex) throws SQLException {
        return ((CallableStatement)_stmt).getString(parameterIndex);
    }

    public String getString(String parameterName) throws SQLException {
        return ((CallableStatement)_stmt).getString(parameterName);
    }

    public Time getTime(int parameterIndex) throws SQLException {
        return ((CallableStatement)_stmt).getTime(parameterIndex);
    }

    public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
        return ((CallableStatement)_stmt).getTime(parameterIndex, cal);
    }

    public Time getTime(String parameterName) throws SQLException {
        return ((CallableStatement)_stmt).getTime(parameterName);
    }

    public Time getTime(String parameterName, Calendar cal) throws SQLException {
        return ((CallableStatement)_stmt).getTime(parameterName, cal);
    }

    public Timestamp getTimestamp(int parameterIndex) throws SQLException {
        return ((CallableStatement)_stmt).getTimestamp(parameterIndex);
    }

    public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
        return ((CallableStatement)_stmt).getTimestamp(parameterIndex, cal);
    }

    public Timestamp getTimestamp(String parameterName) throws SQLException {
        return ((CallableStatement)_stmt).getTimestamp(parameterName);
    }

    public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
        return ((CallableStatement)_stmt).getTimestamp(parameterName, cal);
    }

    public URL getURL(int parameterIndex) throws SQLException {
        return ((CallableStatement)_stmt).getURL(parameterIndex);
    }

    public URL getURL(String parameterName) throws SQLException {
        return ((CallableStatement)_stmt).getURL(parameterName);
    }

    public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
        ((CallableStatement)_stmt).registerOutParameter(parameterIndex, sqlType);
    }

    public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
        ((CallableStatement)_stmt).registerOutParameter(parameterIndex, sqlType, scale);
    }

    public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
        ((CallableStatement)_stmt).registerOutParameter(parameterName, sqlType);
    }

    public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
        ((CallableStatement)_stmt).registerOutParameter(parameterName, sqlType, scale);
    }

    public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
        ((CallableStatement)_stmt).registerOutParameter(parameterName, sqlType, typeName);
    }

    public void registerOutParameter(int paramIndex, int sqlType, String typeName) throws SQLException {
        ((CallableStatement)_stmt).registerOutParameter(paramIndex, sqlType, typeName);
    }

    public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
        ((CallableStatement)_stmt).setAsciiStream(parameterName, x, length);
    }

    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        _paramsStr.put(parameterName, x);
        ((CallableStatement)_stmt).setBigDecimal(parameterName, x);
    }

    public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
        ((CallableStatement)_stmt).setBinaryStream(parameterName, x, length);
    }

    public void setBoolean(String parameterName, boolean x) throws SQLException {
        _paramsStr.put(parameterName, x);
        ((CallableStatement)_stmt).setBoolean(parameterName, x);
    }

    public void setByte(String parameterName, byte x) throws SQLException {
        _paramsStr.put(parameterName, x);
        ((CallableStatement)_stmt).setByte(parameterName, x);
    }

    public void setBytes(String parameterName, byte[] x) throws SQLException {
        _paramsStr.put(parameterName, x);
        ((CallableStatement)_stmt).setBytes(parameterName, x);
    }

    public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
        ((CallableStatement)_stmt).setCharacterStream(parameterName, reader, length);
    }

    public void setDate(String parameterName, Date x) throws SQLException {
        _paramsStr.put(parameterName, x);
        ((CallableStatement)_stmt).setDate(parameterName, x);
    }

    public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
        _paramsStr.put(parameterName, x);
        ((CallableStatement)_stmt).setDate(parameterName, x, cal);
    }

    public void setDouble(String parameterName, double x) throws SQLException {
        _paramsStr.put(parameterName, x);
        ((CallableStatement)_stmt).setDouble(parameterName, x);
    }

    public void setFloat(String parameterName, float x) throws SQLException {
        _paramsStr.put(parameterName, x);
        ((CallableStatement)_stmt).setFloat(parameterName, x);
    }

    public void setInt(String parameterName, int x) throws SQLException {
        _paramsStr.put(parameterName, x);
        ((CallableStatement)_stmt).setInt(parameterName, x);
    }

    public void setLong(String parameterName, long x) throws SQLException {
        _paramsStr.put(parameterName, x);
        ((CallableStatement)_stmt).setLong(parameterName, x);
    }

    public void setNull(String parameterName, int sqlType) throws SQLException {
        _paramsStr.put(parameterName, "null");
        ((CallableStatement)_stmt).setNull(parameterName, sqlType);
    }

    public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
        _paramsStr.put(parameterName, "null");
        ((CallableStatement)_stmt).setNull(parameterName, sqlType, typeName);
    }

    public void setObject(String parameterName, Object x) throws SQLException {
        _paramsStr.put(parameterName, x);
        ((CallableStatement)_stmt).setObject(parameterName, x);
    }

    public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
        _paramsStr.put(parameterName, x);
        ((CallableStatement)_stmt).setObject(parameterName, x, targetSqlType);
    }

    public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
        _paramsStr.put(parameterName, x);
        ((CallableStatement)_stmt).setObject(parameterName, x, targetSqlType, scale);
    }

    public void setShort(String parameterName, short x) throws SQLException {
        _paramsStr.put(parameterName, x);
        ((CallableStatement)_stmt).setShort(parameterName, x);
    }

    public void setString(String parameterName, String x) throws SQLException {
        _paramsStr.put(parameterName, x);
        ((CallableStatement)_stmt).setString(parameterName, x);
    }

    public void setTime(String parameterName, Time x) throws SQLException {
        _paramsStr.put(parameterName, x);
        ((CallableStatement)_stmt).setTime(parameterName, x);
    }

    public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
        _paramsStr.put(parameterName, x);
        ((CallableStatement)_stmt).setTime(parameterName, x, cal);
    }

    public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
        _paramsStr.put(parameterName, x);
        ((CallableStatement)_stmt).setTimestamp(parameterName, x);
    }

    public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
        _paramsStr.put(parameterName, x);
        ((CallableStatement)_stmt).setTimestamp(parameterName, x, cal);
    }

    public void setURL(String parameterName, URL val) throws SQLException {
        _paramsStr.put(parameterName, val);
        ((CallableStatement)_stmt).setURL(parameterName, val);
    }

    public boolean wasNull() throws SQLException {
        return ((CallableStatement)_stmt).wasNull();
    }

    public void addBatch() throws SQLException {
        _stmt.addBatch();
    }

    public void clearParameters() throws SQLException {
        _paramsStr.clear();
        _paramsIdxr.clear();
        _stmt.clearParameters();
    }

    public boolean execute() throws SQLException {
        printParams();
        return _stmt.execute();
    }

    public ResultSet executeQuery() throws SQLException {
        printParams();
        return _stmt.executeQuery();
    }

    public int executeUpdate() throws SQLException {
        printParams();
        return _stmt.executeUpdate();
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return _stmt.getMetaData();
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        return _stmt.getParameterMetaData();
    }

    public void setArray(int i, Array x) throws SQLException {
        _paramsIdxr.put(i, x);
        _stmt.setArray(i, x);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        _stmt.setAsciiStream(parameterIndex, x, length);
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setBigDecimal(parameterIndex, x);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        _stmt.setBinaryStream(parameterIndex, x, length);
    }

    public void setBlob(int i, Blob x) throws SQLException {
        _stmt.setBlob(i, x);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setBoolean(parameterIndex, x);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setByte(parameterIndex, x);
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setBytes(parameterIndex, x);
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        _stmt.setCharacterStream(parameterIndex, reader, length);
    }

    public void setClob(int i, Clob x) throws SQLException {
        _stmt.setClob(i, x);
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setDate(parameterIndex, x);
    }

    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setDate(parameterIndex, x, cal);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setDouble(parameterIndex, x);
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setFloat(parameterIndex, x);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setInt(parameterIndex, x);
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setLong(parameterIndex, x);
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        _paramsIdxr.put(parameterIndex, "null");
        _stmt.setNull(parameterIndex, sqlType);
    }

    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
        _stmt.setNull(paramIndex, sqlType, typeName);
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setObject(parameterIndex, x);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setObject(parameterIndex, x, targetSqlType);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setObject(parameterIndex, x, targetSqlType, scale);
    }

    public void setRef(int i, Ref x) throws SQLException {
        _paramsIdxr.put(i, x);
        _stmt.setRef(i, x);
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setShort(parameterIndex, x);
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setString(parameterIndex, x);
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setTime(parameterIndex, x);
    }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setTime(parameterIndex, x, cal);
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setTimestamp(parameterIndex, x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setTimestamp(parameterIndex, x, cal);
    }

    @SuppressWarnings("deprecation")
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setUnicodeStream(parameterIndex, x, length);
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        _paramsIdxr.put(parameterIndex, x);
        _stmt.setURL(parameterIndex, x);
    }

    public void addBatch(String sql) throws SQLException {
        _stmt.addBatch(sql);
    }

    public void cancel() throws SQLException {
        _stmt.cancel();
    }

    public void clearBatch() throws SQLException {
        _stmt.clearBatch();
    }

    public void clearWarnings() throws SQLException {
        _stmt.clearWarnings();
    }

    public void close() throws SQLException {
        _stmt.close();
    }

    public boolean execute(String sql) throws SQLException {
        if (shouldPrint()) printParams();
        return _stmt.execute(sql);
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        if (shouldPrint()) printParams();
        return _stmt.execute(sql, autoGeneratedKeys);
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        if (shouldPrint()) printParams();
        return _stmt.execute(sql, columnIndexes);
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        if (shouldPrint()) printParams();
        return _stmt.execute(sql, columnNames);
    }

    public int[] executeBatch() throws SQLException {
        if (shouldPrint()) printParams();
        return _stmt.executeBatch();
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        if (shouldPrint()) printParams();
        return _stmt.executeQuery(sql);
    }

    public int executeUpdate(String sql) throws SQLException {
        if (shouldPrint()) printParams();
        return _stmt.executeUpdate(sql);
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        if (shouldPrint()) printParams();
        return _stmt.executeUpdate(sql, autoGeneratedKeys);
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        if (shouldPrint()) printParams();
        return _stmt.executeUpdate(sql, columnIndexes);
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        if (shouldPrint()) printParams();
        return _stmt.executeUpdate(sql, columnNames);
    }

    public Connection getConnection() throws SQLException {
        return _stmt.getConnection();
    }

    public int getFetchDirection() throws SQLException {
        return _stmt.getFetchDirection();
    }

    public int getFetchSize() throws SQLException {
        return _stmt.getFetchSize();
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return _stmt.getGeneratedKeys();
    }

    public int getMaxFieldSize() throws SQLException {
        return _stmt.getMaxFieldSize();
    }

    public int getMaxRows() throws SQLException {
        return _stmt.getMaxRows();
    }

    public boolean getMoreResults() throws SQLException {
        return _stmt.getMoreResults();
    }

    public boolean getMoreResults(int current) throws SQLException {
        return _stmt.getMoreResults(current);
    }

    public int getQueryTimeout() throws SQLException {
        return _stmt.getQueryTimeout();
    }

    public ResultSet getResultSet() throws SQLException {
        return _stmt.getResultSet();
    }

    public int getResultSetConcurrency() throws SQLException {
        return _stmt.getResultSetConcurrency();
    }

    public int getResultSetHoldability() throws SQLException {
        return _stmt.getResultSetHoldability();
    }

    public int getResultSetType() throws SQLException {
        return _stmt.getResultSetType();
    }

    public int getUpdateCount() throws SQLException {
        return _stmt.getUpdateCount();
    }

    public SQLWarning getWarnings() throws SQLException {
        return _stmt.getWarnings();
    }

    public void setCursorName(String name) throws SQLException {
        _stmt.setCursorName(name);
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        _stmt.setEscapeProcessing(enable);
    }

    public void setFetchDirection(int direction) throws SQLException {
        _stmt.setFetchDirection(direction);
    }

    public void setFetchSize(int rows) throws SQLException {
        _stmt.setFetchSize(rows);
    }

    public void setMaxFieldSize(int max) throws SQLException {
        _stmt.setMaxFieldSize(max);
    }

    public void setMaxRows(int max) throws SQLException {
        _stmt.setMaxRows(max);
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        _stmt.setQueryTimeout(seconds);
    }

    private void printParams() {
        if (_paramsIdxr.size() > 0 || _paramsStr.size() > 0) {
            StringBuffer buf = new StringBuffer();
            buf.append("bound ");
            for (Map.Entry<Integer, Object> entry : _paramsIdxr.entrySet()) {
                try {
                    buf.append("(").append(entry.getKey()).append(",").append(entry.getValue()).append(") ");
                } catch (Throwable e) {
                    // We don't want to mess with the connection just for logging
                }
            }
            for (Map.Entry<String, Object> entry : _paramsStr.entrySet()) {
                try {
                    buf.append("(").append(entry.getKey()).append(",").append(entry.getValue()).append(") ");
                } catch (Throwable e) {
                    // We don't want to mess with the connection just for logging
                }
            }
            print(buf.toString());
        }
    }

    private boolean shouldPrint() {
        if (_log != null)
            return _log.isDebugEnabled();
        else return true;
    }

    private void print(String str) {
        if (_log != null)
            _log.debug(str);
        else System.out.println(str);
    }
}
