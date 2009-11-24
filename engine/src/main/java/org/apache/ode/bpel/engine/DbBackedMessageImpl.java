package org.apache.ode.bpel.engine;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.dao.MessageDAO;
import org.w3c.dom.Element;

public class DbBackedMessageImpl extends MessageImpl {

    private MessageDAO _dao;
    
    public DbBackedMessageImpl(MessageDAO dao) {
        _dao = dao;
    }

    @Override
    public Element getMessage() {
        return _dao.getData();
    }

    @Override
    public QName getType() {
        return _dao.getType();
    }

    @Override
    public void setMessage(Element msg) {
        _dao.setData(msg);
    }

    @Override
    public Element getHeader() {
        return _dao.getHeader();
    }

    @Override
    public void setHeader(Element msg) {
        _dao.setHeader(msg);
    }
}
