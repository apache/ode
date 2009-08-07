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
package org.apache.ode.bpel.extvar.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.extvar.jdbc.DbExternalVariable.Column;
import org.apache.ode.bpel.extvar.jdbc.DbExternalVariable.RowKey;
import org.apache.ode.bpel.extvar.jdbc.DbExternalVariable.RowVal;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.ObjectPrinter;
import org.apache.ode.bpel.evar.ExternalVariableModule;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.apache.ode.bpel.evar.IncompleteKeyException;
import org.w3c.dom.Element;

public class JdbcExternalVariableModule implements ExternalVariableModule {

    private static final Log __log = LogFactory.getLog(JdbcExternalVariableModule.class);

    public static final String JDBC_NS = "http://ode.apache.org/externalVariables/jdbc";
    
    /** Unique QName for the engine, this should be the element used for the external-variable configuration. */
    public static final QName NAME = new QName(JDBC_NS, "jdbc");

    /** Manually configured data sources. */
    private final HashMap<String, DataSource> _dataSources = new HashMap<String, DataSource>();

    /** Variables we know about via configure() method calls. */
    private final HashMap<EVarId, DbExternalVariable> _vars = new HashMap<EVarId, DbExternalVariable>();

    public void configure(QName pid, String extVarId, Element config) throws ExternalVariableModuleException {
        EVarId evarId = new EVarId(pid, extVarId);
        DataSource ds = null;

        Element jndiDs = DOMUtils.findChildByName(config, new QName(JDBC_NS, "datasource-jndi"));
        Element jndiRef = DOMUtils.findChildByName(config, new QName(JDBC_NS, "datasource-ref"));
        Element initMode = DOMUtils.findChildByName(config, new QName(JDBC_NS, "init-mode"));
        if (jndiRef != null) {
            String refname = jndiRef.getTextContent().trim();
            ds = _dataSources.get(refname);
            if (ds == null)
                throw new ExternalVariableModuleException("Data source reference \"" + refname
                        + "\" not found for external variable " + evarId
                        + "; make sure to register the data source with the engine!");
        } else if (jndiDs != null) {
            String name = jndiDs.getTextContent().trim();
            Object dsCandidate;
            InitialContext ctx;
            try {
                ctx = new InitialContext();
            } catch (Exception ex) {
                throw new ExternalVariableModuleException("Unable to access JNDI context for external variable " + evarId, ex);
            }

            try {
                dsCandidate = ctx.lookup(name);
            } catch (Exception ex) {
                throw new ExternalVariableModuleException("Lookup of data source for " + evarId + "  failed.", ex);
            } finally {
                try {
                    ctx.close();
                } catch (NamingException e) { /* ignore */ } 
            }

            if (dsCandidate == null)
                throw new ExternalVariableModuleException("Data source \"" + name + "\" not found in JNDI!");

            if (!(dsCandidate instanceof DataSource))
                throw new ExternalVariableModuleException("JNDI object \"" + name + "\" does not implement javax.sql.DataSource");

            ds = (DataSource) dsCandidate;
        }

        if (ds == null) {
            throw new ExternalVariableModuleException("No valid data source configuration for JDBC external varible " + evarId);
        }

        Connection conn = null;
        DatabaseMetaData metaData;
        try {
            conn = ds.getConnection();
            metaData = conn.getMetaData();
        } catch (Exception ex) {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                // ignore
            }
            throw new ExternalVariableModuleException("Unable to open database connection for external variable " + evarId, ex);
        }

