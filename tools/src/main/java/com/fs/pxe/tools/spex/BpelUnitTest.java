/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.spex;

import com.fs.pxe.bpel.capi.CompilationMessage;
import com.fs.pxe.bpel.capi.CompileListener;
import com.fs.pxe.bpel.compiler.BpelC;
import com.fs.pxe.bpel.o.OPartnerLink;
import com.fs.pxe.bpel.o.OProcess;
import com.fs.pxe.bpel.o.Serializer;
import com.fs.pxe.bpel.test.BpelTestDef;
import com.fs.pxe.bpel.test.IBpelInvoker;
import com.fs.pxe.bpel.test.IInvokerCallback;

import com.fs.pxe.ra.PxeConnection;
import com.fs.pxe.sfwk.deployment.SarFileBuilder;
import com.fs.pxe.sfwk.deployment.som.SystemDescriptor;
import com.fs.pxe.sfwk.deployment.som.impl.*;
import com.fs.pxe.sfwk.mngmt.DomainAdminMBean;
import com.fs.pxe.sfwk.mngmt.SystemAdminMBean;
import com.fs.pxe.sfwk.rr.RepositoryWsdlLocator;
import com.fs.pxe.sfwk.rr.ResourceRepository;
import com.fs.pxe.sfwk.rr.ResourceRepositoryBuilder;
import com.fs.pxe.sfwk.rr.URLResourceRepository;
import com.fs.pxe.tools.bpelc.RrWsdlFinder;
import com.fs.utils.DOMUtils;
import com.fs.utils.StreamUtils;
import com.fs.utils.fs.TempFileManager;
import com.fs.utils.xml.capture.XmlDependencyScanner;
import com.fs.utils.xsd.Duration;
import com.fs.utils.xsd.XMLCalendar;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.wsdl.xml.WSDLLocator;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;

/**
 * <p>
 * Simple framework for providing unit test functionality for BPEL process
 * scripts. Basic premise is that process has a synchronous receive/reply
 * enclosing the entire process. Assertions can be applied to the final response
 * data (as a series of XPath assertions) to determine correctness of test.
 * </p>
 * 
 * <p>
 * Limitations include complex workflow with arbitrary (indeterminate) responses
 * within flows (concurrent) execution. Best we can do is provide a canned
 * response for a partnerlink, operation tuple.
 * </p>
 * 
 */
public class BpelUnitTest extends TestCase {
  
  /**
   * Service Provider URI of the "TestCase" service provider. This service
   * provider merely reresents this test-case in the PXE domain.
   */
  static URI PARTNER_SPURI = newURI("uri:protocoladapter.bpel.unit-test");


  /**
   * Service Provider URI of the BPEL service provider.
   */
  static URI BPEL_SPURI = newURI("uri:bpelProvider");

  private static final Log __log = LogFactory.getLog(BpelUnitTest.class);
  
  /**
   * Test parameters. These include name of BPEL file, name of WSDL, etc...
   */
  private BpelTestDef _testDef;


  /**
   * The assertions that should be checked after the process completes.
   */
  private XPath[] _assertions;

  /**
   * The PXE connection allows us to communicate with service providers deployed
   * in PXE. This is how we ask the receive-reply service provider to create the
   * message exchanges that start the test.
   */
  private PxeConnection _conn;

  /**
   * PartnerLinkName->(OperationName->(MessageDef[]))
   */
  private Map<String, Map<String, List<BpelTestDef.MessageDef>>> _messages =
    new HashMap<String, Map<String, List<BpelTestDef.MessageDef>>>();

  /** CorrelationId->Response */
  private Map<String, String> _responses = new HashMap<String,String>();
  /** CorrelationId->Failure */
  private Map<String, String> _failures = new HashMap<String, String>();
  
  private Lock _responseLock = new ReentrantLock();
  private Condition _messageExchangeCompleted = _responseLock.newCondition();
  
  private Callback _callback;
  
