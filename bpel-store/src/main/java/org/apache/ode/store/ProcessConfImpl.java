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
import java.io.FileFilter;
import java.net.URI;
import java.text.ParseException;
import java.util.*;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dd.TCleanup;
import org.apache.ode.bpel.dd.TDeployment;
import org.apache.ode.bpel.dd.TInvoke;
import org.apache.ode.bpel.dd.TMexInterceptor;
import org.apache.ode.bpel.dd.TProcessEvents;
import org.apache.ode.bpel.dd.TProvide;
import org.apache.ode.bpel.dd.TSchedule;
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
import org.apache.ode.utils.CronExpression;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.HierarchicalProperties;
import org.apache.ode.utils.WatchDog;
import org.apache.ode.utils.CollectionUtils;
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

    private final Date _deployDate;
    private File _configDir;
    private final Map<QName, Node> _props;
    private final HashMap<String, Endpoint> _partnerRoleInitialValues = new HashMap<String, Endpoint>();

    private final HashMap<String, Endpoint> _myRoleEndpoints = new HashMap<String, Endpoint>();
    private final ArrayList<QName> _sharedServices = new ArrayList<QName>();
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

    // monitor the IL property file and reload it if necessary
    private WatchDog<Map<File, Long>, PropertiesObserver> propertiesWatchDog;

    private EndpointReferenceContext eprContext;

    private final ProcessCleanupConfImpl processCleanupConfImpl;

    ProcessConfImpl(QName pid, QName type, long version, DeploymentUnitDir du, TDeployment.Process pinfo, Date deployDate,
                    Map<QName, Node> props, ProcessState pstate, EndpointReferenceContext eprContext, File configDir) {
        _pid = pid;
        _version = version;
        _du = du;
        _pinfo = pinfo;
        _deployDate = deployDate;
        _configDir = configDir;
        _props = Collections.unmodifiableMap(props);
        _state = pstate;
        _type = type;
        _inMemory = _pinfo.isSetInMemory() && _pinfo.getInMemory();
        this.eprContext = eprContext;

        propertiesWatchDog = new WatchDog<Map<File, Long>, PropertiesObserver>(new PropertiesMutable(), new PropertiesObserver());

        initLinks();
        initMexInterceptors();
        initEventList();

        processCleanupConfImpl = new ProcessCleanupConfImpl(pinfo);
        
        initSchedules();
    }

    private List<File> collectEndpointConfigFiles() {
        // please mind the order: process-level files must be before system-level files
        List<File> propFiles = new ArrayList<File>();

        propFiles.addAll(_du.getEndpointConfigFiles());
        if (_configDir == null) {
            if (__log.isDebugEnabled()) __log.debug("No config directory set up.");
        } else if (_configDir.isDirectory()) {
            // list and sort endpoint config files
            File[] files = _configDir.listFiles(new FileFilter() {
                public boolean accept(File path) {
                    return path.getName().endsWith(".endpoint") && path.isFile();
                }
            });
            if (files != null) {
                Arrays.sort(files);
                propFiles.addAll(Arrays.asList(files));
            } else {
                if (__log.isErrorEnabled()) __log.error(_configDir + " does not exist or is not a directory");
            }
        } else {
            if (__log.isErrorEnabled()) __log.error(_configDir + " does not exist or is not a directory");
        }
        return propFiles;
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

                if (provide.isSetEnableSharing()) {
                    _sharedServices.add(service.getName());
                }
            }
        }
    }

    public Date getDeployDate() {
        return _deployDate;
    }

    public String getDeployer() {
        return "";
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

    public long getCBPFileSize() {
        CBPInfo cbpInfo = _du.getCBPInfo(getType());
        if (cbpInfo == null)
            throw new ContextException("CBP record not found for type " + getType());
        return cbpInfo.cbp.length();
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

    public Definition getDefinitionForPortType(QName portTypeName) {
        return _du.getDefinitionForPortType(portTypeName);
    }

    public Map<String, Endpoint> getInvokeEndpoints() {
        return Collections.unmodifiableMap(_partnerRoleInitialValues);
    }

    public Map<String, Endpoint> getProvideEndpoints() {
        return Collections.unmodifiableMap(_myRoleEndpoints);
    }

    public boolean isSharedService(QName serviceName) {
        return _sharedServices.contains(serviceName);
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unchecked")
    public Map<String, String> getEndpointProperties(EndpointReference epr) {
        final Map map = eprContext.getConfigLookup(epr);
        final QName service = (QName) map.get("service");
        final String port = (String) map.get("port");

        // update properties if necessary
        // do it manually to save resources (instead of using a thread)
        propertiesWatchDog.check();
        final Map prop = propertiesWatchDog.getObserver().get().getProperties(service, port);
        if(!prop.isEmpty() && __log.isDebugEnabled()) {
            StringBuilder msg = new StringBuilder("Properties for ");
            if(service!=null) msg.append("service ").append(service);
            if(port!=null) msg.append(", port ").append(port);
            msg.append(": {");
            for (Iterator it = prop.entrySet().iterator(); it.hasNext();) {
                Map.Entry e = (Map.Entry) it.next();
                msg.append(e.getKey()).append("=>").append(e.getValue());
                if(it.hasNext()) msg.append(", ");
            }
            msg.append("}");
            __log.debug(msg);
        }
        return prop;
    }

    private class PropertiesMutable implements WatchDog.Mutable<Map<File, Long>> {

        public boolean exists() {
            return true;
        }

        public boolean hasChangedSince(Map<File, Long> since) {
            return !CollectionUtils.equals(lastModified(), since);
        }

        public Map<File, Long> lastModified() {
            List<File> files = collectEndpointConfigFiles();
            Map<File, Long> m = new HashMap<File, Long>(files.size() * 15 / 10);
            for (File f : files) m.put(f, Long.valueOf(f.lastModified()));
            return m;
        }

        @Override
        public String toString() {
            return "Endpoint files for "+_du.toString();
        }
    }

    private class PropertiesObserver extends WatchDog.DefaultObserver<HierarchicalProperties> {

        public void init() {
            try {
                // do not hold a reference on the file list, so that changes are handled
                // and always create a new instance of the HierarchicalProperties
                object = new HierarchicalProperties(collectEndpointConfigFiles());
            } catch (IOException e) {
                throw new ContextException("Integration-Layer Properties cannot be loaded!", e);
            }
        }
    }

    public boolean isCleanupCategoryEnabled(boolean instanceSucceeded, CLEANUP_CATEGORY category) {
        return processCleanupConfImpl.isCleanupCategoryEnabled(instanceSucceeded, category);
    }

    public Set<CLEANUP_CATEGORY> getCleanupCategories(boolean instanceSucceeded) {
        return processCleanupConfImpl.getCleanupCategories(instanceSucceeded);
    }
    
    private void initSchedules() {
        for(TSchedule schedule : _pinfo.getScheduleList()) {
            for(TCleanup cleanup : schedule.getCleanupList()) {
                assert !cleanup.getFilterList().isEmpty();
            }
        }
    }
    
    public List<CronJob> getCronJobs() {
        List<CronJob> jobs = new ArrayList<CronJob>();
        
        for(TSchedule schedule : _pinfo.getScheduleList()) {
            CronJob job = new CronJob();
            try {
                job.setCronExpression(new CronExpression(schedule.getWhen()));
                for(final TCleanup aCleanup : schedule.getCleanupList()) {
                    CleanupInfo cleanupInfo = new CleanupInfo();
                    assert !aCleanup.getFilterList().isEmpty();
                    cleanupInfo.setFilters(aCleanup.getFilterList());
                    ProcessCleanupConfImpl.processACleanup(cleanupInfo.getCategories(), aCleanup.getCategoryList());
                    
                    Map<String, Object> runnableDetails = new HashMap<String, Object>();
                    runnableDetails.put("cleanupInfo", cleanupInfo);
                    runnableDetails.put("pid", _pid);
                    runnableDetails.put("transactionSize", 10);
                    job.getRunnableDetailList().add(runnableDetails);
                }
                jobs.add(job);
            } catch( ParseException pe ) {
                __log.error("Exception during parsing the schedule cron expression: " + schedule.getWhen() + ", skipped the scheduled job.", pe);
            }
        }
        
        return jobs;
    }
}