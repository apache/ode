package com.fs.pxe.ql.jcc;

import javax.xml.namespace.QName;

public class ASTProperty extends SimpleNode {
    protected QName name;

    public ASTProperty(int id) {
        super(id);
    }

    public void setName(String value) {
        name = QName.valueOf(value.substring(1));
    }

    /**
     * @return the name
     */
    public QName getName() {
        return name;
    }
   
}
