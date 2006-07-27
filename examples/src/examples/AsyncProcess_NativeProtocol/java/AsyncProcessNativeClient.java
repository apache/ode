import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;

import org.apache.ode.xcontrib.sp.rpc.Request;
import org.apache.ode.xcontrib.sp.rpc.RpcInteraction;
import org.apache.ode.xcontrib.sp.rpc.Response;
import org.apache.ode.ra.OdeConnection;
import org.apache.ode.ra.OdeConnectionFactory;
import org.apache.ode.ra.OdeManagedConnectionFactory;

import org.apache.ode.utils.StreamUtils;
import org.apache.ode.utils.rmi.RMIConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Demonstration of connecting to ODE using a ODE managed connection.
 *  There are two ways to 'acquire' a OdeConnectionFactory, either by deploying
 *  the OdeManagedConnectionFactory as a standard JCA resource and looking up
 *  the OdeConnectionFactory e.g.:
 *  InitialContext ctx = new InitialContext();
 *  OdeConnectionFactory cf = (OdeConnectionFactory)ctx.lookup(NAME_OF_BOUND_ODE_CF);
 *  
 *  This examples uses the 2nd method, which is to explictly create the {@link OdeManagedConnectionFactory},
 *  configure it, and create the {@link OdeConnectionFactory}.  By default,
 *  the OdeManagedConnectionFactory will use an RMI protocol.
 */
public class AsyncProcessNativeClient {
  
  private static String PAYLOAD_TEMPLATE = null;
  private static Pattern SEQ = Pattern.compile("\\$sequence\\$");
  private static int COUNTER = 1;
  
  static {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      StreamUtils.copy(bos,new BufferedInputStream(
        AsyncProcessNativeClient.class.getResourceAsStream("payload.xml")));
      PAYLOAD_TEMPLATE = new String(bos.toByteArray());
    } catch(Exception e) {
      System.err.println(e);
      e.printStackTrace(System.err);
    }
  }

  private static String createPayload() {
    String now = Long.toString(System.currentTimeMillis());
    Matcher m = SEQ.matcher(PAYLOAD_TEMPLATE);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
        m.appendReplacement(sb, now + "-" + COUNTER++);
    }
    m.appendTail(sb);
    return sb.toString();
  }
  
	public static void main(String[] argv) throws Exception { 
    
		OdeManagedConnectionFactory pmcf = new OdeManagedConnectionFactory();
    pmcf.setURL(RMIConstants.getConnectionURL());
    OdeConnectionFactory cf = (OdeConnectionFactory)pmcf.createConnectionFactory();
    OdeConnection conn = (OdeConnection)cf.getConnection();
    RpcInteraction interaction =
      (RpcInteraction)conn.createServiceProviderSession(
          "uri:protocoladapter.native.inbound", RpcInteraction.class);
    
    // the parameters of the RpcRequest match the settings in ode-system.xml
    // for the native service provider
    Request request = new Request("AsyncProcess_NativeProtocol", // system name
                                              "AsyncProcess",              // service name
                                              "ProcessPORT",                 // port name
                                              "Run");                    // operation
    
    request.setPartData("payload", createPayload());
    Response response = interaction.invoke(request, 30000);
    System.out.println(response.getPartData("payload"));
  }
}
