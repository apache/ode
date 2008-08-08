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
package org.apache.ode.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dd.TDeployment;
import org.apache.ode.bpel.dd.TInvoke;
import org.apache.ode.bpel.dd.TMexInterceptor;
import org.apache.ode.bpel.dd.TProcessEvents;
import org.apache.ode.bpel.dd.TProvide;
import org.apache.ode.bpel.dd.TScopeEvents;
import org.apache.ode.bpel.dd.TService;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessState;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.store.DeploymentUnitDir.CBPInfo;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.HierarchicalProperties;
import org.apache.ode.utils.fs.FileWatchDog;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implementation of the {@link org.apache.ode.bpel.iapi.ProcessConf} interface.
 * Provides configuration information for a process. Note that this class should
 * be immutable, that is the engine expects it to return consistent results!
 *
 * @author mriou <mriou at apache dot org>
 */
public class ProcessConfImpl implements ProcessConf {
    private static final Log __log = LogFactory.getLog(ProcessConfImpl.class);
    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    private final Date _deployDate;
    private final Map<QName, Node> _props;
    private final HashMap<String, Endpoint> _partnerRoleInitialValues = new HashMap<String, Endpoint>();

    private final HashMap<String, Endpoint> _myRoleEndpoints = new HashMap<String, Endpoint>();
    private final Map<String, Set<BpelEvent.TYPE>> _events = new HashMap<String, Set<BpelEvent.TYPE>>();
    private final ArrayList<String> _mexi = new ArrayList<String>();
    ProcessState _state;
    final TDeployment.Process _pinfo;
    final DeploymentUnitDir _du;
    private long _version = 0;
    private QName _pid;
    private QName _type;

    // cache the inMemory flag because XMLBeans objects are heavily synchronized (guarded by a coarse-grained lock)
    private volatile boolean _inMemory = false;

    // provide the EPR properties
    private HierarchicalProperties eprProperties;
    // monitor the EPR property file and reload it if necessary
    private EPRPropertiesWatchDog eprPropertiesWatchDog;
    private final ReadWriteLock eprPropertiesLock = new ReentrantReadWriteLock();

    private EndpointReferenceContext eprContext;

    ProcessConfImpl(QName pid, QName type, long version, DeploymentUnitDir du, TDeployment.Process pinfo, Date deployDate,
                    Map<QName, Node> props, ProcessState pstate, EndpointReferenceContext eprContext) {
        _pid = pid;
        _version = version;
        _du = du;
        _pinfo = pinfo;
        _deployDate = deployDate;
        _props = Collections.unmodifiableMap(props);
        _state = pstate;
        _type = type;
        _inMemory = _pinfo.isSetInMemory() && _pinfo.getInMemory();
        this.eprContext = eprContext;
        eprPropertiesWatchDog = new EPRPropertiesWatchDog();

        initLinks();
        initMexInterceptors();
        initEventList();
    }

    private void initMexInterceptors() {
        if (_pinfo.getMexInterceptors() != null) {
            for (TMexInterceptor mexInterceptor : _pinfo.getMexInterceptors().getMexInterceptorList()) {
                _mexi.add(mexInterceptor.getClassName());
            }
        }
    }

    private void initLinks() {
        if (_pinfo.getInvokeList() != null) {
            for (TInvoke invoke : _pinfo.getInvokeList()) {
                String plinkName = invoke.getPartnerLink();
                TService service = invoke.getService();
                // NOTE: service can be null for partner links
                if (service == null)
                    continue;
                __log.debug("Processing <invoke> element for process " + _pinfo.getName() + ": partnerlink " + plinkName + " --> "
                        + service);
                _partnerRoleInitialValues.put(plinkName, new Endpoint(service.getName(), service.getPort()));
            }
        }

        if (_pinfo.getProvideList() != null) {
            for (TProvide provide : _pinfo.getProvideList()) {
                String plinkName = provide.getPartnerLink();
                TService service = provide.getService();
                if (service == null) {
                    String errmsg = "Error in <provide> element for process " + _pinfo.getName() + "; partnerlink " + plinkName
                            + "did not identify an endpoint";
                    __log.error(errmsg);
                    throw new ContextException(errmsg);
                }
                __log.debug("Processing <provide> element for process " + _pinfo.getName() + ": partnerlink " + plinkName + " --> "
                        + service.getName() + " : " + service.getPort());
                _myRoleEndpoints.put(plinkName, new Endpoint(service.getName(), service.getPort()));
            }
        }
    }

    public Date getDeployDate() {
        return _deployDate;
    }

    public List<File> getFiles() {
        return _du.allFiles();
    }

