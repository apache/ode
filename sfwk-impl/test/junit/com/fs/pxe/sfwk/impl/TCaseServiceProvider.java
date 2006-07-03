/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.pxe.sfwk.spi.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Dummy Service Provider used with {@link TestDomainNode}.
 */
public class TCaseServiceProvider implements ServiceProvider {
  private static final Log __log = LogFactory.getLog(TCaseServiceProvider.class);

  static final Map<String, ServiceProvider> PROVIDERS = new HashMap<String, ServiceProvider>();

  ServiceProviderContext _context;

  Set<ServiceContext> _services = new HashSet<ServiceContext>();

  static final List<ServiceEvent> _serviceEvents = new ArrayList<ServiceEvent>();
  static final List<String> _history = new Vector<String>();
  static final List<Element> _rcvd = new ArrayList<Element>();

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws com.fs.pxe.sfwk.spi.ServiceProviderException DOCUMENTME
   */
  public boolean isRunning()
                    throws ServiceProviderException {
    return true;
  }

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public Class getServiceProviderAPI() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * DOCUMENTME
   *
   * @param service DOCUMENTME
   *
   * @throws com.fs.pxe.sfwk.spi.ServiceProviderException DOCUMENTME
   */
  public void activateService(ServiceContext service)
                       throws ServiceProviderException {
    __log.info("activateService service=" + service);

    _history.add("+M");
    try {
      Thread.sleep(300);
      _services.add(service);
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    } finally {
      _history.add("-M");
    }

  }

  public InteractionHandler createInteractionHandler(Class interactionClass)
                                              throws ServiceProviderException {
    return new InteractionImpl();
  }

  public void deactivateService(ServiceContext service)
                         throws ServiceProviderException {
    __log.info("deactivateService service=" + service);
    _history.add("+M");
    try {
      Thread.sleep(300);
      _services.remove(service);
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    } finally {
      _history.add("-M");
    }
  }

  public void deployService(ServiceConfig service)
                     throws ServiceProviderException {
    __log.info("deployService service=" + service);
    _history.add("+M");
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    } finally {
      _history.add("-M");
    }
  }

  public void initialize(ServiceProviderContext context)
                  throws ServiceProviderException {
    _context = context;
    __log.info("initialize uri=" + _context.getProviderURI());
    PROVIDERS.put(_context.getProviderURI(), this);
  }

  public void start()
             throws ServiceProviderException {
    __log.info("start");
  }

  public void stop()
            throws ServiceProviderException {
    __log.info("stop");
  }

  public void undeployService(ServiceConfig service)
                       throws ServiceProviderException {
    __log.info("deployService service=" + service);
    _history.add("+M");
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    } finally {
      _history.add("-M");
    }
  }

  public void onServiceEvent(ServiceEvent serviceEvent)
          throws ServiceProviderException {
    __log.info("onServiceEvent: " + serviceEvent);
    _history.add("+W");
    try {
      _serviceEvents.add(serviceEvent);
      Thread.sleep(100);
      if (serviceEvent instanceof MessageExchangeEvent)
        onMessageExchangeEvent((MessageExchangeEvent) serviceEvent);
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    } finally {
      _history.add("-W");
    }
  }


  private void onMessageExchangeEvent(MessageExchangeEvent mexevent) {
    try {
      switch (mexevent.getEventType()) {
        case MessageExchangeEvent.IN_RCVD_EVENT:
          Message output = mexevent.getMessageExchange().createOutputMessage();
          output.setPart("return", "bar");
          mexevent.getMessageExchange().output(output);
          break;
        case MessageExchangeEvent.OUT_RCVD_EVENT:
          Message rcvd =mexevent.getMessageExchange().lastOutput();
          _rcvd.add(rcvd.getMessage());
          break;
      }
    } catch (MessageExchangeException mex) {
      mex.printStackTrace();
    }
  }

  private class InteractionImpl implements TCaseInteraction, InteractionHandler {
    public void testEchoString() {
      try {
        _context.getTransactionManager().begin();
        ServiceContext svc = _services.iterator().next();
        MessageExchange mex = svc.createMessageExchange(svc.getImports()[0],null,null,"echoString");
        Message msg = mex.createInputMessage();
        msg.setPart("inputString","foo");
        mex.input(msg);
        _context.getTransactionManager().commit();
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }

    public void close() {
      // nothing to do
    }
  }

	/**
	 * @see com.fs.pxe.sfwk.spi.ServiceProvider#getProviderURI()
	 */
	public String getProviderURI() {
		return _context.getProviderURI();
	}

}
