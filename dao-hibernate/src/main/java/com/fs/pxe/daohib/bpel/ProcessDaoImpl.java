/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.bpel;

import com.fs.pxe.bpel.common.CorrelationKey;
import com.fs.pxe.bpel.dao.*;
import com.fs.pxe.daohib.SessionManager;
import com.fs.pxe.daohib.bpel.hobj.*;
import com.fs.pxe.daohib.hobj.HLargeData;
import com.fs.utils.DOMUtils;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * Hibernate-based {@link ProcessDAO} implementation.
 */
class ProcessDaoImpl extends HibernateDao implements ProcessDAO {
  private static final String QRY_CORRELATOR = "where this.correlatorId = ?";
  private static final String QRY_EPR = "where this.modelId =  ?";

  private HProcess _process;

  public ProcessDaoImpl(SessionManager sm, HProcess process) {
    super(sm,process);
    _process = process;
  }

  public QName getProcessId() {
    return QName.valueOf(_process.getProcessId());
  }

  public ProcessInstanceDAO getInstance(Long iid) {
    ProcessInstanceDAO instance = BpelDAOConnectionImpl._getInstance(_sm, getSession(), iid);
    if (instance == null || !instance.getProcess().getProcessId().equals(getProcessId()))
      return null;
    return instance;
  }


  public CorrelatorDAO getCorrelator(String  corrId) {

    Iterator results = null;
    Query q = getSession().createFilter(_process.getCorrelators(),
            QRY_CORRELATOR);
    results = q.setString(0, corrId).iterate();

    if(!results.hasNext()){
      String msg = "no such correlator: corrId = " + corrId;
      throw new IllegalArgumentException(msg);
    }

    try {
      return new CorrelatorDaoImpl(_sm, (HCorrelator)results.next());
    } finally {
      Hibernate.close(results);
    }
  }

  public void removeRoutes(String routeId, ProcessInstanceDAO target) {
    for (HCorrelator hCorrelator : _process.getCorrelators()) {
      new CorrelatorDaoImpl(_sm, (HCorrelator) hCorrelator).removeRoutes(routeId, target);
    }
  }

  public ProcessInstanceDAO createInstance(CorrelatorDAO correlator) {
    HProcessInstance instance = new HProcessInstance();
    instance.setInstantiatingCorrelator((HCorrelator)((CorrelatorDaoImpl)correlator).getHibernateObj());
    instance.setProcess(_process);
    instance.setCreated(new Date());
    getSession().save(instance);
    _process.getInstances().add(instance);

    return new ProcessInstanceDaoImpl(_sm,instance);
  }

  /**
   * @see com.fs.pxe.bpel.dao.ProcessDAO#findInstance(CorrelationKey)
   */
  @SuppressWarnings("unchecked")
  public Collection<ProcessInstanceDAO> findInstance(CorrelationKey ckeyValue) {

    Criteria criteria = getSession().createCriteria(HCorrelationSet.class);
    criteria.add(Expression.eq("scope.instance.process.id",_process.getId()));
    criteria.add(Expression.eq("value", CorrelationKeySerializer.toCanonicalString(ckeyValue)));
    criteria.addOrder(Order.desc("scope.instance.created"));
    return criteria.list();

  }

  /**
   * @see com.fs.pxe.bpel.dao.ProcessDAO#instanceCompleted(ProcessInstanceDAO)
   */
  public void instanceCompleted(ProcessInstanceDAO instance) {
    // nothing to do here (yet?)
  }

  /**
   * @see com.fs.pxe.bpel.dao.ProcessDAO#instanceCompleted(ProcessInstanceDAO)
   */
  public void setProperty(String name, String ns, Node content) {
    setProperty(name, ns, DOMUtils.domToStringLevel2(content), false);
  }
  /**
   * @see com.fs.pxe.bpel.dao.ProcessDAO#instanceCompleted(ProcessInstanceDAO)
   */
  public void setProperty(String name, String ns, String content) {
    setProperty(name, ns, content, true);
  }

  private void setProperty(String name, String ns, String content, boolean simple) {
    HProcessProperty existingProperty = getProperty(name, ns);
    if (existingProperty == null) {
      HProcessProperty property = new HProcessProperty();
      property.setName(name);
      property.setNamespace(ns);
      if (simple) property.setSimpleContent(content);
      else property.setMixedContent(content);
      _process.getProperties().add(property);
      property.setProcess(_process);
      getSession().save(property);
    } else {
      if (content == null) {
        getSession().delete(existingProperty);
        _process.getProperties().remove(existingProperty);
      } else {
        if (simple) existingProperty.setSimpleContent(content);
        else existingProperty.setMixedContent(content);
        getSession().save(existingProperty);
      }
    }
  }

