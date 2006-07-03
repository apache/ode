/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package test.pxe;

import com.fs.pxe.bpel.bdi.breaks.ActivityBreakpoint;
import com.fs.pxe.bpel.bdi.breaks.Breakpoint;
import com.fs.pxe.bpel.common.InstanceQuery;
import com.fs.pxe.bpel.common.ProcessState;
import com.fs.pxe.bpel.jmx.ProcessMBean;
import com.fs.pxe.bpel.provider.BpelManagementFacade;
import com.fs.pxe.ra.PxeConnection;
import com.fs.pxe.ra.PxeConnectionFactory;
import com.fs.pxe.ra.PxeManagedConnectionFactory;
import com.fs.pxe.sfwk.mngmt.DomainAdminMBean;
import com.fs.pxe.xcontrib.sp.rpc.Request;
import com.fs.pxe.xcontrib.sp.rpc.Response;
import com.fs.pxe.xcontrib.sp.rpc.RpcInteraction;
import com.fs.utils.StreamUtils;
import com.fs.utils.rmi.RMIConstants;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import junit.framework.Assert;
import junit.framework.TestCase;


/**
 *
 * @author mstevens
 */
public abstract class Base extends TestCase {
  
  public Base(String testName) {
    super(testName);
  }

  private static ThreadLocal<PxeConnection> _pc =
    new ThreadLocal<PxeConnection>() {
      protected synchronized PxeConnection initialValue() {
        PxeManagedConnectionFactory pmcf = new PxeManagedConnectionFactory();
        try {
          pmcf.setURL(RMIConstants.getConnectionURL());
          PxeConnectionFactory cf =
            (PxeConnectionFactory)pmcf.createConnectionFactory();
          return (PxeConnection)cf.getConnection();
        } catch(Exception e) {
          System.out.println("Failure acquiring PxeConnection " + e);
          e.printStackTrace(System.out);
          return null;
        }
      }
    };
    
  public static class BpelManagementHelper {
    public BpelManagementFacade fascade;
    /**
     * format:
     *
     * SystemName.ServiceName
     */
    public String processID;
  }
  
  private static ThreadLocal<BpelManagementHelper> _im =
    new ThreadLocal<BpelManagementHelper>() {
      protected synchronized BpelManagementHelper initialValue() {
        try {
          PxeConnection pc = getPxeConnection();
          BpelManagementHelper helper = new BpelManagementHelper();
          helper.fascade = 
            (BpelManagementFacade)getPxeConnection().createServiceProviderSession(
              "uri:bpelProvider", BpelManagementFacade.class);
          return helper;
        } catch(Exception e) {
          System.out.println("Failure acquiring PxeConnection " + e);
          e.printStackTrace(System.out);
          return null;
        }
      }
    };
  
  private static ThreadLocal<RpcInteraction> _native = new ThreadLocal<RpcInteraction>() {
    protected synchronized RpcInteraction initialValue() {
      try {
        return (RpcInteraction)getPxeConnection().createServiceProviderSession(
          "uri:protocoladapter.native.inbound", RpcInteraction.class);
      } catch(Exception e) {
        System.out.println("Failure acquiring PxeConnection " + e);
        e.printStackTrace(System.out);
        return null;
      }
    }
  };
  
  protected static PxeConnection getPxeConnection() throws Exception {
    return _pc.get();
	}
  
	protected static BpelManagementHelper getBpelManagementHelper() throws Exception {
		return _im.get();
	}

	protected static RpcInteraction getNativeXmlInteraction() throws Exception {
    return _native.get();
	}
  
	protected BpelManagementHelper getBpelManagementHelper(String systemName, String serviceName)
     throws Exception {
    _im.get().processID = systemName + "." + serviceName;
		return _im.get();
	}
  
	protected BpelManagementHelper getHelloBpelManagement()
     throws Exception {
		return getBpelManagementHelper("HelloWorld", "helloWorld.BpelService");
	}
  
	protected BpelManagementHelper getHelloNativeBpelManagement()
     throws Exception {
    return getBpelManagementHelper(
      "HelloWorld_NativeProtocol", "helloWorld.BpelService");
	}
  
	protected BpelManagementHelper getAsyncNativeBpelManagement()
     throws Exception {
		return getBpelManagementHelper(
      "AsyncProcess_NativeProtocol", "ProcessSync.BpelService");
	}
  
  private static JMXConnector _connector;
  
