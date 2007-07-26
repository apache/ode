package org.apache.ode.bpel.engine;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * Message, stored in memory.
 * 
 * @author Maciej Szefler
 * 
 */
class MemBackedMessageImpl extends MessageImpl {

    private Element _msg;

    private QName _type;

    MemBackedMessageImpl(Element msg, QName type, boolean ro) {
        _msg = msg;
        _type = type;
        if (ro)
            makeReadOnly();
    }

    @Override
    public Element getMessage() {
        return _msg;
    }

    @Override
    public QName getType() {
        return _type;
    }

    @Override
    public void setMessage(Element msg) {
        checkWrite();
        _msg = msg;
    }

}
