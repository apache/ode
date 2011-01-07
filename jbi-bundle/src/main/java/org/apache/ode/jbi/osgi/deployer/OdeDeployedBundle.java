/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ode.jbi.osgi.deployer;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jbi.component.Component;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.component.ServiceUnitManager;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.apache.ode.bpel.iapi.ProcessStore;
import org.apache.ode.jbi.OdeContext;
import org.osgi.framework.Bundle;

public class OdeDeployedBundle {
    private static final Logger LOG = Logger.getLogger(OdeDeployedBundle.class.getName());
    private boolean updated;
    private Bundle bundle;
    private OdeExtenderImpl odeRegistrationService;
    private File rootDir;
    private String duName;
    private String name;

    OdeDeployedBundle(Bundle bundle, OdeExtenderImpl odeRegistrationService) {
        if (LOG.isLoggable(Level.FINE))
            LOG.fine("Initialized ODE service unit deployer for bundle: " + bundle.getSymbolicName());
        this.bundle = bundle;
        this.odeRegistrationService = odeRegistrationService;
    }

    String getName() {
        if (this.name == null)
            this.name = this.bundle.getSymbolicName();
        return this.name;
    }

    private String getDUName() {
        if (this.duName == null)
            this.duName = getName() + "-" + bundle.getVersion().getMicro();
        return this.duName;
    }

    private File getRootDir() {
        if (this.rootDir == null && this.bundle.getBundleContext() != null)
            this.rootDir = this.bundle.getBundleContext().getDataFile("bpelData/" + getDUName());
        return this.rootDir;
    }

    void doStart() throws Exception {
        // If we are already started, don't bother starting again.
        LOG.info("Starting ODE service unit: " + getName());

        // Wait until ODE is available before starting.
        waitAvailable();

        // Do we need to undeploy first?
        boolean needUpdate = updated;
        boolean forceDeploy = needUpdate;
        this.updated = false;

        // Do deploy.
        this.deploy(this.bundle, this.odeRegistrationService.getOdeComponent().getServiceUnitManager(), forceDeploy, needUpdate);
    }

    void doStop() throws Exception {
        LOG.info("Stopping ODE service unit: " + getName());
        this.shutdown(this.bundle, this.odeRegistrationService.getOdeComponent().getServiceUnitManager());
    }

    void doInstall() throws Exception {
        LOG.info("Installing ODE service unit: " + getName());
    }

    void doUninstall() throws Exception {
        LOG.info("Uninstalling ODE service unit: " + getName());
        this.undeploy(this.bundle, this.odeRegistrationService.getOdeComponent().getServiceUnitManager());
    }

    void doUpdate() throws Exception {
        LOG.info("Updating ODE service unit: " + getName());

        // We simply mark the update state of this bundle so that on next start, we do a redeploy
        updated = true;
    }