  private static synchronized JMXConnector getJMXConnector() throws Exception {
    String jmxurl = System.getProperty("pxe.jmxurl");
    if (null == jmxurl) {
      jmxurl = "";
    }
      
    if (null == _connector) {
			JMXServiceURL jmxServURL = new JMXServiceURL(jmxurl);
			Map<String,Object> environment = new HashMap<String,Object>();
      
      String username = System.getProperty("pxe.jmxusername");
      if(username != null && username.trim().length()>0) {
        // using JMXConnector.CREDENTIALS
        String password = System.getProperty("pxe.jmxpassword");
        if(null == password)
          password = "";
        String[] credentials = new String[]
            { username , password };
        environment.put(JMXConnector.CREDENTIALS, credentials);
      }
			_connector = JMXConnectorFactory.connect(jmxServURL, environment);
    }
    return _connector;
  }
    
	private static MBeanServerConnection _connection;
  
	private static synchronized MBeanServerConnection getMBeanServerConnection()
    throws Exception {
    if(null ==_connection)
			_connection = getJMXConnector().getMBeanServerConnection();
    return _connection;
	}  

  
  protected boolean checkForSystem(String systemName) throws Exception {
		ObjectName name = ObjectName.getInstance(
      "com.fivesight.pxe:type=DomainAdmin,node=node0,domain=MyDomain");
		DomainAdminMBean mbean = (DomainAdminMBean)
			MBeanServerInvocationHandler.newProxyInstance(
        getMBeanServerConnection(),
				name, DomainAdminMBean.class, false);
			ObjectName[] names = mbean.getSystems();
			System.out.println("Systems:");
      boolean found = false;
			for (ObjectName sysName : names) {
        if(sysName.getKeyProperty("system").equals(systemName)) {
          found = true;
          break;
        }
			}
      return found;
  }

  private static Map<String,ProcessMBean> _processMBeans = new HashMap<String,ProcessMBean>();
  
  protected ProcessMBean getHelloProcessMBean() throws Exception {
    return getProcessMBean("HelloWorld", "helloWorld.BpelService","HelloWorld");
  }
  
  protected ProcessMBean getHelloNativeProcessMBean() throws Exception {
    return getProcessMBean("HelloWorld_NativeProtocol", "helloWorld.BpelService","HelloWorld_NativeProtocol");
  }
  
  protected ProcessMBean getAsyncNativeProcessMBean() throws Exception {
    return getProcessMBean("AsyncProcess_NativeProtocol", "ProcessSync.BpelService","AsyncProcess_NativeProtocol");
  }
  
  protected static synchronized ProcessMBean getProcessMBean(
    String systemName,
    String serviceName,
    String processName) throws Exception {
    //
    // according to PXE system conventions we should a unique service name per system
    //
    ObjectName name = ObjectName.getInstance(
      "com.fivesight.pxe:domain=MyDomain,node=node0,type=BPELProcessAdmin" +
      ",system=" + systemName +
      ",service=" + serviceName);
    System.out.println("Looking for " + name);
    if(null == _processMBeans.get(name.toString())) {
      System.out.println("Connecting to " + name);

      ProcessMBean process = (ProcessMBean)MBeanServerInvocationHandler.newProxyInstance(
        getMBeanServerConnection(),name, ProcessMBean.class, false);
      Assert.assertEquals("BPEL Process Defintion Name of MBean",processName,process.getName());
      _processMBeans.put(name.toString(), process);
    }
    return _processMBeans.get(name.toString());
  }
  
  protected void setUp() throws Exception {
    getBpelManagementHelper();
    getMBeanServerConnection();
  }
  
  protected void setUpHello() throws Exception {
    if(!checkForSystem("HelloWorld"))
      throw new Exception("PXE System HelloWorld needs to be deployed");
    if(null == getProcessMBean("HelloWorld", "helloWorld.BpelService", "HelloWorld"))
      throw new Exception("Cannot acquire 'HelloWorld' ProcessMBean for " +
        "'helloWorld.BpelService' in 'HelloWorld' system");
  }

  protected void setUpHelloNative() throws Exception {
    if(!checkForSystem("HelloWorld_NativeProtocol"))
      throw new Exception("PXE System HelloWorld_NativeProtocol needs to be deployed");
    if(null == getProcessMBean("HelloWorld_NativeProtocol", "helloWorld.BpelService", "HelloWorld_NativeProtocol"))
      throw new Exception("Cannot acquire 'HelloWorld_NativeProtocol' ProcessMBean for " +
        "'helloWorld.BpelService' in 'HelloWorld_NativeProtocol' system");
  }

  protected void setUpAsyncNative() throws Exception {
    if(!checkForSystem("AsyncProcess_NativeProtocol"))
      throw new Exception("PXE System AsyncProcess_NativeProtocol needs to be deployed");
    if(null == getProcessMBean("AsyncProcess_NativeProtocol", "ProcessSync.BpelService", "AsyncProcess_NativeProtocol"))
      throw new Exception("Cannot acquire 'AsyncProcess_NativeProtocol' ProcessMBean for " +
        "'ProcessSync.BpelService' in 'AsyncProcess_NativeProtocol' system");
  }
  
