package org.apache.ode.bpel.rapi;

import javax.xml.namespace.QName;
import java.util.Map;

public interface ExtensionBundle {

    String getNamespaceURI();

    void registerExtensionActivities();

    Map<QName, ExtensionValidator> getExtensionValidators();
}
