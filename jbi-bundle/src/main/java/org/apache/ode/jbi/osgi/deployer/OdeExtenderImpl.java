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

import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jbi.component.Component;

import org.apache.ode.jbi.osgi.ServiceUnitActivator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.osgi.context.BundleContextAware;

public class OdeExtenderImpl implements OdeExtender, InitializingBean, DisposableBean, BundleContextAware {
    private static final Logger LOG = Logger.getLogger(OdeExtenderImpl.class.getName());
    private static final String SU_ACTIVATOR_DEPRECATED = ServiceUnitActivator.class.getName();
    private BundleContext bundleContext;
    private BundleTracker tracker;
    private Component odeComponent;
    private Executor executor;

    public OdeExtenderImpl() {
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void afterPropertiesSet() throws Exception {
        // Create our executor.
        executor = Executors.newSingleThreadExecutor(new OdeExtenderThreadFactory("ODE Extender"));

        // Start tracking bundles. We are looking for /deploy.xml as a signature
        // to deploy the ODE BPEL bundle, if the bundle has at least one .bpel
        // file.
        int stateMask = Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING | Bundle.INSTALLED | Bundle.UNINSTALLED;
        this.tracker = new BundleTracker(this.bundleContext, stateMask, new ODEBundleTrackerCustomizer());
        this.tracker.open();
    }

    public void destroy() throws Exception {
        // Close the tracker.
        BundleTracker tracker = this.tracker;
        this.tracker = null;
        if (tracker != null)
            tracker.close();

        // Drop our thread pool.
        this.executor = null;
    }

    public Bundle[] getBundles() {
        return this.tracker.getBundles();
    }

    public void setOdeComponent(Component odeComponent) {
        this.odeComponent = odeComponent;
    }

    public Component getOdeComponent() {
        return odeComponent;
    }

    private boolean isBPELBundle(Bundle bundle) {
        boolean result = false;

        // First see if there is a deploy.xml.
        URL entry = bundle.getEntry("deploy.xml");
        if (entry != null) {
            // Next, check if there's at least one BPEL file.
            @SuppressWarnings("rawtypes")
            Enumeration bpelFiles = bundle.findEntries("/", "*.bpel", false);
            if (bpelFiles != null && bpelFiles.hasMoreElements()) {
                // Make sure there's a symbolic name.
                if (bundle.getSymbolicName() != null) {
                    // Make sure there's no bundle activator.
                    // NOTE: if the "ServiceUnitActivator" is found, we hijack those bundles as well and manage them.
                    Dictionary<?, ?> headers = bundle.getHeaders();
                    Object activator = null;
                    if (headers == null || (activator = headers.get(Constants.BUNDLE_ACTIVATOR)) == null || SU_ACTIVATOR_DEPRECATED.equals(activator)) {
                        if (LOG.isLoggable(Level.FINE))
                            LOG.fine("Recognized ODE deployment bundle: " + bundle.getSymbolicName());
                        result = true;
                    } else
                        LOG.warning("Ignoring ODE bundle " + bundle.getSymbolicName() + " which has custom activator: " + activator);
                } else
                    LOG.warning("Ignoring ODE bundle " + bundle.getBundleId() + " which has no OSGi symbolic name");
            }
        }

        return result;
    }

    private class ODEBundleTrackerCustomizer implements BundleTrackerCustomizer {
        private Map<Long, OdeDeployedBundle> bundles = new ConcurrentHashMap<Long, OdeDeployedBundle>();

        public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
        }

        public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
            if (event != null) {
                if (LOG.isLoggable(Level.FINE))
                    LOG.fine("Received " + getEventType(event.getType()) + " event for bundle: " + bundle.getSymbolicName());
                switch (event.getType()) {
                case BundleEvent.STARTED:
                    executor.execute(new Start((OdeDeployedBundle) object));
                    break;
                case BundleEvent.STOPPING:
                    executor.execute(new Stop((OdeDeployedBundle) object));
                    break;
                case BundleEvent.INSTALLED:
                    executor.execute(new Install((OdeDeployedBundle) object));
                    break;
                case BundleEvent.UNINSTALLED:
                    executor.execute(new Uninstall((OdeDeployedBundle) object));
                    break;
                case BundleEvent.UPDATED:
                    executor.execute(new Update((OdeDeployedBundle) object));
                    break;
                }

                // Do this outside the try/catch above. Last chance to drop a
                // bundle.
                if (event.getType() == BundleEvent.UNINSTALLED)
                    bundles.remove(bundle.getBundleId());
            }
        }

        private String getEventType(int type) {
            switch (type) {
            case BundleEvent.INSTALLED:
                return "installed";
            case BundleEvent.LAZY_ACTIVATION:
                return "lazy activation";
            case BundleEvent.RESOLVED:
                return "resolved";
            case BundleEvent.STARTED:
                return "started";
            case BundleEvent.STARTING:
                return "starting";
            case BundleEvent.STOPPED:
                return "stopped";
            case BundleEvent.STOPPING:
                return "stopping";
            case BundleEvent.UNINSTALLED:
                return "uninstalled";
            case BundleEvent.UNRESOLVED:
                return "unresolved";
            case BundleEvent.UPDATED:
                return "updated";
            }
            return "(unknown: " + type + ")";
        }

        public Object addingBundle(Bundle bundle, BundleEvent event) {
            Object result = null;

            // Is this a BPEL bundle?
            if (isBPELBundle(bundle)) {
                // Found BPEL signature; setup deployer.
                OdeDeployedBundle deployer = bundles.get(bundle.getBundleId());
                if (deployer == null)
                    bundles.put(bundle.getBundleId(), deployer = new OdeDeployedBundle(bundle, OdeExtenderImpl.this));
                result = deployer;

                // Is this an initial bundle?
                if (event == null) {
                    // If the bundle is active, then we didn't start it. So
                    // start it now.
                    if (bundle.getState() == Bundle.ACTIVE)
                        executor.execute(new Start(deployer));
                } else
                    modifiedBundle(bundle, event, deployer);
            }

            return result;
        }
    }

    private static abstract class OperationClosure implements Runnable {
        protected OdeDeployedBundle target;

        OperationClosure(OdeDeployedBundle target) {
            this.target = target;
        }

        public final void run() {
            String name = Thread.currentThread().getName();
            try {
                Thread.currentThread().setName(name + ":" + getClass().getSimpleName() + ":" + target.getName());
                perform();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Could not perform '" + getClass().getSimpleName() + "' operation on bundle: " + target.getName(), e);
            } finally {
                Thread.currentThread().setName(name);
            }
        }

        protected abstract void perform() throws Exception;
    }

    private static class Start extends OperationClosure {
        Start(OdeDeployedBundle target) {
            super(target);
        }

        @Override
        protected void perform() throws Exception {
            target.doStart();
        }
    }

    private static class Stop extends OperationClosure {
        Stop(OdeDeployedBundle target) {
            super(target);
        }

        @Override
        protected void perform() throws Exception {
            target.doStop();
        }
    }

    private static class Install extends OperationClosure {
        Install(OdeDeployedBundle target) {
            super(target);
        }

        @Override
        protected void perform() throws Exception {
            target.doInstall();
        }
    }

    private static class Uninstall extends OperationClosure {
        Uninstall(OdeDeployedBundle target) {
            super(target);
        }

        @Override
        protected void perform() throws Exception {
            target.doUninstall();
        }
    }

    private static class Update extends OperationClosure {
        Update(OdeDeployedBundle target) {
            super(target);
        }

        @Override
        protected void perform() throws Exception {
            target.doUpdate();
        }
    }
}