  protected synchronized static void tearDownThread() throws Exception {
    getPxeConnection().close();
    _pc.remove();
    // RpcInteraction - not necessary to clean up ??
    // BpelManagementFacade - not necessary to clean up ??
  }

  protected void tearDown() throws Exception {
    tearDownThread();
    if (_connector != null) {
			_connector.close();
      _connector = null;
    }
  }
  
  protected static Response invokeNative(
    String systemName,
    String serviceName,
    String portName,
    String operation,
    Map<String,String> partData,
    int msTimeout) throws Exception {
    
    Request request = new Request(systemName, serviceName,portName,operation);
    Iterator<Entry<String,String>> parts = partData.entrySet().iterator();
    while(parts.hasNext()) {
      Entry<String,String> part = parts.next();
      request.setPartData(part.getKey(),part.getValue());
    }
    
    return getNativeXmlInteraction().invoke(request, msTimeout);
  }
  
  
  
  
  //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////// Hello Native ///////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
     
  private static void sayHelloNative() throws Exception {
    Map<String,String> partData = new HashMap<String,String>();
    partData.put("TestPart", "<TestPart xmlns=\"\">Hello</TestPart>");
    System.out.println("calling invokeNative(...) WARNING-willblock-");
    Response response = invokeNative("HelloWorld_NativeProtocol", // system name
      "HelloService",              // service name
      "HelloPort",                 // port name
      "hello", partData, 30000);
    System.out.println(response.getPartData("TestPart"));
  }
  
  protected void sayHelloNative(boolean background) throws Exception {
    if(!background)
      sayHelloNative();
    else {
      System.out.println("###sending native XML message in separate Thread...");
      new Thread(new Runnable() {
        public void run() {
          try {
            sayHelloNative();
          } catch(Exception e) {
            e.printStackTrace(System.out);
            Assert.fail("Exception reached in thread calling sayHelloNative() " + e);
          }
        }
      }).start();
      System.out.println("###...sending native XML message completed");
      tearDownThread();
    }
  }
  
  protected Long createNewHelloNativeInstance(BpelManagementHelper im) throws Exception {
    InstanceQuery query = new InstanceQuery();
    query.processId = im.processID;
    query.processState = ProcessState.ALL_STATES;
    List<Long> result = im.fascade.getProcessInstances(query);
    Long[] piids = result.toArray(new Long[result.size()]);
    System.out.println("" + piids.length + " total existing instances");

    System.out.println("creating new Hello process instance with native message");   
    System.out.println("sending native XML message in separate Thread...");
    
    sayHelloNative(true);
    
    //
    // TODO - do we need to wait here really?
    //
    System.out.println("Waiting 5 seconds for new process instance to reach breakpoint");
    Thread.sleep(5000);
    
    query = new InstanceQuery();
    query.processId = im.processID;
    query.processState = ProcessState.ALL_STATES;
    result = im.fascade.getProcessInstances(query);
    piids = result.toArray(new Long[result.size()]);
    System.out.println("now " + piids.length + " total instances");
    
    //
    // TODO acquire PIIDs using MBean events
    //
    
    Long newPiid = null;
    //
    // Quick and Dirty approach to get the last one assuming increasing PIID value
    //
    for(Long piid: piids)
      if(null == newPiid || newPiid.compareTo(piid) < 0)
        newPiid = piid;
    
    return newPiid;
  }
  
  protected Long trapNewHelloNativeInstance(BpelManagementHelper im, String activityName) throws Exception {
    
    clearGlobalBreakpoints(im);
    
    ActivityBreakpoint activityBreakpoint = im.fascade.addGlobalActivityBreakpoint(
      im.processID,activityName);
    System.out.println("added Global ActivityBreakpoint " + activityBreakpoint);   
        
    Long newPiid = createNewHelloNativeInstance(im);
    
    Assert.assertNotNull("Failure to acquire new process instance ID",newPiid);
    
    short state = im.fascade.getState(newPiid);
    
    System.out.println("new PIID=" + newPiid + " state=" + state);
    
    Assert.assertEquals("new process instance is not suspended",state,ProcessState.STATE_SUSPENDED);
    
    clearGlobalBreakpoints(im);
    return newPiid;
  }

  
  
  
  
  
  
  
  //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////// Async Native ///////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  
  private static String PAYLOAD_ALL_TEMPLATE = null;
  private static String PAYLOAD_ONE_TEMPLATE = null;
  private static Pattern SEQ = Pattern.compile("\\$sequence\\$");
  private static int COUNTER = 1;
  
