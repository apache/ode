/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.memdao;

import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.XmlDataDAO;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.util.Properties;
import java.io.IOException;


/**
 * A very simple, in-memory implementation of the {@link XmlDataDAO} interface.
 */
class XmlDataDaoImpl extends DaoBaseImpl implements XmlDataDAO {

    private Node _data;
    private Properties _properties = new Properties();
    private ScopeDaoImpl _scope;
    private String _name;

    XmlDataDaoImpl(ScopeDaoImpl scope,String varname){
        _scope = scope;
        _name = varname;
    }

    /**
     * @see XmlDataDAO#isNull()
     */
    public boolean isNull() {
        return _data == null;
    }

    /**
     * @see XmlDataDAO#get()
     */
    public Node get() {
        if (_data == null || !(_data instanceof Element)) return _data;
        
        Document doc = DOMUtils.newDocument();
        Node copy = doc.importNode(_data, true);
        doc.appendChild(copy);
        return copy;
    }

    /**
     * @see XmlDataDAO#remove()
     */
    public void remove() {
        _data = null;
    }

    /**
     * @see XmlDataDAO#set(org.w3c.dom.Node)
     */
    public void set(Node val) {
        if (val == null || !(val instanceof Element)) {
            _data = val;
            return;
        }
        // For some reason we're getting some weird DOM trees from ServiceMix. Until we
        // spot where it exactly comes from, this fixes it.
        // The weirdness lies in elements being in xmlns="" when printed with a DOMWriter
        // but having a ns when the elements are queried directly.
        try {
            _data = DOMUtils.stringToDOM(DOMUtils.domToString(val));
        } catch (SAXException e) {
            // Should never happen but life is full of surprises
            throw new RuntimeException("Couldn't reread!", e);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't reread!", e);
        }
    }

    /**
     * @see org.apache.ode.bpel.dao.XmlDataDAO#getProperty(java.lang.String)
     */
    public String getProperty(String propertyName) {
        return _properties.getProperty(propertyName);
    }

    /**
     * @see org.apache.ode.bpel.dao.XmlDataDAO#setProperty(java.lang.String, java.lang.String)
     */
    public void setProperty(String pname, String pvalue) {
        _properties.setProperty(pname, pvalue);
    }

    public Properties getProperties() {
        return _properties;
    }

    /**
     * @see org.apache.ode.bpel.dao.XmlDataDAO#getScopeDAO()
     */
    public ScopeDAO getScopeDAO() {
        return _scope;
    }

    public String getName() {
        return _name;
    }

}
