/*
 * File:      $Id: ProcessDaoImpl.java 1220 2006-04-27 20:03:24Z mbs $
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.memdao;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.CorrelationSetDAO;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ProcessPropertyDAO;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A very simple, in-memory implementation of the {@link ProcessDAO} interface.
 */
class ProcessDaoImpl extends DaoBaseImpl implements ProcessDAO {
    private static final Log __log = LogFactory.getLog(ProcessDaoImpl.class);

    private QName _processId;
    private QName _type;
    private final Map<String, CorrelatorDAO> _correlators = new ConcurrentHashMap<String, CorrelatorDAO>();
    protected final Map<Long, ProcessInstanceDAO> _instances = new ConcurrentHashMap<Long, ProcessInstanceDAO>();
    protected final Map<Integer, PartnerLinkDAO> _plinks = new ConcurrentHashMap<Integer, PartnerLinkDAO>();
    private Map<String, ProcessDaoImpl> _store;
    private BpelDAOConnectionImpl _conn;
    private final Date _deployDate = new Date();
    private boolean _retired;
    private boolean _active;
    private byte[] _compiledProcess;
    private MultiKeyMap _properties = new MultiKeyMap();

    public ProcessDaoImpl(BpelDAOConnectionImpl conn,
                          Map<String, ProcessDaoImpl> store,
                          QName processId, QName type) {
        if (__log.isDebugEnabled()) {
            __log.debug("Creating ProcessDao object for process \"" + processId + "\".");
        }

        _conn = conn;
        _store = store;
        _processId = processId;
        _type = type;
    }

    public QName getProcessId() {
        return _processId;
    }

    public CorrelatorDAO getCorrelator(String cid) {
        CorrelatorDAO ret = _correlators.get(cid);
        if (ret == null) {
            throw new IllegalArgumentException("no such correlator: " + cid);
        }
        return ret;
    }

    public Collection<CorrelatorDAO> getCorrelators() {
        return _correlators.values();
    }

    public void removeRoutes(String routeId, ProcessInstanceDAO target) {
        for (CorrelatorDAO correlatorDAO : _correlators.values()) {
            correlatorDAO.removeRoutes(routeId, target);
        }
    }

    public ProcessInstanceDAO createInstance(CorrelatorDAO correlator) {
        ProcessInstanceDaoImpl newInstance = new ProcessInstanceDaoImpl(_conn, this, correlator);
        _instances.put(newInstance.getInstanceId(), newInstance);
        return newInstance;
    }

    public ProcessInstanceDAO getInstance(Long instanceId) {
        return _instances.get(instanceId);
    }


    public Collection<ProcessInstanceDAO> findInstance(CorrelationKey key) {
        ArrayList<ProcessInstanceDAO> result = new ArrayList<ProcessInstanceDAO>();
        for (ProcessInstanceDAO instance : _instances.values()) {
            for (CorrelationSetDAO corrSet : instance.getCorrelationSets()) {
                if (corrSet.getValue().equals(key)) result.add(instance);
            }
        }
        return result;
    }

    public void instanceCompleted(ProcessInstanceDAO instance) {
        _instances.remove(instance.getInstanceId());
    }

    public void setProperty(String name, String ns, Node content) {
        if (content == null) {
            _properties.remove(name, ns);
        } else {
            ProcessPropertyDAOImpl pp = new ProcessPropertyDAOImpl();
            pp.setName(name);
            pp.setNamespace(ns);
            pp.setMixedContent(DOMUtils.domToString(content));
            _properties.put(name, ns, pp);
        }
    }

    public void setProperty(String name, String ns, String content) {
        if (content == null) {
            _properties.remove(name, ns);
        } else {
            ProcessPropertyDAOImpl pp = new ProcessPropertyDAOImpl();
            pp.setName(name);
            pp.setNamespace(ns);
            pp.setSimpleContent(content);
            _properties.put(name, ns, pp);
        }
    }

    public Collection<ProcessPropertyDAO> getProperties() {
        return _properties.values();
    }

    public Collection<PartnerLinkDAO> getDeployedEndpointReferences() {
        return _plinks.values();
    }

    public void delete() {
        _store.remove(_processId);
    }

    public int getVersion() {
        return 0;
    }

    public String getDeployer() {
        return "nobody";
    }

    public Date getDeployDate() {
        return _deployDate;
    }

    public boolean isRetired() {
        return _retired;
    }

    public void setRetired(boolean retired) {
        this._retired = retired;
    }

    public QName getType() {
        return _type;
    }

    public PartnerLinkDAO addDeployedPartnerLink(int plinkModelId, String plinkName, String myRoleName, String partnerRoleName) {
        PartnerLinkDAOImpl plink = new PartnerLinkDAOImpl();
        plink.setPartnerLinkModelId(plinkModelId);
        plink.setPartnerLinkName(plinkName);
        plink.setMyRoleName(myRoleName);
        plink.setPartnerRoleName(partnerRoleName);
        _plinks.put(plinkModelId, plink);
        return plink;
    }

    public PartnerLinkDAO getDeployedEndpointReference(int plinkModelId) {
        return _plinks.get(plinkModelId);
    }

    public void setActive(boolean active) {
        _active = active;
    }

    public boolean isActive() {
        return _active;
    }

    public void addCorrelator(String correlator) {
        CorrelatorDaoImpl corr = new CorrelatorDaoImpl(correlator);
        _correlators.put(corr.getCorrelatorId(), corr);
    }

    public void setCompiledProcess(byte[] cbp) {
        _compiledProcess = cbp;
    }

    public byte[] getCompiledProcess() {
        return _compiledProcess;
    }

    /**
     * Nothing to do.
     * @see org.apache.ode.bpel.dao.ProcessConfigurationDAO#update()
     */
    public void update() {
        //TODO Check requirement for persisting.
    }
}
