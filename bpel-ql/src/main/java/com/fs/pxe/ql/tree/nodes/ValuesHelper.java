package com.fs.pxe.ql.tree.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ValuesHelper {
    public static Collection extract(Collection values) {
        Collection<Object> result = new ArrayList<Object>(values.size());
        for(Iterator iter = values.iterator();iter.hasNext();) {
          Value value = (Value)iter.next();
          result.add(value.getValue());
        }
        return result;
    }
}
