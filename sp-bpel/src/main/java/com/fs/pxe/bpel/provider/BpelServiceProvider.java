/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.provider;

import com.fs.pxe.bpel.dao.BpelDAOConnectionFactory;
import com.fs.pxe.bpel.engine.BpelServerImpl;
import com.fs.pxe.bpel.iapi.Message;
import com.fs.pxe.bpel.iapi.MyRoleMessageExchange;
import com.fs.pxe.bpel.pmapi.BpelManagementFacade;
import com.fs.pxe.bpel.scheduler.quartz.QuartzSchedulerImpl;
import com.fs.pxe.sfwk.spi.*;
import com.fs.utils.msg.MessageBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;


/**
 * The BPEL service provider implementation.
 * TODO: Messages are hardcoded, fix...
 */
public class BpelServiceProvider implements ServiceProvider {

  /** Extension for bpel process deployment descriptor. */
  public static final String DD_EXTENSION = ".dd";

  private static final Log __log = LogFactory.getLog(BpelServiceProvider.class);
  private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

  /**
   * Cache of deployed services (so we don't need to go to deployment store each time)
   */
  private final HashMap<String,BpelService> _activeServices = new HashMap<String,BpelService>();
  private final HashMap<QName,BpelService> _activeServicesByProcessId = new HashMap<QName,BpelService>();

  private ServiceProviderContext _spContext;
  private BpelServerImpl _server;

  BpelDAOConnectionFactory _bpelDAOCF;

  EndpointReferenceContextImpl _eprContext;

  MessageExchangeContextImpl _mexContext;

  QuartzSchedulerImpl _scheduler;

  
  private String _mds = "pxe-ds";
  

  private String _stateStoreConnectionFactory;

  private DataSource _dataSource;
  
  public boolean isRunning() throws ServiceProviderException {
    return true;
  }

  public void activateService(ServiceContext service) throws ServiceProviderException {
    if (__log.isDebugEnabled())
      __log.debug("activateService: " + service);

    BpelService ase = findBpelService(service);

    if (ase != null) {
      return;
    }

    if (__log.isDebugEnabled()) {
      __log.debug("service not active, creating new entry.");
    }

    ase = new BpelService(
            this,
            _server,
            genProcessId(service),
            service,
            _bpelDAOCF,
            _spContext.getTransactionManager(),
            _spContext.getMBeanServer());

    ase.start();
    _activeServices.put(service.getServiceUUID(), ase);
    _activeServicesByProcessId.put(genProcessId(service), ase);

  }

  BpelService findBpelService(ServiceContext service) {
    return findBpelService(service.getServiceUUID());
  }

  BpelService findBpelService(String serviceUUID) {
    return _activeServices.get(serviceUUID);
  }

