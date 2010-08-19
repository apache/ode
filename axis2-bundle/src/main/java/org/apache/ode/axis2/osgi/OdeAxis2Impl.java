package org.apache.ode.axis2.osgi;

import java.io.File;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.ode.axis2.ODEAxis2Server;
import org.apache.ode.axis2.ODEConfigProperties;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;

public class OdeAxis2Impl implements OdeAxis2, BundleContextAware {
    
    private ODEAxis2Server server;
    private BundleContext bundleContext;
    
    private ODEConfigProperties config;
    
    public void init() throws Exception {
        String rootDir = System.getProperty("org.apache.ode.configDir", "ode");
        File confFile = new File(rootDir + "/conf/axis2.xml");
        System.out.println("Conf file " + confFile.getAbsolutePath());
        config = new ODEConfigProperties(new File(rootDir + "/conf"));
        config.load();
        server = new ODEAxis2Server(new File(rootDir).getAbsolutePath(), new File(rootDir).getAbsolutePath(), confFile.getAbsolutePath(), Integer.parseInt(config.getProperty("port")), config);
        server.start();
    }
    
    public void destroy() throws Exception {
        server.stop();
        server = null;
    }

    public Collection<QName> deployProcess(String bundleName) {
        return server.deployProcess(bundleName);
    }

    public void undeployProcess(String bundleName) {
        server.undeployProcess(bundleName);
    }

    public boolean isDeployed(String bundleName) {
        return server.isDeployed(bundleName);
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setConfig(ODEConfigProperties config) {
        this.config = config;
    }
}
