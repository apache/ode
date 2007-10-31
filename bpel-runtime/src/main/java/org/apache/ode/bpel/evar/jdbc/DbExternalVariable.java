package org.apache.ode.bpel.evar.jdbc;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.sql.DataSource;
import javax.xml.namespace.QName;

import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.apache.ode.utils.ISO8601DateParser;
import org.apche.ode.bpel.evar.ExternalVariableModuleException;
import org.apche.ode.bpel.evar.ExternalVariableModule.Locator;
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

    private static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

    EVarId evarId;

    DataSource dataSource;

    final ArrayList<Column> columns = new ArrayList<Column>();

    private final HashMap<String, Column> colmap = new HashMap<String, Column>();

    final ArrayList<Column> keycolumns = new ArrayList<Column>();

    final ArrayList<Column> inscolumns = new ArrayList<Column>();

    final ArrayList<Column> updcolumns = new ArrayList<Column>();

    InitType initType = InitType.update_insert;

    public String[] autoColNames;

    String select;

    String insert;

    String update;

    String table;

    String schema;

    /** Does the database support retrieval of generated keys? */
    boolean generatedKeys;

    QName rowname = new QName(null, "row");

    DbExternalVariable(EVarId evar, DataSource ds) {
        this.evarId = evar;
        this.dataSource = ds;
    }

    Column getColumn(String key) {
        return colmap.get(key);
    }

    void addColumn(Column c) {
        c.idx = columns.size(); 
        colmap.put(c.name, c);
        columns.add(c);
        if (c.key) {
            keycolumns.add(c);
            autoColNames = new String[keycolumns.size()];
            for (int i = 0; i < autoColNames.length; ++i)
                autoColNames[i] = keycolumns.get(i).colname;
        }
        createSelect();
        createInsert();
        createUpdate();
    }

    public int numColumns() {
        return columns.size();
    }

    /**
     * Create a key from a locator.
     * 
     * @param locator
     * @return
     */
    RowKey generateKey(Locator locator) throws ExternalVariableModuleException {
        RowKey rc = new RowKey();
        for (Column kc : keycolumns) {
            String s = locator.get(kc.name);
            if (s == null) /* incomplete key */
                return null;
            rc.add(kc.fromText(s));

        }

        return rc;
    }

    private void createSelect() {
        StringBuilder sb = new StringBuilder("select ");
        boolean first = true;
        for (Column c : columns) {
            if (!first) {
                sb.append(',');
            }
            first = false;

            sb.append(c.colname);
        }
        sb.append(" from " + table);
        if (keycolumns.size() > 0) {
            sb.append(" where ");
            first = true;

            for (Column kc : keycolumns) {
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
        updcolumns.clear();
        StringBuilder sb = new StringBuilder("update ");
        sb.append(table);
        sb.append(" set ");
        boolean first = true;
        for (Column c : columns) {
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
                updcolumns.add(c);
            }
        }

        if (keycolumns.size() > 0) {
            sb.append(" where ");
            first = true;

            for (Column kc : keycolumns) {
                if (!first) {
                    sb.append(" and ");
                }
                first = false;

                sb.append(kc.colname);
                sb.append(" = ?");
            }

        }

        // If we have no key columns, we cannot do an update
        if (keycolumns.size() == 0)
            update = null;
        else
            update = sb.toString();

    }

    private void createInsert() {
        inscolumns.clear();
        StringBuilder sb = new StringBuilder("insert into ");
        sb.append(table);
        sb.append(" ( ");
        boolean first = true;
        for (Column c : columns) {
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
        for (Column c : columns) {
            if (c.genType == GenType.sequence)
                continue;
            if (!first)
                sb.append(',');
            first = false;

            if (c.genType == GenType.expression)
                sb.append(c.expression);
            else {
                sb.append(" ? ");
                inscolumns.add(c);
            }
        }
        sb.append(" ) ");

        insert = sb.toString();

    }

    class Column {
        
        int idx;
        
        /** name of the column */
        String name;

        /** database name of the column (in case we need to override */
        String colname;

        /** Is this a key column? */
        boolean key;

        /** Type of value generator to use for creating values for this column. */
        GenType genType;

        /** The (SQL) expression used to populate the column. */
        String expression;

        /** The SQL data type of this column, one of java.sql.Types */
        int dataType;

        /** Indicates NULL values are OK */
        boolean nullok;
        
        QName elname;


        Column(String name, String colname, boolean key, GenType genType, String expression) {
            this.name = name;
            this.colname = colname == null ? name : colname;
            this.key = key;
            this.genType = genType;
            this.expression = expression;
            elname = new QName(null, name);
        }

        public Object getValue(String name, RowVal values, Long iid) {
            switch (genType) {
            case ctimestamp:
            case utimestamp:
                return isTimeStamp() ? new Timestamp(new Date().getTime()) : new Date();
            case uuid:
                return new GUID().toString();
            case pid:
                return evarId.pid.toString();
            case iid:
                return iid;
            case none:
            default:
                return values.get(name);
            }
        }

        /**
         * Return <code>true</code> if column is a date-like type.
         * 
         * @return
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
         * 
         * @return
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
         * 
         * @return
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
            
            if (isDate())
                return ISO8601DateParser.format((Date)val);
            else if (isTime())
                return ISO8601DateParser.format((Date)val);
            else if (isTimeStamp())
                return ISO8601DateParser.format((Date)val);
            else
                return val.toString();
        }

        Object fromText(String val) throws ExternalVariableModuleException {
            try {
                // TODO: use xsd:date and xsd:time conversions
                if (isDate())
                    return new java.sql.Date(ISO8601DateParser.parse(val).getTime());
                else if (isTime())
                    return new java.sql.Time(ISO8601DateParser.parse(val).getTime());
                else if (isTimeStamp())
                    return new java.sql.Timestamp(ISO8601DateParser.parse(val).getTime());
                else if (isInteger())
                    return Long.valueOf(val);
                else if (isReal())
                    return Double.valueOf(val);
                else if (isBoolean())
                    return Boolean.valueOf(val);

                return val;
            } catch (Exception ex) {
                throw new ExternalVariableModuleException("Unable to convert value \"" + val + "\" for column \"" + name + "\" !",
                        ex);
            }
        }
    }

    /**
     * 
     * Key used to identify a row.
     * 
     * @author Maciej Szefler <mszefler at gmail dot com>
     * 
     */
    class RowKey extends ArrayList<Object> {

        /**
         * Create empty row key.
         */
        RowKey() {
        }

        /**
         * Write the key to a locator.
         * 
         * @param locator
         */
        void write(Locator locator) {
            locator.clear();
            int idx = 0;
            for (Column kc : keycolumns)
                locator.put(kc.name, kc.toText(get(idx++)));
        }

    }

    /**
     * Row values.
     * 
     * @author Maciej Szefler <mszefler at gmail dot com>
     * 
     */
    class RowVal extends ArrayList<Object> {
        RowVal() {
            super(columns.size());
            for (int i = 0; i < columns.size(); ++i)
                add(null);
        }

        Object get(String name) {
            Column c = colmap.get(name);
            if (c == null)
                return null;
            int idx = columns.indexOf(c);
            return get(idx);
        }

        public void put(String name, Object val) {
            Column c = colmap.get(name);
            if (c == null)
                return;

            int idx = columns.indexOf(c);
            this.set(idx, val);
        }
    }

    Element renderXmlRow(RowVal value) {
        Document doc = DOMUtils.newDocument();
        Element el = doc.createElementNS(rowname.getNamespaceURI(), rowname.getLocalPart());
        doc.appendChild(el);
        for (Column c : columns) {
            Object data = value.get(c.idx);
            Element cel = doc.createElementNS(c.elname.getNamespaceURI(), c.elname.getLocalPart());
            String strdat = c.toText(data);
            if (strdat != null)
                cel.appendChild(doc.createTextNode(strdat));
            else
                cel.setAttributeNS(XSI_NS, "xsi:nil", "true");
                    
            el.appendChild(cel);
        }

        return el;
    }

    RowVal parseXmlRow( Element rowel) throws ExternalVariableModuleException {
        RowVal ret = new RowVal();
        NodeList nl = rowel.getChildNodes();
        for (int i = 0; i < nl.getLength(); ++i) {
            Node n = nl.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE)
                continue;
            String key = n.getLocalName();
            String val = n.getTextContent();

            Column column = getColumn(key);
            if (column == null)
                continue;

            String nil = ((Element)n).getAttributeNS(XSI_NS, "nil");
            if (nil != null && "true".equalsIgnoreCase(nil))
                ret.put(key,null);
            else
                ret.put(key, column.fromText(val));
        }
        return ret;
    }

}