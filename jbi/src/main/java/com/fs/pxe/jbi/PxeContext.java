/*
 * File:      $Id: PxeContext.java 492 2006-01-02 16:12:09Z holger $
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.jbi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.NormalizedMessage;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fs.pxe.bpel.dao.BpelDAOConnectionFactory;
import com.fs.pxe.bpel.engine.BpelServerImpl;
import com.fs.pxe.bpel.iapi.EndpointReference;
import com.fs.pxe.bpel.scheduler.quartz.QuartzSchedulerImpl;
import com.fs.pxe.jbi.msgmap.Mapper;
import com.fs.utils.DOMUtils;

/**
 * Encapsulation of all the junk needed to get the BPEL engine running.
 * 
 */
public final class PxeContext {

  private static final Log __log = LogFactory.getLog(PxeContext.class);
  
  /** static singleton */
  private static PxeContext __self;

  private ComponentContext _context;

  private Map<QName, Document> _descriptorCache = new ConcurrentHashMap<QName, Document>();

  /** Ordered list of messsage mappers. */
  private ArrayList<Mapper> _mappers = new ArrayList<Mapper>();

  /** Mapper by class name. */
  private Map<String,Mapper> _mappersByClassName = new HashMap<String,Mapper>();

  public PxeConsumer _consumer = new PxeConsumer(this);

  public JbiMessageExchangeProcessor _jbiMessageExchangeProcessor = new JbiMessageExchangeEventRouter(
      this);

  public BpelServerImpl _server;

  public EndpointReferenceContextImpl _eprContext;

  public MessageExchangeContextImpl _mexContext;

  public QuartzSchedulerImpl _scheduler;

  public ExecutorService _executorService;

  public BpelDAOConnectionFactory _daocf;

  public PxeConfigProperties _config;

  public DataSource _dataSource;

  /** Mapping of PxeServiceId to PxeService */
  private Map<QName, PxeService> _activePxeServices = new ConcurrentHashMap<QName, PxeService>();

  /** Mapping of JbiServiceName to PxeService */
  private Map<QName, PxeService> _activeJbiServices = new ConcurrentHashMap<QName, PxeService>();


  public PxeContext() {
  }

  /**
   * Gets the delivery channel.
   * 
   * @return delivery channel
   */
  public DeliveryChannel getChannel() {
    DeliveryChannel chnl = null;

    if (_context != null) {
      try {
        chnl = _context.getDeliveryChannel();
      } catch (Exception e) {
        // TODO better error logging
        e.printStackTrace();
      }
    }

    return chnl;
  }

  /**
   * Sets the Component context.
   * 
   * @param ctx
   *          component context.
   */
  public void setContext(ComponentContext ctx) {
    _context = ctx;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return Transformation engine context
   */
  public ComponentContext getContext() {
    return _context;
  }

  /**
   * Used to grab a reference of this object.
   * 
   * @return an initialized TransformationEngineContext reference
   */
  public static synchronized PxeContext getInstance() {
    if (__self == null) {
      synchronized (PxeContext.class) {
        if (__self == null) {
          __self = new PxeContext();
        }
      }
    }

    return __self;
  }

  public void addEndpointDoc(QName svcname, Document df) {
    _descriptorCache.put(svcname, df);
  }

  public Document getServiceDescription(QName svcName) {
    return _descriptorCache.get(svcName);
  }

  public TransactionManager getTransactionManager() {
    return (TransactionManager) getContext().getTransactionManager();
  }

  public MyEndpointReference activateEndpoint(QName pid, QName pxeServiceId,
      Element externalEpr) throws Exception {
    QName jbiServiceName;
    String jbiEndpointName;

    if ( __log.isDebugEnabled() ) {
      __log.debug( "Activate endpoint: " + prettyPrint( externalEpr ) );
    }

    QName elname = new QName(externalEpr.getNamespaceURI(),externalEpr.getLocalName());
    if (elname.equals(EndpointReference.SERVICE_REF_QNAME)) {
        externalEpr = DOMUtils.getFirstChildElement(externalEpr);
        if ( externalEpr == null ) {
            throw new IllegalArgumentException( "Unsupported EPR: " + prettyPrint(externalEpr) );
        }
        elname = new QName(externalEpr.getNamespaceURI(),externalEpr.getLocalName());
    }
    
    // extract serviceName and endpointName from JBI EPR 
    if (elname.equals(EndpointReferenceContextImpl.JBI_EPR)) {
      String serviceName = externalEpr.getAttribute("service-name");
      jbiServiceName = EndpointReferenceContextImpl.convertClarkQName( serviceName );
      jbiEndpointName = externalEpr.getAttribute("end-point-name");
    } else {
      throw new IllegalArgumentException( "Unsupported EPR: " + prettyPrint(externalEpr) );
    }
    
    PxeService service = new PxeService(this, pxeServiceId, jbiServiceName, jbiEndpointName);
    MyEndpointReference myepr = new MyEndpointReference(service);
    service.activate();
    _activePxeServices.put(pxeServiceId, service);
    _activeJbiServices.put(jbiServiceName, service);
    return myepr;

  }

  public void deactivateEndpoint(MyEndpointReference epr) throws Exception {
    PxeService svc = _activePxeServices.remove(epr.getService().getPxeServiceId());
    _activeJbiServices.remove(epr.getService().getJbiServiceName());
    if (svc != null) {
      svc.deactivate();
    }
  }

  public PxeService getService(QName pxeServiceId) {
	    return _activePxeServices.get(pxeServiceId);
	  }

  public PxeService getServiceByServiceName(QName jbiServiceName) {
	    return _activeJbiServices.get(jbiServiceName);
	  }

  public Mapper findMapper(NormalizedMessage nmsMsg, Operation op) {
    ArrayList<Mapper> maybe = new ArrayList<Mapper>();

    for (Mapper m : _mappers) {
      Mapper.Recognized result = m.isRecognized(nmsMsg, op);
      switch (result) {
      case TRUE:
        return m;
      case FALSE:
        continue;
      case UNSURE:
        maybe.add(m);
        break;
      }
    }
    
    if (maybe.size() == 0)
      return null;
    if (maybe.size() == 1)
      return maybe.get(0);
    
    __log.warn("Multiple mappers may match input message for operation " + op.getName());
    // Get the first match.
    return maybe.get(0);
  }
  
  public Mapper getMapper(String name) {
    return _mappersByClassName.get(name);
  }
  
  public void registerMapper(Mapper mapper) {
    _mappers.add(mapper);
    _mappersByClassName.put(mapper.getClass().getName(), mapper);
  }
  
  public Mapper getDefaultMapper() {
    return _mappers.get(0);
  }

  private String prettyPrint( Element el ) {
      try {
          return DOMUtils.prettyPrint( el );
      } catch ( java.io.IOException ioe ) {
          return ioe.getMessage();
      }
  }
  
}
