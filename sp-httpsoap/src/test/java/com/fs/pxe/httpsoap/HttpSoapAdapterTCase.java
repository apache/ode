/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.httpsoap;

import com.fs.pxe.ra.PxeConnection;
import com.fs.pxe.sfwk.bapi.DomainConfigImpl;
import com.fs.pxe.sfwk.impl.mock.pconn.MockProtocolConnector;
import com.fs.pxe.sfwk.spi.*;
import com.fs.utils.DOMUtils;

import java.io.Serializable;
import java.net.URL;

import junit.framework.TestCase;

import org.w3c.dom.Element;

public class HttpSoapAdapterTCase extends TestCase
        implements MockProtocolConnector.ServiceEventHandler {
  protected static final String REQUEST_URI = "http://foo.bar/baz";
  protected static final String SP_URI = "uri:HttpSoapAdapterInbound";
  protected DomainConfigImpl config;
//  protected InMemServer _binding;
//  protected SimpleSystemDeploymentBundle sdd;
  protected PxeConnection conn;
  protected HttpSoapInteraction interaction;
  protected URL goodMessage;

  public void setUp() throws Exception {
    com.fs.pxe.daomem.InMemDAOStoreConnectionFactoryImpl.clear();
//    MockProtocolConnector.setMessageExchangeListenerFactory(this);
//    _binding = new InMemServer("HttpSoapAdapterTestDomain");
//    _binding.installServiceProvider(new ServiceProviderConfigImpl(SP_URI,
//            HttpSoapAdapter.class, new Properties()));
//
//    _binding.installServiceProvider(new ServiceProviderConfigImpl("uri:MockProtocolConnector",
//            MockProtocolConnector.class, new Properties()));
//
//    _binding.start();
//
//    sdd = new SimpleSystemDeploymentBundle();
//
//    SystemDescriptor sd = SystemDescriptorFactory.parseDescriptor(getClass().getResource("pxe-system.xml"),null, null, true);
//    sdd.setSystemDescriptor(sd);
//    ObjectName systemON = _binding.getDomainNode().getDomainAdminMBean().deploySystemBundle(sdd);
//    SystemAdminMBean system = (SystemAdminMBean) _binding.getDomainNode().resolve(systemON, SystemAdminMBean.class);
//    system.enable();

    goodMessage = getClass().getResource("testRequest.soap");
  }


  public void onMessageExchange(ServiceContext service, MessageExchangeEvent messageExchangeEvent)
          throws ServiceProviderException, MessageExchangeException {
    MessageExchange mex = messageExchangeEvent.getMessageExchange();
    Message output = mex.createOutputMessage();
    Element ret = DOMUtils.newDocument().createElementNS(null,"TestPart");
    ret.appendChild(ret.getOwnerDocument().createTextNode("foobar"));
    try {
      output.setPart("TestPart",ret);
      mex.output(output);
    } catch (MessageFormatException mfe) {
      mfe.printStackTrace();
      fail("MessageFormatException");
    }
  }

	public void onScheduledWorkEvent(ServiceContext service, Serializable payload) throws ServiceProviderException {
		throw new UnsupportedOperationException();
	}

}
