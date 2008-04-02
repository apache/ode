/**
 * 
 */
package org.apache.ode.bpel.extvar.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.ode.bpel.extvar.jdbc.DbExternalVariable.Column;;

class RowSubset extends ArrayList<Object> {
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
     * Return <code>true</code> if all entries are non-null.
     * @return
     */
    boolean isComplete() {
        for (Object o : this) 
            if (o == null)
                return false;
        
        return true;
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