    public QName getProcessId() {
        return _pid;
    }

    public QName getType() {
        return _pinfo.getType() == null ? _type : _pinfo.getType();
    }

    public String getPackage() {
        return _du.getName();
    }

    public Map<QName, Node> getProcessProperties() {
        return _props;
    }

    public long getVersion() {
        return _version;
    }

    public int getRuntimeVersion() {
        return 2;
    }

    public InputStream getCBPInputStream() {
        CBPInfo cbpInfo = _du.getCBPInfo(getType());
        if (cbpInfo == null)
            throw new ContextException("CBP record not found for type " + getType());
        try {
            return new FileInputStream(cbpInfo.cbp);
        } catch (FileNotFoundException e) {
            throw new ContextException("File Not Found: " + cbpInfo.cbp, e);
        }
    }

    public String getBpelDocument() {
        CBPInfo cbpInfo = _du.getCBPInfo(getType());
        if (cbpInfo == null)
            throw new ContextException("CBP record not found for type " + getType());
        try {
            String relative = getRelativePath(_du.getDeployDir(), cbpInfo.cbp).replaceAll("\\\\", "/");
            if (!relative.endsWith(".cbp"))
                throw new ContextException("CBP file must end with .cbp suffix: " + cbpInfo.cbp);
            relative = relative.replace(".cbp", ".bpel");
            File bpelFile = new File(_du.getDeployDir(), relative);
            if (!bpelFile.exists()) __log.warn("BPEL file does not exist: " + bpelFile);
            return relative;
        } catch (IOException e) {
            throw new ContextException("IOException in getBpelRelativePath: " + cbpInfo.cbp, e);
        }
    }

    public URI getBaseURI() {
        return _du.getDeployDir().toURI();
    }

    public ProcessState getState() {
        return _state;
    }

    void setState(ProcessState state) {
        _state = state;
    }

    public List<String> getMexInterceptors(QName processId) {
        return Collections.unmodifiableList(_mexi);
    }

    public Definition getDefinitionForService(QName serviceName) {
        return _du.getDefinitionForService(serviceName);
    }

    public Map<String, Endpoint> getInvokeEndpoints() {
        return Collections.unmodifiableMap(_partnerRoleInitialValues);
    }

    public Map<String, Endpoint> getProvideEndpoints() {
        return Collections.unmodifiableMap(_myRoleEndpoints);
    }

    private void handleEndpoints() {
        // for (TProvide provide : _pinfo.getProvideList()) {
        // OPartnerLink pLink = _oprocess.getPartnerLink(provide.getPartnerLink());
        // if (pLink == null) {
        // String msg = __msgs.msgDDPartnerLinkNotFound(provide.getPartnerLink());
        // __log.error(msg);
        // throw new BpelEngineException(msg);
        // }
        // if (!pLink.hasMyRole()) {
        // String msg = __msgs.msgDDMyRoleNotFound(provide.getPartnerLink());
        // __log.error(msg);
        // throw new BpelEngineException(msg);
        // }
        // }
        // for (TInvoke invoke : _pinfo.getInvokeList()) {
        // OPartnerLink pLink = _oprocess.getPartnerLink(invoke.getPartnerLink());
        // if (pLink == null) {
        // String msg = __msgs.msgDDPartnerLinkNotFound(invoke.getPartnerLink());
        // __log.error(msg);
        // throw new BpelEngineException(msg);
        // }
        // if (!pLink.hasPartnerRole()) {
        // String msg = __msgs.msgDDPartnerRoleNotFound(invoke.getPartnerLink());
        // __log.error(msg);
        // throw new BpelEngineException(msg);
        // }
        // TODO Handle non initialize partner roles that just provide a binding
        // if (!pLink.initializePartnerRole && _oprocess.version.equals(Namespaces.WS_BPEL_20_NS)) {
        // String msg = ProcessDDInitializer.__msgs.msgDDNoInitiliazePartnerRole(invoke.getPartnerLink());
        // ProcessDDInitializer.__log.error(msg);
        // throw new BpelEngineException(msg);
        // }
        // }
    }

    DeploymentUnitDir getDeploymentUnit() {
        return _du;
    }

    public boolean isTransient() {
        return _inMemory;
    }

    public void setTransient(boolean t) {
        _pinfo.setInMemory(t);
        _inMemory = t;
    }

    public boolean isEventEnabled(List<String> scopeNames, BpelEvent.TYPE type) {
        if (scopeNames != null) {
            for (String scopeName : scopeNames) {
                Set<BpelEvent.TYPE> evtSet = _events.get(scopeName);
                if (evtSet != null) {
                    if (evtSet.contains(type)) return true;
                }
            }
        }
        Set<BpelEvent.TYPE> evtSet = _events.get(null);
        if (evtSet != null) {
            // Default filtering at the process level for some event types
            if (evtSet.contains(type)) return true;
        }
        return false;
    }

