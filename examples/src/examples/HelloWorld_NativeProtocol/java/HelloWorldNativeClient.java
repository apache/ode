
import org.apache.ode.xcontrib.sp.rpc.Request;
import org.apache.ode.xcontrib.sp.rpc.RpcInteraction;
import org.apache.ode.xcontrib.sp.rpc.Response;
import org.apache.ode.ra.OdeConnection;
import org.apache.ode.ra.OdeConnectionFactory;
import org.apache.ode.ra.OdeManagedConnectionFactory;
import org.apache.ode.utils.rmi.RMIConstants;

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
public class HelloWorldNativeClient {
  
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
