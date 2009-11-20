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

package org.apache.ode.jbi;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jbi.management.DeploymentException;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Representation of a JBI service unit. A JBI service unit may actually consist
 * of multiple processes.
 */
class OdeServiceUnit {
    private static final Log __log = LogFactory.getLog(OdeServiceUnit.class);

    private static final Messages __msgs = Messages.getMessages(Messages.class);

    /** The ever-present context. */
    private OdeContext _ode;

    /** Our own directory managed by JBI */
    private File _serviceUnitRootPath;

    /** Our JBI indentifier. */
    private String _serviceUnitID;

    private Collection<QName> _registered = new ArrayList<QName>();
    
    private static final String LIB_DIR = "lib";
    
    /** Ctor. */
    OdeServiceUnit(OdeContext ode, String serviceUnitID, String serviceUnitRootPath) {
        _ode = ode;
        _serviceUnitID = serviceUnitID;
        _serviceUnitRootPath = new File(serviceUnitRootPath);
    }

    public void deploy() throws DeploymentException {
    	ClassLoader cl = Thread.currentThread().getContextClassLoader();
    	try {
    		Thread.currentThread().setContextClassLoader(getConfigurationClassLoader());
            _ode._store.deploy(_serviceUnitRootPath);
        } catch (Exception ex) {
            String errmsg = __msgs.msgOdeProcessDeploymentFailed(_serviceUnitRootPath, _serviceUnitID);
            __log.error(errmsg, ex);
            throw new DeploymentException(errmsg, ex);
        } finally {
        	Thread.currentThread().setContextClassLoader(cl);
        }
    }

    public void undeploy() throws Exception {
        try {
            Collection<QName> undeployed = _ode._store.undeploy(_serviceUnitRootPath);
            for (QName pqname : undeployed) {
                _ode._server.unregister(pqname);
            }

        } catch (Exception ex) {
            String errmsg = __msgs.msgOdeProcessUndeploymentFailed(null);
            __log.error(errmsg, ex);
            throw new DeploymentException(errmsg, ex);
        }
    }

    public void init() throws Exception {
        // TODO Auto-generated method stub

    }

    public void shutdown() throws Exception {
        // TODO Auto-generated method stub

    }

    public void start() throws Exception {
        List<QName> pids = _ode._store.listProcesses(_serviceUnitRootPath.getName());
        if (pids == null) {
            __log.error(_serviceUnitRootPath.getName() + " not found in process store. ");
            throw new IllegalStateException("Process store and JBI out of synch.");
        }

        Exception e = null;
        for (QName pid : pids) {
            try {
                _ode._server.register(_ode._store.getProcessConfiguration(pid));
                _registered.add(pid);
            } catch (Exception ex) {
                e = ex;
                __log.error("Unable to load " + pid, ex);
                break;
            }
        }
        if (_registered.size() != pids.size()) {
            for (QName pid : new ArrayList<QName>(_registered))
                try {
                    _ode._server.unregister(pid);
                    _registered.remove(pid);
                } catch (Exception ex) {
                    __log.error("Unable to unload " + pid, ex);
                }
        }

        if (e != null)
            throw e;
    }

    public void stop() throws Exception {
        for (QName pid : new ArrayList<QName>(_registered)) {
            try {
                _ode._server.unregister(pid);
                _registered.remove(pid);
            } catch (Exception ex) {
                __log.error("Unable to unload " + pid, ex);
            }
        }
    }
    
    public ClassLoader getConfigurationClassLoader() throws DeploymentException {
    	return new URLClassLoader(getDefaultLocations(), getClass().getClassLoader());
    }
    
    protected URL[] getDefaultLocations() throws DeploymentException {
        try {
            File[] jars = new File(_serviceUnitRootPath, LIB_DIR).listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    name = name.toLowerCase();
                    return name.endsWith(".jar") || name.endsWith(".zip");
                }
            });
            URL[] urls = new URL[jars != null ? jars.length + 1 : 1];
            urls[0] = _serviceUnitRootPath.toURI().toURL();
            if (jars != null) {
                for (int i = 0; i < jars.length; i++) {
                    urls[i + 1] = jars[i].toURI().toURL();
                }
            }
            if (__log.isDebugEnabled()) {
            	for (URL u : urls) {
            		__log.debug("in classpath for "+_serviceUnitID+" using url: "+u);
            	}
            }
            return urls;
        } catch (MalformedURLException e) {
            throw new DeploymentException("Unable to get default classpath locations", e);
        }
    }

}