    private void deploy(Bundle bundle, ServiceUnitManager suM, boolean forceDeploy, boolean undeploy) throws Exception {
        // Do deployment.
        File rootDir = getRootDir();
        String duName = getDUName();
        boolean needDeploy = rootDir.mkdirs() || forceDeploy;
        if (LOG.isLoggable(Level.FINE))
            LOG.fine("Exploding content to " + rootDir + " for " + duName);
        Enumeration<?> en = bundle.findEntries("/", "*", false);
        while (en.hasMoreElements())
            needDeploy |= copyOne(rootDir, (URL) en.nextElement());

        // Now start it.
        ClassLoader l = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader suL = suM.getClass().getClassLoader();
            Thread.currentThread().setContextClassLoader(new BundleClassLoader(suL, bundle));
            try {
                // Try first an init/start, which will fail if the process isn't
                // here.
                if (needDeploy) {
                    // If deployed, undeploy first.
                    if (undeploy && isDeployed(duName)) {
                        // Do the undeploy to service unit manager.
                        LOG.info("Performing undeploy " + duName + " from dir " + rootDir);
                        suM.undeploy(duName, rootDir.getAbsolutePath());

                        // Now, remove any .cbp files.
                        File[] cbpFiles = rootDir.listFiles(new FilenameFilter() {
                            public boolean accept(File dir, String name) {
                                return name.endsWith(".cbp");
                            }
                        });
                        for (File cbpFile : cbpFiles) {
                            LOG.info("Removing compiled bpel process: " + cbpFile);
                            cbpFile.delete();
                        }
                    }

                    LOG.info("Deploying " + duName + " to dir " + rootDir);
                    suM.deploy(duName, rootDir.getAbsolutePath());
                }
                suM.init(duName, rootDir.getAbsolutePath());
                suM.start(duName);
            } catch (javax.jbi.management.DeploymentException e) {
                LOG.log(Level.WARNING, "Deploy failed; could not deploy/start this bundle: " + this.getName(), e);
                throw e;
            }
        } finally {
            Thread.currentThread().setContextClassLoader(l);
        }
    }

    private boolean isDeployed(String processName) {
        boolean Result = true;
        try {
            // Get the "ProcessStore" interface by grabbing the internal field
            // of OdeContext and querying for the processes. Could also use PMAPI here, 
            // but in any case we just need to know if the process exists in a deployed state.
            //
            // TODO: add a OdeContext.getStore() method to clean this up.
            OdeContext inst = OdeContext.getInstance();
            Field _store = inst.getClass().getDeclaredField("_store");
            _store.setAccessible(true);
            ProcessStore ps = (ProcessStore) _store.get(inst);
            List<QName> processes = ps.listProcesses(processName);
            Result = processes != null && !processes.isEmpty();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Could not determine deployment state for process: " + processName, e);
        }
        return Result;
    }

    private boolean copyOne(File dest, URL url) throws Exception {
        File d = new File(dest, url.getPath());
        boolean needDeploy = !d.exists();
        long length = d.exists() ? d.length() : 0L;
        InputStream str = url.openStream();
        if (str != null) {
            FileWriter wr = new FileWriter(d);
            try {
                IOUtils.copy(str, wr);
            } finally {
                wr.flush();
                wr.close();
            }

            // If file is zero-length (which is the case handling a directory),
            // just remove it.
            if (d.exists() && d.length() == 0) {
                d.delete();
                needDeploy = false;
            } else
                needDeploy |= length != d.length();
        }
        return needDeploy;
    }

    private void shutdown(Bundle bundle, ServiceUnitManager suM) throws Exception {
        String duName = getDUName();
        if (suM != null) {
            suM.stop(duName);
            suM.shutDown(duName);
        } else {
            LOG.warning("Could not shutdown this process (" + duName + ") because ode component was never located");
        }
    }

    private void undeploy(Bundle bundle, ServiceUnitManager suM) throws Exception {
        String duName = getDUName();
        if (suM != null) {
            if (isDeployed(duName)) {
                // Use ODE's classloader to avoid class loading from the bundle
                // being undeployed.
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(suM.getClass().getClassLoader());
                    File rootDir = getRootDir();
                    LOG.info("Performing undeploy " + duName + " from dir " + rootDir);
                    suM.undeploy(duName, rootDir.getAbsolutePath());
                } finally {
                    Thread.currentThread().setContextClassLoader(classLoader);
                }
            }
        } else {
            LOG.warning("Could not shutdown this process (" + duName + ") because ode component was never located");
        }
    }

    public class BundleClassLoader extends ClassLoader {
        private final Bundle delegate;

        public BundleClassLoader(ClassLoader parent, Bundle delegate) {
            super(parent);
            this.delegate = delegate;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            try {
                return getParent().loadClass(name);
            } catch (Exception e) {
                return delegate.loadClass(name);
            }
        }
    }

    private void waitAvailable() throws InterruptedException {
        /**
         * We need to wait until the service unit manager is available before 
         * proceeding.  Also, since the ode component itself does an asynchronous
         * start with respect to this bundle, we need to wait until it is done
         * initializing.  This would be much cleaner if we could simply
         * call "isStarted" on OdeLifeCycle, which maintains a started state.
         * 
         * If we do not wait until the ode component is started, deployments
         * will fail sporadically because of asynchronous start race conditions.
         */
        boolean showedWait = false;
        while (this.odeRegistrationService.getOdeComponent().getServiceUnitManager() == null || !isStarted(this.odeRegistrationService.getOdeComponent())) {

            // Do a wait.
            if (!showedWait) {
                LOG.info("Waiting for ODE to arrive (" + getName() + ")...");
                showedWait = true;
            }
            Thread.sleep(500L);
        }
    }

    private boolean isStarted(Component odeComponent) {

        boolean Result = true;
        try {
            // Get the ODE component started state by grabbing the internal field
            // of OdeLifeCycle.  We cannot rely on the started state of the ODE
            // component bundle.
            //
            // TODO: add OdeLifeCycle.isStarted() and do a cast of odeComponent.getLifeCycle() accordingly.
            ComponentLifeCycle inst = odeComponent.getLifeCycle();
            Field _started = inst.getClass().getDeclaredField("_started");
            _started.setAccessible(true);
            Result = Boolean.TRUE.equals(_started.get(inst));
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Could not determine started state for ode component", e);
        }
        return Result;
    }
}
