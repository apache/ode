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

import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.apache.ode.utils.ISO8601DateParser;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.apache.ode.bpel.evar.ExternalVariableModule.Locator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Configuration for an external variable.
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 */
class DbExternalVariable {
    private static final Log __log = LogFactory.getLog(DbExternalVariable.class);

	private static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

	EVarId evarId;

	DataSource dataSource;

	final ArrayList<Column> _columns = new ArrayList<Column>();

	private final HashMap<String, Column> _colmap = new HashMap<String, Column>();

	final ArrayList<Column> _keycolumns = new ArrayList<Column>();

	final ArrayList<Column> _inscolumns = new ArrayList<Column>();

	final ArrayList<Column> _updcolumns = new ArrayList<Column>();

	InitType _initType = InitType.update_insert;

	public String[] _autoColNames;

	String select;

	String insert;

	String update;

	String table;

    String schema; // table schema

	/** Does the database support retrieval of generated keys? */
	boolean generatedKeys;

	DbExternalVariable(EVarId evar, DataSource ds) {
		this.evarId = evar;
		this.dataSource = ds;
	}

	Column getColumn(String key) {
		return _colmap.get(key);
	}

	void addColumn(Column c) {
		c.idx = _columns.size();
		_colmap.put(c.name, c);
		_columns.add(c);
		if (c.key) {
			_keycolumns.add(c);
			_autoColNames = new String[_keycolumns.size()];
			for (int i = 0; i < _autoColNames.length; ++i)
				_autoColNames[i] = _keycolumns.get(i).colname;
		}
		createSelect();
		createInsert();
		createUpdate();
	}

	public int numColumns() {
		return _columns.size();
	}

	/**
	 * Create a key from a locator.
	 */
	RowKey keyFromLocator(Locator locator) throws ExternalVariableModuleException {
		RowKey rc = new RowKey();
		parseXmlRow(rc, locator.reference);
		
        // Put in the static goodies such as pid/iid
		for (Column c : rc._columns) {
			switch (c.genType) {
			case iid:
			case pid:
                rc.put(c.name, c.getValue(c.name, null, null, locator.iid));
				break;
			}
		}
		
		return rc;
	}

	private void createSelect() {
		StringBuilder sb = new StringBuilder("select ");
		boolean first = true;
		for (Column c : _columns) {
			if (!first) {
				sb.append(',');
			}
			first = false;

			sb.append(c.colname);
		}
		sb.append(" from ");
		sb.append(schema + "." + table);
		if (_keycolumns.size() > 0) {
			sb.append(" where ");
			first = true;

			for (Column kc : _keycolumns) {
				if (!first) {
					sb.append(" and ");
				}
				first = false;

				sb.append(kc.colname);
				sb.append(" = ?");
			}
			select = sb.toString();

		} else {
			select = null;
		}
	}

	private void createUpdate() {
		_updcolumns.clear();
		StringBuilder sb = new StringBuilder("update ");
		sb.append(schema + "." + table);
		sb.append(" set ");
		boolean first = true;
		for (Column c : _columns) {
			// Don't ever update keys or sequences or create time stamps
            if (c.genType == GenType.sequence || c.key || c.genType == GenType.ctimestamp)
				continue;

			if (!first)
				sb.append(", ");
			first = false;

			sb.append(c.colname);
			sb.append(" = ");
			if (c.genType == GenType.expression)
				sb.append(c.expression);
			else {
				sb.append(" ?");
				_updcolumns.add(c);
			}
		}

		if (_keycolumns.size() > 0) {
			sb.append(" where ");
			first = true;

			for (Column kc : _keycolumns) {
				if (!first) {
					sb.append(" and ");
				}
				first = false;

				sb.append(kc.colname);
				sb.append(" = ?");
			}
		}

		// If we have no key columns, we cannot do an update
		if (_keycolumns.size() == 0)
			update = null;
		else
			update = sb.toString();
	}

