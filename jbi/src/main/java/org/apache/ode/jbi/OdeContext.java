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

import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.scheduler.quartz.QuartzSchedulerImpl;
import org.apache.ode.jbi.msgmap.Mapper;
import org.apache.ode.utils.DOMUtils;

/**
 * Encapsulation of all the junk needed to get the BPEL engine running.
 * 
 * @author mszefler
 */
final class OdeContext {

  private static final Log __log = LogFactory.getLog(OdeContext.class);
  
  /** static singleton */
  private static OdeContext __self;

  private ComponentContext _context;

  private Map<QName, Document> _descriptorCache = new ConcurrentHashMap<QName, Document>();

  /** Ordered list of messsage mappers. */
  private ArrayList<Mapper> _mappers = new ArrayList<Mapper>();

  /** Mapper by class name. */
  private Map<String,Mapper> _mappersByClassName = new HashMap<String,Mapper>();

  OdeConsumer _consumer = new OdeConsumer(this);

  JbiMessageExchangeProcessor _jbiMessageExchangeProcessor = new JbiMessageExchangeEventRouter(
      this);

  BpelServerImpl _server;

  EndpointReferenceContextImpl _eprContext;

  MessageExchangeContextImpl _mexContext;

  QuartzSchedulerImpl _scheduler;

  ExecutorService _executorService;

  BpelDAOConnectionFactory _daocf;

  OdeConfigProperties _config;

  DataSource _dataSource;

  /** Mapping of OdeServiceId to OdeService */
  private Map<QName, OdeService> _activeOdeServices = new ConcurrentHashMap<QName, OdeService>();

  /** Mapping of JbiServiceName to OdeService */
  private Map<QName, OdeService> _activeJbiServices = new ConcurrentHashMap<QName, OdeService>();


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
  public static synchronized OdeContext getInstance() {
    if (__self == null) {
      synchronized (OdeContext.class) {
        if (__self == null) {
          __self = new OdeContext();
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

  public MyEndpointReference activateEndpoint(QName pid, QName odeServiceId,
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
    
    OdeService service = new OdeService(this, odeServiceId, jbiServiceName, jbiEndpointName);
    MyEndpointReference myepr = new MyEndpointReference(service);
    service.activate();
    _activeOdeServices.put(odeServiceId, service);
    _activeJbiServices.put(jbiServiceName, service);
    return myepr;

  }

  public void deactivateEndpoint(MyEndpointReference epr) throws Exception {
    OdeService svc = _activeOdeServices.remove(epr.getService().getOdeServiceId());
    _activeJbiServices.remove(epr.getService().getJbiServiceName());
    if (svc != null) {
      svc.deactivate();
    }
  }

  public OdeService getService(QName odeServiceId) {
	    return _activeOdeServices.get(odeServiceId);
	  }

  public OdeService getServiceByServiceName(QName jbiServiceName) {
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