  BpelService findBpelServiceByProcessId(QName procId){
    return _activeServicesByProcessId.get(procId);
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#getProviderURI()
   */
  public String getProviderURI() {
    return _spContext.getProviderURI();
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#createInteractionHandler(Class)
   * @param interactionClass
   */
  public Object createInteractionHandler(Class interactionClass)
                                              throws ServiceProviderException {
    return _server.getBpelManagementFacade();
  }


  public void onServiceEvent(ServiceEvent serviceEvent) throws ServiceProviderException, MessageExchangeException {
    BpelService service = findBpelService(serviceEvent.getTargetService());
    if (service == null) {
      String errmsg = "Received ServiceEvent for unknown service: " + serviceEvent;
      __log.error(errmsg);
      throw new ServiceProviderException(errmsg);
    }
    service.onServiceEvent(serviceEvent);
  }


  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#deactivateService(com.fs.pxe.sfwk.spi.ServiceContext)
   */
  public void deactivateService(ServiceContext service)
                         throws ServiceProviderException {
    if (__log.isDebugEnabled()) {
      __log.debug("deactivateService: " + service);
    }

    BpelService ase = findBpelService(service);

    if(ase != null)	{
      ase.stop();
    }
    _activeServices.remove(service.getServiceUUID());
    _activeServicesByProcessId.remove(genProcessId(service));
  }

  public void deployService(ServiceConfig service) throws ServiceProviderException {
    if (__log.isDebugEnabled()) {
      __log.debug("deployService: " + service.getServiceName());
    }

    File dd = genDeployDDFile(service);
    if (!dd.exists()) {
      String errmsg = __msgs.msgBpelDeploymentDescriptorNotFound(dd.toString(),service.getServiceName());
      __log.error(errmsg);
      throw new ServiceProviderException(errmsg);
      
    }
    
    QName procName = genProcessId(service);
    try {
      _server.deploy(procName,dd.toURI());
    } catch (Exception ex) {
      String errmsg = __msgs.msgBpelDeployFailure(dd.toString(),service.getServiceName());
      __log.error(errmsg,ex);
      throw new ServiceProviderException(errmsg);
    }

  }

  private File genDeployDDFile(ServiceConfig service) {
    File dd = new File(service.getDeployDir(), service.getServiceName() + DD_EXTENSION);
    return dd;
  }

  static QName genProcessId(ServiceConfig service) {
    return new QName(null, service.getSystemName() + "." + service.getServiceName());
  }

  /* (non-Javadoc)
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#initialize(com.fs.pxe.sfwk.spi.ServiceProviderConfig)
   */
  public void initialize(ServiceProviderContext context)
                  throws ServiceProviderException {
    if (__log.isDebugEnabled()) {
      __log.debug("initializing");
    }

    _spContext = context;
    configureStateStoreFactory();
    _server = new BpelServerImpl();
    // We don't want the server to automatically activate deployed processes,
    // we'll do that explcitly
    _server.setAutoActivate(false);
    _eprContext = new EndpointReferenceContextImpl(this);
    _mexContext = new MessageExchangeContextImpl(this);
    _scheduler = new QuartzSchedulerImpl();
    _scheduler.setBpelServer(_server);
    _scheduler.setExecutorService(_spContext.getExeuctorService(), 5);
    _scheduler.setTransactionManager(context.getTransactionManager());
    _scheduler.setDataSource(_dataSource);

    _scheduler.init();
    
    _server.setDaoConnectionFactory(_bpelDAOCF);
    _server.setEndpointReferenceContext(_eprContext);
    _server.setMessageExchangeContext(_mexContext);
    _server.setScheduler(_scheduler);
    _server.init();
  }

  /* (non-Javadoc)
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#start()
   */
  public void start()
             throws ServiceProviderException {
    if (__log.isDebugEnabled()) {
      __log.debug("starting, active services=" + _activeServices);
    }
    
    _server.start();

  }

  /**
   * @see ServiceProvider#stop
   */
  public void stop()
            throws ServiceProviderException {
    if (__log.isDebugEnabled()) {
      __log.debug("stopping, active services=" + _activeServices);
    }

    _server.stop();
  }

  public void undeployService(ServiceConfig service)
                       throws ServiceProviderException {
    if (__log.isDebugEnabled()) {
      __log.debug("undeployService: " + service);
    }

    QName procName = genProcessId(service);
    
    try {
      _server.undeploy(procName);
    }catch(Exception e){
      String errmsg = __msgs.msgBpelUndeployFailure(service.getServiceName());
      __log.error(errmsg,e);
      throw new ServiceProviderException(errmsg,e);
    }
  }


  

  private void configureStateStoreFactory()
                                   throws ServiceProviderException {
    if (__log.isTraceEnabled()) {
      __log.trace("configureStateStoreFactory");
    }
    
    try {
      InitialContext ctx = new InitialContext();
      _dataSource = (DataSource) ctx.lookup(_mds);
      ctx.close();
    } catch (Exception ex) {
      String errmsg =__msgs.msgBpelDatabaseNotFound(_mds);
      __log.error(errmsg,ex);
      throw new ServiceProviderException(errmsg,ex);
    }
    
    if (_stateStoreConnectionFactory == null) {
      String errmsg = __msgs.msgBpelStateStorePropertyNotSet();
      __log.error(errmsg);
      throw new ServiceProviderException(errmsg);
    }

    try {
      InitialContext ctx = new InitialContext();
      _bpelDAOCF = (com.fs.pxe.bpel.dao.BpelDAOConnectionFactory) ctx.lookup(_stateStoreConnectionFactory);
      ctx.close();
    } catch (Exception ex) {
      String msg = __msgs.msgBpelStateStoreFactoryNotFound(_stateStoreConnectionFactory);
      __log.error(msg);
      throw new ServiceProviderException(msg, ex);
    }

    try {
      _bpelDAOCF.init(new Properties());
    } catch (Exception e) {
      String msg = __msgs.msgBpelStateStoreFactoryInstantiationErr(_stateStoreConnectionFactory);
      __log.error(msg);
      throw new ServiceProviderException(msg, e);
    }
  }

  BpelManagementFacade getBpelManagementFacade() {
    return _server.getBpelManagementFacade();
  }

  void handleBpelResponse(MessageExchange sfwkMex, MyRoleMessageExchange mex) {
    try {
    switch (mex.getStatus()) {
    case ASYNC:
      break;
    case ONE_WAY:
      // We're done.
      sfwkMex.release();
      mex.complete();
      break;
    case RESPONSE: {
      Message response = mex.getResponse();
      com.fs.pxe.sfwk.spi.Message sfwkResponse;
      try {
        sfwkResponse = sfwkMex.createOutputMessage();
        sfwkResponse.setMessage(response.getMessage());
        sfwkMex.output(sfwkResponse);
      } catch (Exception ex) {
        __log.error("Error setting response.", ex);
        sfwkMex.failure("MessageFormat?");
      }
      mex.complete();
    }
      break;
    case FAILURE: {
      // TODO: get failure descriptions.
      sfwkMex.failure("Failure.");
      mex.complete();
    }
      break;

    case FAULT: {
      Message response = mex.getResponse();
      com.fs.pxe.sfwk.spi.Message sfwkResponse = sfwkMex.createOutputMessage();
      try {
      sfwkResponse.setMessage(response.getMessage());
      sfwkMex.outfault(mex.getFault(),
          sfwkResponse);
      } catch (Exception ex) {
        __log.error("Error setting response.", ex);
        sfwkMex.failure("MessageFormat?");

      }
      mex.complete();
    }
      break;
    default:
    // ERROR, should not be in any of the other states
    }

    }catch (MessageExchangeException ex) {
      
    }
   }

  public String getStateStoreConnectionFactory() {
    return _stateStoreConnectionFactory;
  }

  public void setStateStoreConnectionFactory(String stateStoreConnectionFactory) {
    _stateStoreConnectionFactory = stateStoreConnectionFactory;
  }
    
  
}
