package org.apache.ode.store.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.store.hobj.HProcessConf;
import org.apache.ode.store.hobj.HProcessProperty;
import org.apache.ode.utils.DOMUtils;
import org.hibernate.SessionFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mriou <mriou at apache dot org>
 */
public class ProcessConfDAOHib implements ProcessConfDAO {

    private static final Log __log = LogFactory.getLog(ProcessConfDAOHib.class);

    private HProcessConf _process;
    private SessionFactory _sf;

    public ProcessConfDAOHib(SessionFactory sf, HProcessConf process) {
        _process = process;
        _sf = sf;
    }

    public Date getDeployDate() {
        return _process.getDeployDate();
    }

    public String getDeployer() {
        return _process.getDeployer();
    }

    public QName getProcessId() {
        return QName.valueOf(_process.getProcessId());
    }

    public String getTypeName() {
        return _process.getTypeName();
    }

    public String getTypeNamespace() {
        return _process.getTypeNamespace();
    }

    public int getVersion() {
        return _process.getVersion();
    }

    public boolean isActive() {
        return _process.isActive();
    }

    public void setActive(boolean active) {
        _process.setActive(active);
    }

    public void setProperty(String name, String ns, Node content) {
        setProperty(name, ns, DOMUtils.domToStringLevel2(content), false);
    }

    public void setProperty(String name, String ns, String content) {
        setProperty(name, ns, content, true);
    }

    private void setProperty(String name, String ns, String content, boolean simple) {
        HProcessProperty existingProperty = getProperty(name, ns);
        if (existingProperty == null) {
            HProcessProperty property = new HProcessProperty();
            property.setName(name);
            property.setNamespace(ns);
            if (simple) property.setSimpleContent(content);
            else property.setMixedContent(content);
            _process.getProperties().add(property);
            property.setProcess(_process);
            _sf.getCurrentSession().save(property);
        } else {
            if (content == null) {
                _sf.getCurrentSession().delete(existingProperty);
                _process.getProperties().remove(existingProperty);
            } else {
                if (simple) existingProperty.setSimpleContent(content);
                else existingProperty.setMixedContent(content);
                _sf.getCurrentSession().save(existingProperty);
            }
        }
        update();
    }

    private HProcessProperty getProperty(String name, String ns) {
        HProcessProperty existingProperty = null;
        for (HProcessProperty hproperty : _process.getProperties()) {
            if (hproperty.getName().equals(name) && hproperty.getNamespace().equals(ns))
                existingProperty = hproperty;
        }
        return existingProperty;
    }

    public Map<QName,Node> getProperties() {
        HashMap<QName,Node> propsMap = new HashMap<QName, Node>();
        Document doc = DOMUtils.newDocument();
        for (HProcessProperty hprop : _process.getProperties()) {
            QName propName = new QName(hprop.getNamespace(), hprop.getName());
            Node propNode = null;
            if (hprop.getSimpleContent() != null) {
                propNode = doc.createTextNode(hprop.getSimpleContent());
            } else if (hprop.getMixedContent() != null) {
                try {
                    propNode = DOMUtils.stringToDOM(hprop.getMixedContent());
                } catch (SAXException e) {
                    __log.error("Mixed content stored in property " + hprop.getName() +
                            " for process " + getProcessId() + " couldn't be converted to a DOM " +
                            "document.", e);
                } catch (IOException e) {
                    __log.error("Mixed content stored in property " + hprop.getName() +
                            " for process " + getProcessId() + " couldn't be converted to a DOM " +
                            "document.", e);
                }
            }
            propsMap.put(propName, propNode);
        }
        return propsMap;
    }

    public void delete() {
        _sf.getCurrentSession().delete(_process);
    }

    private void update() {
      _sf.getCurrentSession().update(_process);
    }
}
