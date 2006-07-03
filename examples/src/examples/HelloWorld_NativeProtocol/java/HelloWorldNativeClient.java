
import com.fs.pxe.xcontrib.sp.rpc.Request;
import com.fs.pxe.xcontrib.sp.rpc.RpcInteraction;
import com.fs.pxe.xcontrib.sp.rpc.Response;
import com.fs.pxe.ra.PxeConnection;
import com.fs.pxe.ra.PxeConnectionFactory;
import com.fs.pxe.ra.PxeManagedConnectionFactory;
import com.fs.utils.rmi.RMIConstants;

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
public class HelloWorldNativeClient {
  
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
    Request request = new Request("HelloWorld_NativeProtocol", // system name
                                              "HelloService",              // service name
                                              "HelloPort",                 // port name
                                              "hello");                    // operation
    
    /* 
     we enclose the xml data with a 'TestPart' element with an empty namespace
     b/c of RPC rules (see WS-I Basic profile). If the WSDL defined this message part 
     as a doc/literal, the part content would would contain as its outermost element
     the xml element defined in the xml schema.
    */
    request.setPartData("TestPart", "<TestPart xmlns=\"\">Hello</TestPart>");
    Response response = interaction.invoke(request, 30000);
    // The response: <TestPart xmlns="">Hello World</TestPart>
    System.out.println(response.getPartData("TestPart"));
  }
}