        try {
            DbExternalVariable dbev = new DbExternalVariable(evarId, ds);
            if (initMode != null)
                try {
                    dbev._initType = InitType.valueOf(initMode.getTextContent().trim());
                } catch (Exception ex) {
                    throw new ExternalVariableModuleException("Invalid <init-mode> value: " + initMode.getTextContent().trim());
                }

            Element tableName = DOMUtils.findChildByName(config, new QName(JDBC_NS, "table"));
            if (tableName == null || tableName.getTextContent().trim().equals(""))
                throw new ExternalVariableModuleException("Must specify <table> for external variable " + evarId);
            String table = tableName.getTextContent().trim();
            String schema = null;
            if (table.indexOf('.') != -1) {
                schema = table.substring(0, table.indexOf('.'));
                table = table.substring(table.indexOf('.') + 1);
            }

            if (metaData.storesLowerCaseIdentifiers()) {
                table = table.toLowerCase();
                if (schema != null)
                    schema = table.toLowerCase();
            } else if (metaData.storesUpperCaseIdentifiers()) {
                table = table.toUpperCase();
                if (schema != null)
                    schema = schema.toUpperCase();
            }

            dbev.generatedKeys = metaData.supportsGetGeneratedKeys();
            ResultSet tables = metaData.getTables(null, schema, table, null);
            if (tables.next()) {
                dbev.table = tables.getString("TABLE_NAME");
                dbev.schema = tables.getString("TABLE_SCHEM");
            } else
                throw new ExternalVariableModuleException("Table \"" + table + "\" not found in database.");

            tables.close();

            List<Element> columns = DOMUtils.findChildrenByName(config, new QName(JDBC_NS, "column"));

            for (Element col : columns) {
                String name = col.getAttribute("name");
                String colname = col.getAttribute("column-name");
                String key = col.getAttribute("key");
                String gentype = col.getAttribute("generator");
                String expression = col.getAttribute("expression");

                if (key == null || "".equals(key))
                    key = "no";
                if (gentype == null || "".equals(gentype))
                    gentype = GenType.none.toString();
                if (colname == null || "".equals(colname))
                    colname = name;

                if (name == null || "".equals(name))
                    throw new ExternalVariableModuleException("External variable " + evarId
                            + " <column> element must have \"name\" attribute. ");

                if (metaData.storesLowerCaseIdentifiers())
                    colname = colname.toLowerCase();
                else if (metaData.storesUpperCaseIdentifiers())
                    colname = colname.toUpperCase();

                GenType gtype;
                try {
                    gtype = GenType.valueOf(gentype);
                } catch (Exception ex) {
                    throw new ExternalVariableModuleException("External variable " + evarId + " column \"" + name
                            + "\" generator type \"" + gentype + "\" is unknown.");

                }

                if (gtype == GenType.expression && (expression == null || "".equals(expression)))
                    throw new ExternalVariableModuleException("External variable " + evarId + " column \"" + name
                            + "\" used \"expression\" generator, but did not specify an expression");

                Column c = dbev.new Column(name, colname, key.equalsIgnoreCase("yes"), gtype, expression);
                ResultSet cmd = metaData.getColumns(null, dbev.schema, dbev.table, colname);
                try {
                if (cmd.next()) {
                    c.dataType = cmd.getInt("DATA_TYPE");
                    c.nullok = cmd.getInt("NULLABLE") != 0;
                } else
                    throw new ExternalVariableModuleException("External variable " + evarId + " referenced "
                            + "non-existant column \"" + colname + "\"!");
                } finally {
                    cmd.close();
                }

                dbev.addColumn(c);
            }

            if (dbev.numColumns() == 0)
                throw new ExternalVariableModuleException("External variable " + evarId + " did not have any <column> elements!");

            _vars.put(evarId, dbev);
        } catch (SQLException se) {
            throw new ExternalVariableModuleException("SQL Error", se);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

    public QName getName() {
        return NAME;
    }

    public boolean isTransactional() {
        return true;
    }

    public void shutdown() {
    }

    public void start() {
    }

    public void stop() {
    }

    public Value writeValue(QName varType, Value newval) throws ExternalVariableModuleException {
        EVarId evarId = new EVarId(newval.locator.pid, newval.locator.varId);
        DbExternalVariable evar = _vars.get(evarId);
        if (evar == null)
            throw new ExternalVariableModuleException("No such variable. "); // todo

        RowKey key = evar.keyFromLocator(newval.locator);
        RowVal val = evar.parseXmlRow(evar.new RowVal(), (Element) newval.value);
        if (__log.isDebugEnabled())
            __log.debug("JdbcExternalVariable.writeValue() RowKey: " + key + " RowVal: " + val);

        if (!key.missingValues() && evar._initType == InitType.delete_insert) {
            // do delete...
            throw new ExternalVariableModuleException("Delete not implemented. "); // todo
        }

        // should we try an update first? to do this we need to have all the required keys
        // and there should be some keys
        boolean tryupdatefirst = (evar._initType == InitType.update || evar._initType == InitType.update_insert)
                && !evar._keycolumns.isEmpty() && !key.missingDatabaseGeneratedValues();

        boolean insert = evar._initType != InitType.update;

        if (__log.isDebugEnabled())
            __log.debug("tryUpdateFirst: " + tryupdatefirst
                        + " insert: " + insert
                        + " initType: " + evar._initType
                        + " key.isEmpty: " + evar._keycolumns.isEmpty()
                        + " key.missingValues: " + key.missingValues()
                        + " key.missingDBValues: " + key.missingDatabaseGeneratedValues());
        
        try {
            if (tryupdatefirst)
                insert = execUpdate(evar, key, val) == 0;
            if (insert) {
                key = execInsert(evar, newval.locator, key, val);
                // Transfer the keys obtained from the db.
                key.write(varType, newval.locator);
            }
        } catch (SQLException se) {
            throw new ExternalVariableModuleException("Error updating row.", se);
        }

        return newval;
    }

    public Value readValue(QName varType, Locator locator) throws ExternalVariableModuleException {
        EVarId evarId = new EVarId(locator.pid, locator.varId);
        DbExternalVariable evar = _vars.get(evarId);
        if (evar == null)
            throw new ExternalVariableModuleException("No such variable: "+evarId);
        
        Element val;
        try {
            RowVal rowval = execSelect(evar, locator);
            val = evar.renderXmlRow(locator, varType, rowval);
        } catch (SQLException se) {
            throw new ExternalVariableModuleException("SQL Error.", se);
        }

        return new Value(locator, val, null);
    }

    /**
     * Manually register a data source. Handy if you don't want to use JNDI to look these up.
     * 
     * @param dsName
     * @param ds
     */
    public void registerDataSource(String dsName, DataSource ds) {
        _dataSources.put(dsName, ds);
    }

    int execUpdate(DbExternalVariable dbev, RowKey key, RowVal values) throws SQLException {
        Connection conn = dbev.dataSource.getConnection();
        PreparedStatement stmt = null;
        try {
            if (__log.isDebugEnabled()) {
                __log.debug("execUpdate: key=" + key + " values=" + values);
                __log.debug("Prepare statement: " + dbev.update);
            }
            stmt = conn.prepareStatement(dbev.update);
            int idx = 1;
            for (Column c : dbev._updcolumns) {
                Object val = values.get(c.name);
                if (__log.isDebugEnabled()) __log.debug("Set value parameter "+idx+": "+val);
                if (val == null)
                    stmt.setNull(idx, c.dataType);
                else
                    stmt.setObject(idx, val);
                idx++;
            }

            for (Column ck : dbev._keycolumns) {
                Object val = key.get(ck.name);
                if (__log.isDebugEnabled()) __log.debug("Set key parameter "+idx+": "+val);
                if (val == null)
                    stmt.setNull(idx, ck.dataType);
                else
                    stmt.setObject(idx, val);
                idx++;
            }
            return stmt.executeUpdate();
        } finally {
            if (stmt != null) stmt.close();
            try {
                conn.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    RowVal execSelect(DbExternalVariable dbev, Locator locator) throws SQLException, ExternalVariableModuleException {
        RowKey rowkey = dbev.keyFromLocator(locator);
        if (__log.isDebugEnabled()) __log.debug("execSelect: " + rowkey);
        
        if (rowkey.missingDatabaseGeneratedValues()) {
            return null;
        }
        
        if (rowkey.missingValues()) {
            throw new IncompleteKeyException(rowkey.getMissing());
        }
        
        RowVal ret = dbev.new RowVal();
        Connection conn = dbev.dataSource.getConnection();
        PreparedStatement stmt = null;
        try {
            if (__log.isDebugEnabled()) __log.debug("Prepare statement: " + dbev.select);
            stmt = conn.prepareStatement(dbev.select);
            int idx = 1;
            for (Object k : rowkey) {
                if (__log.isDebugEnabled()) __log.debug("Set key parameter "+idx+": "+k);
                stmt.setObject(idx++, k);
            }

            ResultSet rs = stmt.executeQuery();
            try {
                if (rs.next()) {
                    for (Column c : dbev._columns)  {
                        Object val;
                        int i = c.idx+1;
                        if (c.isDate()) val = rs.getDate(i);
                        else if (c.isTimeStamp()) val = rs.getTimestamp(i);
                        else if (c.isTime()) val = rs.getTime(i);
                        else if (c.isInteger()) val = new Long(rs.getLong(i));
                        else if (c.isReal()) val = new Double(rs.getDouble(i));
                        else if (c.isBoolean()) val = new Boolean(rs.getBoolean(i));
                        else val = rs.getObject(i);
                        if (__log.isDebugEnabled()) __log.debug("Result column index "+c.idx+": "+val);
                        ret.set(c.idx,val);
                    }
                } else
                    return null;
            } finally {
                rs.close();
            }
        } finally {
            if (stmt != null) stmt.close();
            try {
                conn.close();
            } catch (SQLException e) {
                // ignore
            }
        }

        return ret;
    }

    RowKey execInsert(DbExternalVariable dbev, Locator locator, RowKey keys, RowVal values) throws SQLException {
        Connection conn = dbev.dataSource.getConnection();
        PreparedStatement stmt = null; 
        try {
            if (__log.isDebugEnabled()) {
                __log.debug("execInsert: keys=" + keys + " values=" + values);
                __log.debug("Prepare statement: " + dbev.insert);
                __log.debug("missingDatabaseGeneratedValues: " + keys.missingDatabaseGeneratedValues());
                __log.debug("_autoColNames: " + ObjectPrinter.stringifyNvList(dbev._autoColNames));
            }

            stmt = keys.missingDatabaseGeneratedValues() 
                ? conn.prepareStatement(dbev.insert, dbev._autoColNames) 
                : conn.prepareStatement(dbev.insert);

            int idx = 1;
            for (Column c : dbev._inscolumns) {
                Object val = c.getValue(c.name, keys, values, locator.iid);
                values.put(c.name, val);
                if (__log.isDebugEnabled()) __log.debug("Set parameter "+idx+": "+val);
                if (val == null)
                    stmt.setNull(idx, c.dataType);
                else 
                    stmt.setObject(idx, val);
                idx++;
            }

            stmt.execute();

            for (Column ck : keys._columns) {
                Object val = values.get(ck.name);
                if (__log.isDebugEnabled()) __log.debug("Key "+ck.name+": "+val);
                keys.put(ck.name,val);
            }

            if (keys.missingDatabaseGeneratedValues() ) {
                // With JDBC 3, we can get the values of the key columns (if the db supports it)
                ResultSet keyRS = stmt.getGeneratedKeys();
                try {
                if (keyRS == null) 
                    throw new SQLException("Database did not return generated keys");
                keyRS.next();
                for (Column ck : keys._columns) {
                    Object value = keyRS.getObject(ck.idx+1);
                    if (__log.isDebugEnabled()) __log.debug("Generated key "+ck.name+": "+value);
                    keys.put(ck.name, value);
                }
                } finally {
                    keyRS.close();
                }
            } 
            return keys;
        } finally {
            if (stmt != null) stmt.close();
            try {
                conn.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

}
