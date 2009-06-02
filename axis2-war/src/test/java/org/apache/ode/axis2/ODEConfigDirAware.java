package org.apache.ode.axis2;

public interface ODEConfigDirAware {
    final String HIB_DERBY_CONF_DIR = ODEConfigDirAware.class.getClassLoader().getResource("webapp").getFile() + "/WEB-INF/conf.hib-derby";
    final String JPA_DERBY_CONF_DIR = ODEConfigDirAware.class.getClassLoader().getResource("webapp").getFile() + "/WEB-INF/conf.jpa-derby";

    String getODEConfigDir();
}