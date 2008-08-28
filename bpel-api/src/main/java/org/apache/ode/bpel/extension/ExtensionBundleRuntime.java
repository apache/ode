package org.apache.ode.bpel.extension;

public interface ExtensionBundleRuntime {

    String getNamespaceURI();

    void registerExtensionActivities();

    ExtensionOperation getExtensionOperationInstance(String localName)
            throws InstantiationException, IllegalAccessException;

}
