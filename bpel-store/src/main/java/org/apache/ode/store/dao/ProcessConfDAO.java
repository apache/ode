package org.apache.ode.store.dao;

import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.Date;
import java.util.Map;

/**
 * @author mriou <mriou at apache dot org>
 */
public interface ProcessConfDAO {
    Date getDeployDate();

    String getDeployer();

    QName getProcessId();

    String getTypeName();

    String getTypeNamespace();

    int getVersion();

    boolean isActive();

    void setActive(boolean active);

    void setProperty(String name, String ns, Node content);

    void setProperty(String name, String ns, String content);

    Map<QName,Node> getProperties();

    void delete();
}