    private void initEventList() {
        TProcessEvents processEvents = _pinfo.getProcessEvents();
        // No filtering, using defaults
        if (processEvents == null) {
            HashSet<BpelEvent.TYPE> all = new HashSet<BpelEvent.TYPE>();
            for (BpelEvent.TYPE t : BpelEvent.TYPE.values()) {
                if (!t.equals(BpelEvent.TYPE.scopeHandling)) all.add(t);
            }
            _events.put(null, all);
            return;
        }

        // Adding all events
        if (processEvents.getGenerate() != null && processEvents.getGenerate().equals(TProcessEvents.Generate.ALL)) {
            HashSet<BpelEvent.TYPE> all = new HashSet<BpelEvent.TYPE>();
            for (BpelEvent.TYPE t : BpelEvent.TYPE.values())
                all.add(t);
            _events.put(null, all);
            return;
        }

        // Events filtered at the process level
        if (processEvents.getEnableEventList() != null) {
            List<String> enabled = processEvents.getEnableEventList();
            HashSet<BpelEvent.TYPE> evtSet = new HashSet<BpelEvent.TYPE>();
            for (String enEvt : enabled) {
                evtSet.add(BpelEvent.TYPE.valueOf(enEvt));
            }
            _events.put(null, evtSet);
        }

        // Events filtered at the scope level
        if (processEvents.getScopeEventsList() != null) {
            for (TScopeEvents tScopeEvents : processEvents.getScopeEventsList()) {
                HashSet<BpelEvent.TYPE> evtSet = new HashSet<BpelEvent.TYPE>();
                for (String enEvt : tScopeEvents.getEnableEventList()) {
                    evtSet.add(BpelEvent.TYPE.valueOf(enEvt));
                }
                _events.put(tScopeEvents.getName(), evtSet);
            }
        }
    }

    private String getRelativePath(File base, File path) throws IOException {
        String basePath = base.getCanonicalPath();
        String cbpPath = path.getCanonicalPath();
        if (!cbpPath.startsWith(basePath))
            throw new IOException("Invalid relative path: base=" + base + " path=" + path);
        String relative = cbpPath.substring(basePath.length());
        if (relative.startsWith(File.separator)) relative = relative.substring(1);
        return relative;
    }

    public List<Element> getExtensionElement(QName qname) {
        try {
            return DOMUtils.findChildrenByName(DOMUtils.stringToDOM(_pinfo.toString()), qname);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public Map<String, String> getEndpointProperties(EndpointReference epr) {
        final Map map = eprContext.getConfigLookup(epr);
        final QName service = (QName) map.get("service");
        final String port = (String) map.get("port");

        // update properties if necessary
        // do it manually to save resources (instead of using a thread)
        eprPropertiesWatchDog.check();
        if (eprProperties == null) {
            return Collections.EMPTY_MAP;
        } else {
            // take a lock so we can have a consistent snapshot of the properties
            eprPropertiesLock.readLock().lock();
            try {
                return eprProperties.getProperties(service, port);
            } finally {
                eprPropertiesLock.readLock().unlock();
            }
        }
    }

    /**
     * Manage the reloading of the propery file every {@link org.apache.ode.utils.fs.FileWatchDog#DEFAULT_DELAY}.
     * The check is done manually, meaning that {@link #check()} must be invoked each time eprProperties is accessed.
     */
    private class EPRPropertiesWatchDog extends FileWatchDog {
        public EPRPropertiesWatchDog() {
            super(_du.getEPRConfigFile());
        }

        protected void init() {
            eprPropertiesLock.writeLock().lock();
            try {
                if (eprProperties == null) {
                    try {
                        eprProperties = new HierarchicalProperties(super.file);
                    } catch (IOException e) {
                        throw new ContextException("EPR Config Properties cannot be loaded!", e);
                    }
                } else {
                    eprProperties.clear();
                }
            } finally {
                eprPropertiesLock.writeLock().unlock();
            }
        }

        protected boolean isInitialized() {
            return eprProperties != null;
        }

        protected void doOnUpdate() {
            eprPropertiesLock.writeLock().lock();
            try {
                init();
                try {
                    eprProperties.loadFile();
                } catch (IOException e) {
                    throw new ContextException("EPR Config Properties cannot be loaded!", e);
                }
            } finally {
                eprPropertiesLock.writeLock().unlock();
            }
        }
    }


}
