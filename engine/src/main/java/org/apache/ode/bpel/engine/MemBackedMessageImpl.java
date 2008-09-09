package org.apache.ode.bpel.engine;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * Message, stored in memory.
 * 
 * @author Maciej Szefler
 * 
 */
public class MemBackedMessageImpl extends MessageImpl {

    private Element _msg;
    private Element _header;

    private QName _type;

    public MemBackedMessageImpl(Element header, Element msg, QName type, boolean ro) {
        _msg = msg;
        _header = header;
        _type = type;
        if (ro) makeReadOnly();
    }

    @Override
    public Element getMessage() {
        return _msg;
    }

    @Override
    public void setMessage(Element msg) {
        checkWrite();
        _msg = msg;
    }

    @Override
    public QName getType() {
        return _type;
    }

    @Override
    public Element getHeader() {
        return _header;
    }
    
    @Override
    public void setHeader(Element header) {
        checkWrite();
        _header = header;
    }

}