  static {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      StreamUtils.copy(bos,new BufferedInputStream(
        Base.class.getResourceAsStream("async_payload_many.xml")));
      PAYLOAD_ALL_TEMPLATE = new String(bos.toByteArray());
      bos = new ByteArrayOutputStream();
      StreamUtils.copy(bos,new BufferedInputStream(
        Base.class.getResourceAsStream("async_payload_one.xml")));
      PAYLOAD_ONE_TEMPLATE = new String(bos.toByteArray());
    } catch(Exception e) {
      System.err.println(e);
      e.printStackTrace(System.err);
    }
  }

  private static String createPayload(boolean all) {
    String now = Long.toString(System.currentTimeMillis());
    Matcher m;
    if(all)
      m = SEQ.matcher(PAYLOAD_ALL_TEMPLATE);
    else
      m = SEQ.matcher(PAYLOAD_ONE_TEMPLATE);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
        m.appendReplacement(sb, now + "-" + COUNTER++);
    }
    m.appendTail(sb);
    return sb.toString();
  }
  
  private static void runAsyncNative(boolean all) throws Exception {
    Map<String,String> partData = new HashMap<String,String>();
    partData.put("payload", createPayload(all));
    System.out.println("calling invokeNative(...) WARNING-willblock-");
    Response response = invokeNative("AsyncProcess_NativeProtocol", // system name
      "AsyncProcess",              // service name
      "ProcessPORT",                 // port name
      "Run", partData, 30000);
    System.out.println(response.getPartData("payload"));
  }
  
  protected void runAsyncNative(boolean background,final boolean all) throws Exception {
    if(!background)
      runAsyncNative(all);
    else {
      System.out.println("###sending native XML message in separate Thread...");
      new Thread(new Runnable() {
        public void run() {
          try {
            runAsyncNative(all);
          } catch(Exception e) {
            e.printStackTrace(System.out);
            Assert.fail("Exception reached in thread calling runAsyncNative() " + e);
          }
        }
      }).start();
      System.out.println("###...sending native XML message completed");
      tearDownThread();
    }
  }
  
  protected Long createNewAsyncNativeInstance(BpelManagementHelper im, boolean all) throws Exception {
    InstanceQuery query = new InstanceQuery();
    query.processId = im.processID;
    query.processState = ProcessState.ALL_STATES;
    List<Long> result = im.fascade.getProcessInstances(query);
    Long[] piids = result.toArray(new Long[result.size()]);
    System.out.println("" + piids.length + " total existing instances");

    System.out.println("creating new AsyncProcess process instance with native message");   
    System.out.println("sending native XML message in separate Thread...");
    
    runAsyncNative(true, all);
    
    //
    // TODO - do we need to wait here really?
    //
    System.out.println("Waiting 5 seconds for new process instance to reach breakpoints");
    Thread.sleep(5000);
    
    query = new InstanceQuery();
    query.processId = im.processID;
    query.processState = ProcessState.ALL_STATES;
    result = im.fascade.getProcessInstances(query);
    piids = result.toArray(new Long[result.size()]);
    System.out.println("now " + piids.length + " total instances");
    
    //
    // TODO acquire PIIDs using MBean events
    //
    
    Long newPiid = null;
    //
    // Quick and Dirty approach to get the last one assuming increasing PIID value
    //
    for(Long piid: piids)
      if(null == newPiid || newPiid.compareTo(piid) < 0)
        newPiid = piid;
    
    return newPiid;
  }
  
  protected Long trapNewAsyncNativeInstance(BpelManagementHelper im, boolean all, String activityName) throws Exception {
    
    clearGlobalBreakpoints(im);
    
    ActivityBreakpoint activityBreakpoint = im.fascade.addGlobalActivityBreakpoint(
      im.processID,activityName);
    System.out.println("added Global ActivityBreakpoint " + activityBreakpoint);   
        
    Long newPiid = createNewAsyncNativeInstance(im, all);
    
    Assert.assertNotNull("Failure to acquire new process instance ID",newPiid);
    
    short state = im.fascade.getState(newPiid);
    
    System.out.println("new PIID=" + newPiid + " state=" + state);
    
    Assert.assertEquals("new process instance is not suspended",state,ProcessState.STATE_SUSPENDED);
    
    clearGlobalBreakpoints(im);
    return newPiid;
  }
  
  
  
  
  
  
  
  
  protected void clearGlobalBreakpoints(BpelManagementHelper im) throws Exception {
    Breakpoint[] globalBreakpoints = im.fascade.getGlobalBreakpoints(im.processID);
    System.out.println("" + globalBreakpoints.length + " global breakpoints:");   
    for(Breakpoint globalBreakpoint: globalBreakpoints) {
      System.out.println("   Removing " + globalBreakpoint);
      im.fascade.removeGlobalBreakpoint(im.processID, globalBreakpoint);
    }
  }  
  
	public abstract void test() throws Exception;

}
