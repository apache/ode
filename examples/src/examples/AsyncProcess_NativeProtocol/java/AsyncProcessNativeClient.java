import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;

import com.fs.pxe.xcontrib.sp.rpc.Request;
import com.fs.pxe.xcontrib.sp.rpc.RpcInteraction;
import com.fs.pxe.xcontrib.sp.rpc.Response;
import com.fs.pxe.ra.PxeConnection;
import com.fs.pxe.ra.PxeConnectionFactory;
import com.fs.pxe.ra.PxeManagedConnectionFactory;

import com.fs.utils.StreamUtils;
import com.fs.utils.rmi.RMIConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Demonstration of connecting to PXE using a PXE managed connection.
 *  There are two ways to 'acquire' a PxeConnectionFactory, either by deploying
 *  the PxeManagedConnectionFactory as a standard JCA resource and looking up
 *  the PxeConnectionFactory e.g.:
 *  InitialContext ctx = new InitialContext();
 *  PxeConnectionFactory cf = (PxeConnectionFactory)ctx.lookup(NAME_OF_BOUND_PXE_CF);
 *  
 *  This examples uses the 2nd method, which is to explictly create the {@link PxeManagedConnectionFactory},
 *  configure it, and create the {@link PxeConnectionFactory}.  By default,
 *  the PxeManagedConnectionFactory will use an RMI protocol.
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
    
		PxeManagedConnectionFactory pmcf = new PxeManagedConnectionFactory();
    pmcf.setURL(RMIConstants.getConnectionURL());
    PxeConnectionFactory cf = (PxeConnectionFactory)pmcf.createConnectionFactory();
    PxeConnection conn = (PxeConnection)cf.getConnection();
    RpcInteraction interaction =
      (RpcInteraction)conn.createServiceProviderSession(
          "uri:protocoladapter.native.inbound", RpcInteraction.class);
    
    // the parameters of the RpcRequest match the settings in pxe-system.xml
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
