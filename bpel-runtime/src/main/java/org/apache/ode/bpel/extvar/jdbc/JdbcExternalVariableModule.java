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
import org.apche.ode.bpel.evar.ExternalVariableModule;
import org.apche.ode.bpel.evar.ExternalVariableModuleException;
import org.w3c.dom.Element;

public class JdbcExternalVariableModule implements ExternalVariableModule {

    private static final Log __log = LogFactory.getLog(JdbcExternalVariableModule.class);

    /** Unique QName for the engine, this should be the element used for the external-variable configuration. */
    public static final QName NAME = new QName("http://www.apache.org/ode/extensions/externalVariables", "jdbc");

    /** Manually configured data sources. */
    private final HashMap<String, DataSource> _dataSources = new HashMap<String, DataSource>();

    /** Variables we know about via configure() method calls. */
    private final HashMap<EVarId, DbExternalVariable> _vars = new HashMap<EVarId, DbExternalVariable>();

    public void configure(QName pid, String extVarId, Element config) throws ExternalVariableModuleException {
        EVarId evarId = new EVarId(pid, extVarId);
        DataSource ds = null;

        Element jndiDs = DOMUtils.findChildByName(config, new QName(null, "datasource-jndi"));
        Element jndiRef = DOMUtils.findChildByName(config, new QName(null, "datasource-ref"));
        Element initMode = DOMUtils.findChildByName(config, new QName(null, "init-mode"));
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
                } catch (NamingException e) {
                    ;
                    ;
                }
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

        Connection conn;
        DatabaseMetaData metaData;
        try {
            conn = ds.getConnection();
            metaData = conn.getMetaData();
        } catch (Exception ex) {
            throw new ExternalVariableModuleException("Unable to open database connection for external variable " + evarId, ex);
        }

        try {
            DbExternalVariable dbev = new DbExternalVariable(evarId, ds);
            if (initMode != null)
                try {
                    dbev.initType = InitType.valueOf(initMode.getTextContent().trim());
                } catch (Exception ex) {
                    throw new ExternalVariableModuleException("Invalid <init-mode> value: " + initMode.getTextContent().trim());
                }

            Element tableName = DOMUtils.findChildByName(config, new QName(null, "table"));
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

            List<Element> columns = DOMUtils.findChildrenByName(config, new QName(null, "column"));

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
                if (cmd.next()) {
                    c.dataType = cmd.getInt("DATA_TYPE");
                    c.nullok = cmd.getInt("NULLABLE") != 0;
                } else
                    throw new ExternalVariableModuleException("External variable " + evarId + " referenced "
                            + "non-existant column \"" + colname + "\"!");

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

    public Value writeValue(Value newval) throws ExternalVariableModuleException {
        EVarId evarId = new EVarId(newval.locator.pid, newval.locator.varId);
        DbExternalVariable evar = _vars.get(evarId);
        if (evar == null)
            throw new ExternalVariableModuleException("No such variable. "); // todo

        RowVal val = evar.parseXmlRow((Element) newval.value);
        RowKey key = evar.generateKey(newval.locator, val);

        if (key != null && evar.initType == InitType.delete_insert) {
            // do delete...
            // TODO
        }

        // should we try an update first? to do this we need to have all the required keys
        // and there should be some keys
        boolean tryupdatefirst = (evar.initType == InitType.update || evar.initType == InitType.update_insert)
                && !evar.keycolumns.isEmpty() && key != null;

        boolean insert = evar.initType != InitType.update;

        try {
            if (tryupdatefirst)
                insert = execUpdate(evar, val) == 0;
            if (insert) {
                key = execInsert(evar, val);
                // Transfer the keys obtained from the db.
                key.write(newval.locator);
            }
        } catch (SQLException se) {
            throw new ExternalVariableModuleException("Error updating row.", se);
        }

        return newval;

    }

    public Value readValue(Locator locator) throws ExternalVariableModuleException {
        EVarId evarId = new EVarId(locator.pid, locator.varId);
        DbExternalVariable evar = _vars.get(evarId);
        if (evar == null)
            throw new ExternalVariableModuleException("No such variable. "); // todo
        
        Element val;
        try {
            RowVal rowval = execSelect(evar, locator);
            if (rowval == null)
                return null;
            val = evar.renderXmlRow(rowval);
        } catch (SQLException se) {
            throw new ExternalVariableModuleException("SQL Error.", se);
        }

        return new Value(locator, val, null);

    }

    /**
     * Manually register a data source. Handy if you don't want to use JDBC to look these up.
     * 
     * @param dsName
     * @param ds
     */
    public void registerDataSource(String dsName, DataSource ds) {
        _dataSources.put(dsName, ds);
    }

    int execUpdate(DbExternalVariable dbev, RowVal values) throws SQLException {
        Connection conn = dbev.dataSource.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(dbev.update);
            int idx = 1;
            for (Column c : dbev.updcolumns) {
                Object val = values.get(c.name);
                stmt.setObject(idx, val);
                idx++;
            }

            for (Column ck : dbev.keycolumns) {
                Object val = values.get(ck.name);
                stmt.setObject(idx, val);
                idx++;
            }

            return stmt.executeUpdate();

        } finally {
            conn.close();
        }

    }

    RowVal execSelect(DbExternalVariable dbev, Locator locator) throws SQLException, ExternalVariableModuleException {
        RowKey rowkey = dbev.generateKey(locator, null);
        RowVal ret = dbev.new RowVal();
        Connection conn = dbev.dataSource.getConnection();
        try {
            PreparedStatement stmt = conn.prepareStatement(dbev.select);
            int idx = 1;
            for (Object k : rowkey)
                stmt.setObject(idx++, k);

            ResultSet rs = stmt.executeQuery();
            try {
                if (rs.next()) {
                    for (Column cr : dbev.columns) 
                        ret.set(cr.idx,rs.getObject(cr.idx+1));

                } else
                    return null;
            } finally {
                rs.close();
            }
        } finally {
            conn.close();
        }

        return ret;
    }

    RowKey execInsert(DbExternalVariable dbev, RowVal values) throws SQLException {
        RowKey keys = dbev.new RowKey();
        Connection conn = dbev.dataSource.getConnection();
        try {
            PreparedStatement stmt = dbev.generatedKeys ? conn.prepareStatement(dbev.insert, dbev.autoColNames) : conn
                    .prepareStatement(dbev.insert);
            int idx = 1;
            for (Column c : dbev.inscolumns) {
                Object val = c.getValue(c.name, values, null);
                values.put(c.name, val);
                stmt.setObject(idx, val);
                idx++;
            }

            stmt.execute();

            if (dbev.generatedKeys) {
                // With JDBC 3, we can get the values of the key columns (if the db supports it)
                ResultSet keyRS = stmt.getResultSet();
                keyRS.next();
                for (Column ck : dbev.keycolumns)
                    keys.add(keyRS.getObject(ck.colname));
            } else {
                for (Column ck : dbev.keycolumns) {
                    Object val = values.get(ck.name);
                    if (val != null)
                        keys.add(val);
                }
            }

            return keys;

        } finally {
            conn.close();
        }

    }

}