  private HProcessProperty getProperty(String name, String ns) {
    HProcessProperty existingProperty = null;
    for (HProcessProperty hproperty : _process.getProperties()) {
      if (hproperty.getName().equals(name) && hproperty.getNamespace().equals(ns))
        existingProperty = hproperty;
    }
    return existingProperty;
  }

  /**
   * @see com.fs.pxe.bpel.dao.ProcessDAO#instanceCompleted(ProcessInstanceDAO)
   */
  public Collection<ProcessPropertyDAO> getProperties() {
    ArrayList<ProcessPropertyDAO> propDAOs =
            new ArrayList<ProcessPropertyDAO>(_process.getProperties().size());
    for (HProcessProperty hproperty : _process.getProperties()) {
      propDAOs.add(new ProcessPropertyDAOImpl(_sm, hproperty));
    }
    return propDAOs;
  }

  public Collection<PartnerLinkDAO> getDeployedEndpointReferences() {
    ArrayList<PartnerLinkDAO> eprDAOs = new ArrayList<PartnerLinkDAO>(_process.getDeploymentPartnerLinks().size());
    for (HPartnerLink hepr : _process.getDeploymentPartnerLinks()) {
      eprDAOs.add(new PartnerLinkDAOImpl(_sm, hepr));
    }
    return eprDAOs;
  }

  public void delete() {
    getSession().delete(_process);
  }

	public String getDeployer() {
		return _process.getDeployer();
	}

	public Date getDeployDate() {
		return _process.getDeployDate();
	}

	public QName getDefinitionName() {
		return QName.valueOf(_process.getType());
	}

	public int getVersion() {
		return _process.getVersion();
	}

  /**
   * @see com.fs.pxe.daohib.bpel.hobj.HProcess#isRetired()
   */
  public boolean isRetired() {
      return _process.isRetired();
  }

  /**
   * @see com.fs.pxe.daohib.bpel.hobj.HProcess#setRetired(boolean)
   */
  public void setRetired(boolean retired) {
      _process.setRetired(retired);
      update();
  }


  public void setActive(boolean active) {
    _process.setActive(active);
    update();
    
    
  }

  public boolean isActive() {
    return _process.isActive();
  }

  public void addCorrelator(String corrid) {
    HCorrelator correlator = new HCorrelator();
    correlator.setCorrelatorId(corrid);
    correlator.setProcess(_process);
    correlator.setLock(0);
    correlator.setCreated(new Date());
    _process.getCorrelators().add(correlator);
    getSession().save(correlator);
    getSession().saveOrUpdate(_process);
  }

  public void setDeployURI(URI dduri) {
    _process.setDeployURI(dduri.toString());
    update();
  }

  public URI getDeployURI() {
    try {
      return _process.getDeployURI() == null ? null : new URI(_process.getDeployURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException("Invalid deployment URI in DB for process " + _process.getProcessId());
    }
  }

  public void setCompiledProcess(byte[] cbp) {
    if (_process.getCompiledProcess() != null)
      getSession().delete(_process.getCompiledProcess());
    HLargeData x = new HLargeData(cbp);
    getSession().save(x);
    _process.setCompiledProcess(x);
  }

  public byte[] getCompiledProcess() {
    HLargeData x = _process.getCompiledProcess();
    return x == null ? null : x.getBinary();
  }

  public PartnerLinkDAO getDeployedEndpointReference(int plinkModelId) {
    Query q  = getSession().createFilter(_process.getDeploymentPartnerLinks(), QRY_EPR);
    q.setInteger(0,plinkModelId);
    HPartnerLink hepr = (HPartnerLink) q.uniqueResult();
    if (hepr == null)
      return null;
    return new PartnerLinkDAOImpl(_sm,hepr);
  }

  public PartnerLinkDAO addDeployedPartnerLink(int plinkModelId, String plinkName, 
      String myRoleName,
      String partnerRoleName) {
    HPartnerLink hepr = new HPartnerLink();
    hepr.setModelId(plinkModelId);
    hepr.setLinkName(plinkName);
    hepr.setMyRole(myRoleName);
    hepr.setPartnerRole(partnerRoleName);
    hepr.setProcess(_process);
    _process.getDeploymentPartnerLinks().add(hepr);
    getSession().save(hepr);
    update();
    return new PartnerLinkDAOImpl(_sm,hepr);
  }

}