	private void createInsert() {
		_inscolumns.clear();
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(schema + "." + table);
		sb.append(" ( ");
		boolean first = true;
		for (Column c : _columns) {
			if (c.genType == GenType.sequence)
				continue;

			if (!first)
				sb.append(',');

			first = false;
			sb.append(c.colname);
		}
		sb.append(" ) ");

		sb.append(" values ( ");

		first = true;
		for (Column c : _columns) {
			if (c.genType == GenType.sequence)
				continue;
			if (!first)
				sb.append(',');
			first = false;

			if (c.genType == GenType.expression)
				sb.append(c.expression);
			else {
				sb.append(" ? ");
				_inscolumns.add(c);
			}
		}
		sb.append(" ) ");

		insert = sb.toString();

	}

    <T extends RowSubset> Element renderXmlRow(Locator locator, QName varType, T value) throws ExternalVariableModuleException {
		Document doc = DOMUtils.newDocument();
        Element el = doc.createElementNS(varType.getNamespaceURI(), varType.getLocalPart());
		doc.appendChild(el);
        if (value != null) {
            for (Column c : value._columns) {
                Object data = value.get(c.idx);
                addElement(el, varType, c, data);
            }
        } else {
            // initialize variable with default/generated values
            RowKey keys = keyFromLocator(locator);
            for (Column c : _columns) {
                Object data = c.getValue(c.name, keys, new RowVal(), locator.iid);
                addElement(el, varType, c, data);
            }
        }
		return el;
	}

    private void addElement(Element parent, QName varType, Column c, Object data) {
        Document doc = parent.getOwnerDocument();
        Element cel = doc.createElementNS(varType.getNamespaceURI(), c.name);
        String strdat = c.toText(data);
        if (strdat != null) {
            cel.appendChild(doc.createTextNode(strdat));
        } else if (c.nullok || c.isGenerated()) { 
            cel.setAttributeNS(XSI_NS, "xsi:nil", "true");
        }
        parent.appendChild(cel);
    }