  protected MBeanServerConnection _mbeanServer;
  
  protected DomainAdminMBean _domainMBean;
  private SystemAdminMBean _systemMBean;
  private String _systemName;
  private String _id;
  
  /**
   * Constructor for test case.
   * 
   * @param name
   *          name of the test case
   * @param test
   *          test parameters
   */
  public BpelUnitTest(String id, String name, BpelTestDef test, PxeConnection conn,
      DomainAdminMBean dmb,
      MBeanServerConnection mbeanServer) throws Exception  {
    super(name);
    _id = id;
    _testDef = test;
    _domainMBean = dmb;
    _mbeanServer = mbeanServer;
    _conn = conn;
    _callback = new Callback();
  }

  /**
   * Execute our test.
   * 
   * @see junit.framework.TestCase#runTest()
   */
  public void runTest() throws Exception {
    if (__log.isDebugEnabled()) {
      __log.debug("Starting test " + _systemName);
    }

    for (int i = 0; i < _testDef.receives.size(); ++i) {
      BpelTestDef.MessageDef msg = _testDef.receives.get(i);
      String svc = msg.partnerLink;
      IBpelInvoker listener = (IBpelInvoker)
        _conn.createServiceProviderSession(PARTNER_SPURI.toASCIIString(),IBpelInvoker.class);

      StringWriter sw = new StringWriter();
      FileReader fr = new FileReader(msg.inFile);
      StreamUtils.copy(sw, fr, 1024);
      fr.close();
      
      if (msg.delay != null)
        Thread.sleep(msg.delay*1000);
      
      // We invoke here, but we get responses asynchronously, through our
      // callback methods!
      listener.invokeBPEL(
          _callback, svc, i == 0 ? "initial" : ("#" +i), 
          msg.operation, patternSubstitution(sw.toString()));
    }

    long endTime = System.currentTimeMillis() + 60 * 1000;
    long ctime, wtime;
    
    
    _responseLock.lock();
    try {
      while((ctime = System.currentTimeMillis()) < endTime &&
          !_responses.containsKey("initial") && !_failures.containsKey("initial")) 
        _messageExchangeCompleted.await(endTime-ctime,TimeUnit.MILLISECONDS);
    } finally {
      _responseLock.unlock();
    }
  
    String initialRequestResponse = _responses.get("initial");

    assertNotNull("InitialResponseCheck", initialRequestResponse);
    for (int i = 0; i < _assertions.length; ++i) {
      assertTrue(_testDef.postChecks.get(i)+ "\nMESSAGE: " + initialRequestResponse, _assertions[i].booleanValueOf(DOMUtils.stringToDOM(initialRequestResponse)));
    }

    if (__log.isDebugEnabled()) {
      __log.debug("Completed test " + _systemName);
    }
  }

