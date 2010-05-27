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

/**
 *
 */
package org.apache.ode.bpel.extvar.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.extvar.jdbc.DbExternalVariable.Column;;

class RowSubset extends ArrayList<Object> {
    private static final Log LOG = LogFactory.getLog(RowSubset.class);

    private static final long serialVersionUID = 1L;

    protected List<Column> _columns;

    /* Name --> Column mapping. */
    protected HashMap<String, Column> _colmap  = new HashMap<String,Column>();


    RowSubset(List<Column> columns) {
        _columns = columns;

        for(Column c : columns) {
            add(null);
            _colmap.put(c.name,c);
        }
    }

    /**
     * Return <code>true</code> if any values are missing (e.g. null value)
     */
    boolean missingValues() {
        for (Column c : _columns) {
            if (get(c.idx) == null) return true;
        }
        return false;
    }

    /**
     * Return <code>true</code> if any database-generated values are missing
     */
    boolean missingDatabaseGeneratedValues() {
        for (Column c : _columns) {
            Object value = get(c.idx);
            if (c.isDatabaseGenerated() && value == null) return true;
        }
        return false;
    }


    Column getColumn(String name) {
        return _colmap.get(name);
    }

    Column getColumn(int idx) {
        return _columns.get(idx);
    }

    Object get(String name) {
        Column c = _colmap.get(name);
        if (c == null)
            return null;
        int idx = _columns.indexOf(c);
        return get(idx);
    }

    void put(String name, Object val) {
        Column c = _colmap.get(name);
        if (c == null)
            return;

        int idx = _columns.indexOf(c);
        this.set(idx, val);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(getClass().getSimpleName());
        buf.append("(");
        for (int i=0; i<size(); i++) {
            if (i>0) buf.append(", ");
            buf.append(_columns.get(i).name);
            buf.append("=");
            buf.append(get(i));
        }
        buf.append(")");
        return buf.toString();
    }
}