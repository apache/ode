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
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.Serializer;
import org.apache.ode.jbi.msgmap.Mapper;
import org.apache.ode.jbi.util.WSDLFlattener;
import org.apache.ode.scheduler.simple.SimpleScheduler;
import org.apache.ode.store.ProcessStoreImpl;
import org.w3c.dom.Document;

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
    private Map<String, Mapper> _mappersByClassName = new HashMap<String, Mapper>();

    OdeConsumer _consumer;

    JbiMessageExchangeProcessor _jbiMessageExchangeProcessor = new JbiMessageExchangeEventRouter(this);

    BpelServerImpl _server;

    EndpointReferenceContextImpl _eprContext;

    MessageExchangeContextImpl _mexContext;

    SimpleScheduler _scheduler;

    ExecutorService _executorService;

    BpelDAOConnectionFactory _daocf;

    OdeConfigProperties _config;

    DataSource _dataSource;

    ProcessStoreImpl _store;

    /** Mapping of Endpoint to OdeService */
    private Map<Endpoint, OdeService> _activeOdeServices = new ConcurrentHashMap<Endpoint, OdeService>();


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
                throw new RuntimeException(e);
            }
        }

        return chnl;
    }

    /**
     * Sets the Component context.
     *
     * @param ctx
     *            component context.
     */
    public void setContext(ComponentContext ctx) {
        _context = ctx;
    }

    public ComponentContext getContext() {
        return _context;
    }

    public static OdeContext getInstance() {
        synchronized (OdeContext.class) {
            if (__self == null) {
                __self = new OdeContext();
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

    public MyEndpointReference activateEndpoint(QName pid, Endpoint endpoint) throws Exception {
        if (__log.isDebugEnabled()) {
            __log.debug("Activate endpoint: " + endpoint);
        }

        OdeService service = new OdeService(this, endpoint);
        try {
            ProcessConf pc = _store.getProcessConfiguration(pid);
            Serializer ofh = new Serializer(pc.getCBPInputStream());
            OProcess compiledProcess = ofh.readOProcess();
            QName portType = null;
            for (Map.Entry<String, Endpoint> provide : pc.getProvideEndpoints().entrySet()) {
                if (provide.getValue().equals(endpoint)) {
                    OPartnerLink plink = compiledProcess.getPartnerLink(provide.getKey());
                    portType = plink.myRolePortType.getQName();
                    break;
                }
            }
            if (portType == null) {
                if (__log.isDebugEnabled()) {
                    __log.debug("Could not find PortType for endpoint");
                }
            } else {
                Definition def = pc.getDefinitionForService(endpoint.serviceName);
                if (def == null) {
                    __log.debug("Could not find definition for service: " + endpoint.serviceName);
                } else {
                    def = new WSDLFlattener(def).getDefinition(portType);
                    Document doc = WSDLFactory.newInstance().newWSDLWriter().getDocument(def);
                    addEndpointDoc(endpoint.serviceName, doc);
                }
            }
        } catch (Exception e) {
            __log.warn("Exception during endpoint activation", e);
        }
        MyEndpointReference myepr = new MyEndpointReference(service);
        service.activate();
        _activeOdeServices.put(endpoint, service);
        return myepr;

    }

    public void deactivateEndpoint(Endpoint endpoint) throws Exception {
        OdeService svc = _activeOdeServices.remove(endpoint);

        if (svc != null) {
            svc.deactivate();
        }
    }

    public OdeService getService(Endpoint endpoint) {
        return _activeOdeServices.get(endpoint);
    }

    public OdeService getService(QName serviceName) {
        for (Map.Entry<Endpoint,OdeService> e : _activeOdeServices.entrySet()){
            if (e.getKey().serviceName.equals(serviceName))
                return e.getValue();
        }
        return null;
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
}
