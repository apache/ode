/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.itests.soap_to_bpel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.File;

import java.net.URL;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.ode.soap.SoapServiceConstants;
import org.apache.ode.httpsoap.HttpSoapInteraction;
import org.apache.ode.httpsoap.HttpSoapInteraction;
import org.apache.ode.httpsoap.HttpSoapInteraction;
import org.apache.ode.httpsoap.HttpSoapInteraction;


/**
 * TestBpelWithSoap
 *
 * @author jguinney
 */
public class TestBpelWithSoap extends junit.framework.TestCase
  implements org.apache.ode.bpel.util.deployment.ServiceProviderInfoCallback {
  private File _sarFile;
  private URL _wsdl;
  private URL _bpel;
  private String _soapServiceUri;
  private String _bpelServiceUri;
  private String _echoServiceUri;
  private org.apache.ode.sfwk.impl.DomainNodeImpl dNode;

  public TestBpelWithSoap(String arg0) {
    super(arg0);
  }

  // ----- ServiceProviderInfoCallback impl
  public String getBpelServiceProviderURI(String processName) {
    return _bpelServiceUri;
  }

  /**
   * DOCUMENTME
   *
   * @param procesName DOCUMENTME
   * @param partner DOCUMENTME
   * @param role DOCUMENTME
   * @param isMyRole DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws IllegalArgumentException DOCUMENTME
   */
  public org.apache.ode.bpel.util.deployment.ServiceProviderInfo getInfo(String procesName,
                                                                     String partner,
                                                                     String role,
                                                                     boolean isMyRole) {
    Properties p = new Properties();
    p.put(SoapServiceConstants.CONCRETE_WSDL_URL, _wsdl.toExternalForm());
    p.put(SoapServiceConstants.SERVICE_QNAME,
          new javax.xml.namespace.QName("http://loans.org/wsdl/loan-approval",
                                        "LoanService").toString());
    p.put(SoapServiceConstants.PORT_NAME, "LoanPort");

    if (partner.startsWith("customer")) {
      return new org.apache.ode.bpel.util.deployment.ServiceProviderInfo(_soapServiceUri,
                                                                     p);
    } else if (partner.equals("echoer")) {
      return new org.apache.ode.bpel.util.deployment.ServiceProviderInfo(_echoServiceUri,
                                                                     p);
    } else {
      throw new IllegalArgumentException("unknown partner '" + partner + "'");
    }
  }

  /**
   * DOCUMENTME
   *
   * @throws Exception DOCUMENTME
   */
  public void testReceiveCorrelation()
                              throws Exception {
    org.apache.ode.sfwk.cci.ServiceProviderConnectionFactory spCF = org.apache.ode.sfwk.cci.ServiceProviderConnectionFactory
                                                                .newInstance(org.apache.ode.sfwk.cci.ServiceProviderConnectionFactory.BINDING_MEM);
    org.apache.ode.sfwk.cci.ServiceProviderConnection conn = spCF.getConnection(new org.apache.ode.sfwk.DomainUUID("DOMAIN:"
                                                                                                           + getClass().getName()),
                                                                            _soapServiceUri,
                                                                            null);
    String[] systems = conn.getSystems();
    String[] svcs = conn.getActiveServices(systems[0]);

    junit.framework.Assert.assertEquals(2, svcs.length);

    final HttpSoapInteraction invokeA = (HttpSoapInteraction)conn
                                         .getServiceProviderAPI(systems[0],
                                                                "customerA.InboundService");
    final HttpSoapInteraction invokeB = (HttpSoapInteraction)conn
                                         .getServiceProviderAPI(systems[0],
                                                                "customerB.InboundService");

    for (int j = 0; j < 10; ++j) {
      Random r = new Random();
      ArrayList nums = new ArrayList();

      for (short i = 0; i < 9; ++i)
        nums.add(Short.valueOf(i));

      short[] order = new short[9];

      for (int i = 0; (nums.size() > 0); ++i) {
        Short s = (Short)nums.remove(r.nextInt(nums.size()));
        order[i] = s.shortValue();
      }

      for (int i = 0; i < 9; ++i) {
        HttpSoapInteraction invoke = null;
        javax.xml.soap.SOAPMessage msg = null;

        switch (order[i]) {
        case 0:
        case 1:
        case 2:
          msg = makeMsg("smith", 1, "soap-request-1.xml");
          invoke = invokeA;

          break;

        case 3:
        case 4:
        case 5:
          msg = makeMsg("smith", 1, "soap-request-2.xml");
          invoke = invokeB;

          break;

        case 6:
          msg = makeMsg("jones", 1, "soap-request-2.xml");
          invoke = invokeB;

          break;

        case 7:
          msg = makeMsg("smith", 2, "soap-request-2.xml");
          invoke = invokeB;

          break;

        case 8:
          msg = makeMsg("smith", 2, "soap-request-2.xml");
          invoke = invokeB;

          break;
        }

        invoke.sendSoapMessage(msg, 0);
      }

      org.apache.ode.bpel.runtime.dao.mem.StateStoreConnectionFactoryImpl ssc = new org.apache.ode.bpel.runtime.dao.mem.StateStoreConnectionFactoryImpl();
      String[] stores = ssc.getStateStores();
      assert stores.length == 1;

      org.apache.ode.bpel.runtime.dao.mem.ProcessDaoImpl process = (org.apache.ode.bpel.runtime.dao.mem.ProcessDaoImpl)ssc
                                                               .getProcess(stores[0],
                                                                           "loanApprovalProcess");

      for (int i = 0; i < order.length; ++i) {
        System.out.print(order[i] + " ");
      }

      System.out.println();

      while (true) {
        Thread.sleep(10000);

        int completed = 0;
        int active = 0;

        for (Iterator iter = process.getInstances()
                                    .iterator(); iter.hasNext();) {
          org.apache.ode.bpel.runtime.dao.ProcessInstanceDAO instance = (org.apache.ode.bpel.runtime.dao.ProcessInstanceDAO)iter
                                                                    .next();
          int state = instance.getState();

          if (state == org.apache.ode.bpel.runtime.dao.ProcessInstanceDAO.STATE_COMPLETED) {
            completed++;
          } else if (state == org.apache.ode.bpel.runtime.dao.ProcessInstanceDAO.STATE_ACTIVE) {
            active++;
          }
        }

        if (completed == 3) {
          if (active != 0) {
            // problem, spit out order
            System.out.println("ERROR!");
          }

          junit.framework.Assert.assertEquals(0, active);
          junit.framework.Assert.assertEquals(4, process.getInstances().size());

          break;
        }
      }

      process.flushCompleted();
      System.out.println("test completed correctly");
    }

    System.out.println("All tests completed.");
  }

  protected void setUp()
                throws Exception {
    _sarFile = File.createTempFile(getClass().getName(), ".jar");
    _wsdl = getClass()
              .getResource("LoanApprovalService.wsdl");
    _bpel = getClass()
              .getResource("test.bpel");
    _soapServiceUri = "uri:soap:" + getClass()
                                      .getName();
    _bpelServiceUri = "uri:bpel:" + getClass()
                                      .getName();
    _echoServiceUri = "uri:echo:" + getClass()
                                      .getName();

    org.apache.ode.bpel.util.deployment.BpelServiceGenerator gen = org.apache.ode.bpel.util.deployment.BpelServiceGenerator
                                                               .newGenerator(this);
    gen.generateSar(_sarFile, _bpel, _wsdl);

    Properties domainProps = new Properties();
    domainProps.put("ode.config.domainUUID", "DOMAIN:" + getClass().getName());
    domainProps.put("ode.config.DomainStateConnectionFactory",
                    org.apache.ode.sfwk.impl.dao.mem.DeploymentStoreDConnectionFactoryImpl.class
                    .getName());

    domainProps.put("ode.config.provider.sp1.uri", _soapServiceUri);
    domainProps.put("ode.config.provider.sp1.class",
                    org.apache.ode.adapters.httpsoap.HttpSoapAdapterInbound.class.getName());

    domainProps.put("ode.config.provider.sp2.uri", _bpelServiceUri);
    domainProps.put("ode.config.provider.sp2.class",
                    org.apache.ode.bpel.provider.BpelServiceProvider.class.getName());

    domainProps.put("ode.config.provider.sp3.uri", _echoServiceUri);
    domainProps.put("ode.config.provider.sp3.class",
                    org.apache.ode.soap.echo.EchoClientProvider.class.getName());

    org.apache.ode.sfwk.impl.config.DomainConfig cfg = new org.apache.ode.sfwk.impl.config.DomainConfig(domainProps,
                                                                                                getClass().getClassLoader());
    org.apache.ode.sfwk.bindings.mem.MemDomainNode memDomainNode = new org.apache.ode.sfwk.bindings.mem.MemDomainNode(cfg);
    dNode = memDomainNode.getDomainNode();

    org.apache.ode.sfwk.mngmt.ISystem system = dNode.getIDomain()
                                                .deploySystem(new org.apache.ode.sfwk.deployment.SarDeploymentBundle(_sarFile));
    system.activate();
    dNode.bindAll();
  }

  protected void tearDown()
                   throws Exception {
    super.tearDown();
  }

  private javax.xml.soap.SOAPMessage makeMsg(String name, int amount,
                                             String file)
                                      throws Exception {
    javax.xml.soap.MessageFactory mf = javax.xml.soap.MessageFactory
                                       .newInstance();
    javax.xml.soap.MimeHeaders mh = new javax.xml.soap.MimeHeaders();
    mh.addHeader("Content-Type", "text/xml");

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);

    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.parse(getClass().getResourceAsStream(file));
    Element e1 = (Element)doc.getElementsByTagName("name")
                             .item(0);
    ((Text)e1.getChildNodes()
             .item(0)).setData(name);

    Element e2 = (Element)doc.getElementsByTagName("amount")
                             .item(0);
    ((Text)e2.getChildNodes()
             .item(0)).setData(Integer.toString(amount));

    String s = org.apache.ode.utils.DOMUtils.domToString(doc.getDocumentElement());

    return mf.createMessage(mh, new ByteArrayInputStream(s.getBytes()));
  }
}