  /**
   * Setup our test. This involves creating a PXE system for the test and
   * deploying it to the in-memory PXE domain.
   * 
   * @see TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    
    if (!(_testDef.receives.size() > 0)) {
      throw new IllegalArgumentException("Must define at least one receive message");
    }

    _assertions = new XPath[_testDef.postChecks.size()];

    SimpleNamespaceContext ctx = new SimpleNamespaceContext(_testDef.namespaces);
    ctx.addNamespace("_pxe", "http://www.fivesight.com/pxe/message");
    
    for (int i = 0; i < _testDef.postChecks.size(); ++i) {
      _assertions[i] = new DOMXPath(_testDef.postChecks.get(i));
      _assertions[i].setNamespaceContext(ctx);
    }

    for (Iterator<BpelTestDef.MessageDef> i = _testDef.invokes.iterator(); i.hasNext(); ) {
      BpelTestDef.MessageDef mdef = i.next();
      Map<String, List<BpelTestDef.MessageDef>> map = _messages.get(mdef.partnerLink);
      if (map == null) {
        _messages.put(mdef.partnerLink, map =
          new HashMap<String, List<BpelTestDef.MessageDef>>());
      }
      List<BpelTestDef.MessageDef> invokes = map.get(mdef.operation);
      if (invokes == null) {
        map.put(mdef.operation, invokes = new ArrayList<BpelTestDef.MessageDef>());
      }
      invokes.add(mdef);
    }
    
    File sarFile = TempFileManager.getTemporaryFile("unit-test");

    // NOTE: We need to get canonical file names in order to
    // get well-behaved URLs out.
    URL bpel = _testDef.bpelFile.getCanonicalFile().toURL();
    URL wsdl = _testDef.wsdlFile.getCanonicalFile().toURL();

    __log.debug("Building deployment archive for test process " + bpel.toExternalForm());
    
    SystemDescriptor desc = generateSar(sarFile, bpel, wsdl);
    _systemName = desc.getName();

    ObjectName systemON = _domainMBean.deploySystem(sarFile.toURL().toExternalForm(), true);
    _systemMBean = getSystem(systemON);
    _systemMBean.enable();
  }

  /*
   * @see TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    if (_systemMBean != null)
      _systemMBean.undeploy();
  }


  /**
   * Create a SAR (system archive) for the test case. We need to create a SAR,
   * because that is what PXE uses as a deployment unit.
   * 
   * @param outputSar
   *          output "SAR" file
   * @param processUrl
   *          URL of the BPEL script
   * @param wsdlURL
   *          url URL of the WSDL
   * @throws Exception
   *           in case of errors
   */
  private SystemDescriptor generateSar(File outputSar, final URL processUrl,
      URL wsdlURL) throws Exception {
    
    // tmpCbp is the compiled BPEL process.
    File tmpCbp = TempFileManager.getTemporaryFile("bpelCompiled");
    File rrFile = TempFileManager.getTemporaryDirectory("rr");

    // First we need to create a resource repository (RR) with the WSDL,
    // Schema, and any other resources required by the BPEL process
    final ResourceRepositoryBuilder rrbuilder = new ResourceRepositoryBuilder(rrFile);
    
    // Scan the WSDL for dependencies.
    XmlDependencyScanner scanner = new XmlDependencyScanner();
    scanner.process(wsdlURL.toURI());
    if (scanner.getErrors().size() != 0) {
      __log.error("Dependency scan errors: " + scanner.getErrors());
      throw new RuntimeBpelUnitTestException("Dependency errors.");
    }
    
    for (URI uri : scanner.getURIs()) {
      rrbuilder.addURI(uri,uri.toURL());
    }

    rrbuilder.addURI(wsdlURL.toURI(), new ByteArrayInputStream(StreamUtils.read(wsdlURL)));
    
    // We create a PXE system descriptor, connecting the partner links
    // of the process to our mock service providers.
    ResourceRepository rr = new URLResourceRepository(rrFile.toURI());
    RepositoryWsdlLocator rrlocator = new RepositoryWsdlLocator(rr, wsdlURL.toURI());


    // Now we can use the BPEL compiler (BpelC) to compile the process.
    BpelC bc = BpelC.newBpelCompiler();
    
    CompileListener myListener = new CompileListener() {
      public void onCompilationMessage(CompilationMessage m) {
        if (m.severity >= CompilationMessage.ERROR) {
          __log.error("Compile Error: " + m.toErrorString());
          throw new RuntimeBpelUnitTestException(
              ((m.source != null) ? m.source : "") + " " + m.messageText);
        }
      }};
    
    // No need to pass rr to compiler, as it is going to resolve relative URIs.
    bc.setCompileListener(myListener);
    bc.setProcessWSDL(wsdlURL.toURI());
    bc.setOutputStream(new BufferedOutputStream(new FileOutputStream(tmpCbp)));
    bc.setWsdlFinder(new RrWsdlFinder(rr, processUrl.toURI()));
    bc.compile(processUrl);

    // We load the compiled BPEL process.
    OProcess oprocess;
    InputStream is = tmpCbp.toURL().openStream();
    try {
      Serializer ofh = new Serializer(is);
      oprocess = ofh.readOProcess();
    } finally {
      try {
        is.close();
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }

    SarFileBuilder sf = new SarFileBuilder();

    try {
      SystemDescriptor sd = generateSystem(oprocess, rrlocator);
      sd.setWsdlUri(new URI(wsdlURL.toExternalForm()));
  
      sf.setWSDLResourceRepository(rrFile);
      sf.setSystemDescriptor(sd);
      sf.addEntry("bpel.bar", tmpCbp.toURL());
      sf.validate();
      sf.pack(outputSar);
      return sd;
    } finally {
      rr.close();
      sf.cleanup();
    }
  }

  /**
   * Generate a PXE system descriptor ({@link SystemDescriptorImpl}) based on
   * the compiled process. This basically means that we create one BPEL service
   * for the BPEL process (that is, one service hosted by the BPEL service
   * provider), and services for each partner-link role defined in that process.
   * These partner-link role services are hosted by the "TestCase" service
   * provider. This service provider simply forwards the events to the test case
   * for processing, forming as bridge between JUnit and the PXE domain.
   * 
   * @param process
   *          compiled BPEL
   * @param wsdlLocator
   *          WSDL locator
   * 
   * @return a PXE system descriptor
   */
  private SystemDescriptorImpl generateSystem(
      OProcess process, WSDLLocator wsdlLocator) throws Exception {

    // Setup the basics of the PXE system deployment descriptor, including:
    // system name, "global" WSDL URI.
    SystemDescriptorImpl sd = new SystemDescriptorImpl();
    sd.setName(_id);
    sd.setWsdlUri(new URI(wsdlLocator.getBaseURI()));

    // Create a <service> element in the descriptor for the BPEL service.
    ServiceImpl svcBPEL = sd.newService();
    svcBPEL.setName("TestBPEL");
    svcBPEL.setProviderUri(BPEL_SPURI);
    PropertyImpl bpelUriProperty = new PropertyImpl();
    bpelUriProperty.setName("compiledProcess");
    bpelUriProperty.setValue("bpel.bar");
    svcBPEL.addProperty(bpelUriProperty);
    // Create a <service> element to represent each partner. The partners
    // are modeled using the MockPartnerServiceProvider
    for (Iterator<OPartnerLink> i = process.getAllPartnerLinks().iterator(); i.hasNext(); ) {
      OPartnerLink partnerLink = i.next();
      ServiceImpl svcPartner = sd.newService();
      svcPartner.setName(partnerLink.getName());
      svcPartner.setProviderUri(PARTNER_SPURI);

      if (partnerLink.myRoleName != null) {
        // Configure a prtBPEL to represent the "myRole" on the BPEL process.
        String channelName = partnerLink.name + "." + partnerLink.myRoleName;

        PortImpl prtBPEL = svcBPEL.newPort();
        prtBPEL.setName(channelName);
        prtBPEL.setType(partnerLink.myRolePortType.getQName());
        prtBPEL.setChannelRef(channelName);

        // Add the same prtBPEL configuration to the BPEL service.
        svcBPEL.addExportedPort(prtBPEL);


        PortImpl prtPartner = svcBPEL.newPort();
        prtPartner.setName("bpel");
        prtPartner.setType(partnerLink.myRolePortType.getQName());
        prtPartner.setChannelRef(channelName);
        // Add the prtBPEL to the partner service.
        svcPartner.addImportedPort(prtPartner);

        // Create the channel linking the above ports.
        ChannelImpl channel = sd.newChannel();
        channel.setName(channelName);
        sd.addChannel(channel);
      }

      if (partnerLink.partnerRoleName != null) {
        // Configure a prtBPEL to represent the "partnerRole" on the BPEL
        // process.
        String channelName = partnerLink.name + "." + partnerLink.partnerRoleName;

        PortImpl prtBPEL = svcBPEL.newPort();
        prtBPEL.setName(channelName);
        prtBPEL.setType(partnerLink.partnerRolePortType.getQName());
        prtBPEL.setChannelRef(channelName);
        svcBPEL.addImportedPort(prtBPEL);

        PortImpl prtPartner = svcBPEL.newPort();
        prtPartner.setName("self");
        prtPartner.setType(partnerLink.partnerRolePortType.getQName());
        prtPartner.setChannelRef(channelName);
        svcPartner.addExportedPort(prtPartner);

        // Create the channel linking the above ports.
        ChannelImpl channel = sd.newChannel();
        channel.setName(channelName);
        sd.addChannel(channel);
      }

      sd.addService(svcPartner);
    }

    sd.addService(svcBPEL);
    return sd;
  }
  
  class Callback extends UnicastRemoteObject implements IInvokerCallback {

		private static final long serialVersionUID = 1L;

    /**
     * @throws RemoteException
     */
		protected Callback() throws RemoteException {
			super();
		}

		/**
     * @see com.fs.pxe.bpel.test.IInvokerCallback#requestResponse(java.lang.String,
     *      java.lang.String)
     */
		public void requestResponse(String id, String data) {
      _responseLock.lock();
      try {
        _responses.put(id, data);
        _messageExchangeCompleted.signalAll();
      } finally {
        _responseLock.unlock();
      }
        
		}

    public void requestFailed(String cid,String description) {
      
      _responseLock.lock();
      try {
        _failures.put(cid, description);
        _messageExchangeCompleted.signalAll();
      } finally {
        _responseLock.unlock();
      }
    }
    
		/**
     * @see com.fs.pxe.bpel.test.IInvokerCallback#invokeResponse(java.lang.String,
     *      java.lang.String)
     */
		public Response invokeResponse(String partnerLink, String op) {
			BpelTestDef.MessageDef msgDef = null;
      try {
        Map<String, List<BpelTestDef.MessageDef>> opMap = _messages.get(partnerLink);
        List<BpelTestDef.MessageDef> msgs = opMap.get(op);
        msgDef = msgs.remove(0);
      } catch (Exception ex) {
        String msg = "TestCase configuration error, no message for <invoke> on " + partnerLink + " operation " + op;
        throw new RuntimeException(msg);
      }
      StringWriter sw = new StringWriter();
      try{
        FileReader fr = new FileReader(msgDef.inFile);
        StreamUtils.copy(sw, fr, 1024);
        fr.close();
      }catch(Exception e){
        throw new RuntimeException(e);
      }
      return new Response(msgDef.fault, sw.toString());
		}

  
  }
 
  private SystemAdminMBean getSystem(ObjectName system) throws Exception {
    return (SystemAdminMBean) MBeanServerInvocationHandler
            .newProxyInstance(_mbeanServer, system, SystemAdminMBean.class, false);
  }
  
  private static URI newURI(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
  
  private static String patternSubstitution(String s){
  	String REGEX = "((!!)(.*?)(!!))";
    Pattern p = Pattern.compile(REGEX);
    Matcher m = p.matcher(s);
    StringBuffer sb = new StringBuffer();
    int lastEnd = 0;
    while(m.find()){
     sb.append(s.substring(lastEnd, m.start()));
     lastEnd = m.end();
     sb.append(timeSubstitution(m.group(3)));
    }
    sb.append(s.substring(lastEnd, s.length()));
    return sb.toString();
  }
  
  private static String timeSubstitution(String s){
    XMLCalendar cal = new XMLCalendar(System.currentTimeMillis());
    Duration d = new Duration(s.trim());
    d.addTo(cal);
    return cal.toString();
  }
  
  public static void main(String[] args){
  	String s = "fox!!PT5S!!def!!PT10M!!tree";
    System.out.println(patternSubstitution(s));
  }
  
  public class RuntimeBpelUnitTestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    RuntimeBpelUnitTestException(String s) {
      super(s);
    }
  }
  
}