	<T extends RowSubset> T parseXmlRow(T ret, Node rowel)
			throws ExternalVariableModuleException {
		if (rowel == null)
			return ret;
		
		NodeList nl = rowel.getChildNodes();
        if (__log.isDebugEnabled()) __log.debug("parseXmlRow: element="+rowel.getLocalName());
		for (int i = 0; i < nl.getLength(); ++i) {
			Node n = nl.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE)
				continue;
			String key = n.getLocalName();
			String val = n.getTextContent();
            if (__log.isDebugEnabled()) __log.debug("Extvar key: "+key+" value: "+val);

			Column column = ret.getColumn(key);
            if (column == null) {
                if (__log.isDebugEnabled()) __log.debug("No matching column for key '"+key+"'");
				continue;
            }

			String nil = ((Element) n).getAttributeNS(XSI_NS, "nil");
            if (nil != null && "true".equalsIgnoreCase(nil) && (val == null || val.trim().length() == 0)) {
                if (__log.isDebugEnabled()) __log.debug("Extvar key: "+key+" is null (xsi:nil)");
				ret.put(key, null);
            } else {
				ret.put(key, column.fromText(val));
            }
		}
		return ret;
	}

	class Column {

		int idx;

		/** name of the column */
        final String name;

		/** database name of the column (in case we need to override */
        final String colname;

		/** Is this a key column? */
        final boolean key;

		/** Type of value generator to use for creating values for this column. */
        final GenType genType;

		/** The (SQL) expression used to populate the column. */
        final String expression;

		/** The SQL data type of this column, one of java.sql.Types */
		int dataType;

		/** Indicates NULL values are OK */
		boolean nullok;

        Column(String name, String colname, boolean key, GenType genType, String expression) {
			this.name = name;
			this.colname = colname == null ? name : colname;
			this.key = key;
			this.genType = genType;
			this.expression = expression;
		}

        public Object getValue(String name, RowKey keys, RowVal values, Long iid) {
			switch (genType) {
			case ctimestamp:
			case utimestamp:
				return isTimeStamp() ? new Timestamp(new Date().getTime())
						: new Date();
			case uuid:
				return new GUID().toString();
			case pid:
				return evarId.pid.toString();
			case iid:
				return iid;
			case none:
			default:
                if (key && keys.get(name) != null)
                    return keys.get(name);
                else
                    return values.get(name);
			}
		}

        boolean supportsEmptyValue() {
            return (dataType == Types.VARCHAR || dataType == Types.LONGVARCHAR || dataType == Types.CLOB); 
        }

		/**
		 * Return <code>true</code> if column is a date-like type.
		 */
		boolean isDate() {
			return dataType == Types.DATE;
		}

		boolean isTimeStamp() {
			return dataType == Types.TIMESTAMP;
		}

		boolean isTime() {
			return dataType == Types.TIME;
		}

		/**
		 * Is this column best represented as an integer?
		 */
		boolean isInteger() {
			switch (dataType) {
			case Types.BIGINT:
			case Types.INTEGER:
			case Types.SMALLINT:
			case Types.TINYINT:
				return true;
			default:
				return false;
			}
		}

		/**
		 * Is this column best represented as a real number?
		 */
		boolean isReal() {
			switch (dataType) {
			case Types.DECIMAL:
			case Types.REAL:
			case Types.NUMERIC:
				return true;
			default:
				return false;
			}

		}

		boolean isBoolean() {
			switch (dataType) {
			case Types.BIT:
				return true;
			default:
				return false;
			}
		}

		String toText(Object val) {
			if (val == null)
				return null;

            Date date = null;
            if (val instanceof java.util.Date) {
                // also applies to java.sql.Time, java.sql.Timestamp
                date = (Date) val;
				return ISO8601DateParser.format((Date) val);
            }
            return val.toString();
		}

		Object fromText(String val) throws ExternalVariableModuleException {
			try {
                if (val == null)
                    return null;
                
                if (!supportsEmptyValue() && val.trim().length() == 0) {
                    return null;
                }
                
				// TODO: use xsd:date and xsd:time conversions
				if (isDate())
                    return new java.sql.Date(ISO8601DateParser.parse(val).getTime());
				else if (isTime())
                    return new java.sql.Time(ISO8601DateParser.parse(val).getTime());
				else if (isTimeStamp())
                    return new java.sql.Timestamp(ISO8601DateParser.parse(val).getTime());
                else if (isInteger()) {
                    String v = val.trim().toLowerCase();
                    if (v.equals("true"))
                        return 1;
                    if (v.equals("false"))
                        return 0;
					return new java.math.BigDecimal(val).longValue();
                } else if (isReal())
					return Double.valueOf(val);
                else if (isBoolean()) {
                    String v = val.trim();
                    if (v.equals("1"))
                        return true;
                    if (v.equals("0"))
                        return false;
					return Boolean.valueOf(val);
                }

				return val;
			} catch (Exception ex) {
				throw new ExternalVariableModuleException(
						"Unable to convert value \"" + val + "\" for column \""
								+ name + "\" !", ex);
			}
		}

        public boolean isGenerated() {
            return (genType != null && !genType.equals(GenType.none));
        }
        
        public boolean isDatabaseGenerated() {
            return isGenerated() && (genType.equals(GenType.sequence) || genType.equals(GenType.expression));
        }
        
        public String toString() {
            return "Column {idx="+idx
                +",name="+name
                +",colname="+colname
                +",key="+key
                +",genType="+genType
                +")";
        }
	}

	/**
	 * Key used to identify a row.
	 */
	class RowKey extends RowSubset {
		private static final long serialVersionUID = 1L;

		/**
		 * Create empty row key.
		 */
		RowKey() {
			super(_keycolumns);
		}

		/**
		 * Write the key to a locator.
		 */
        void write(QName varType, Locator locator) throws ExternalVariableModuleException {
            locator.reference = renderXmlRow(locator, varType, this);
		}

		public Set<String> getMissing() {
			HashSet<String> missing = new HashSet<String>();
			for (Column c : _keycolumns) {
                if (get(c.idx) == null)
					missing.add(c.name);
			}
			return missing;
		}
	}

	/**
	 * Row values.
	 */
	class RowVal extends RowSubset {
		private static final long serialVersionUID = 1L;

		RowVal() {
			super(DbExternalVariable.this._columns);
		}
	}

}
