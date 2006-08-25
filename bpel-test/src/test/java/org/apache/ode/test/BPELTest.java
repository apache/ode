package org.apache.ode.test;

import java.io.File;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.memdao.BpelDAOConnectionFactoryImpl;
import org.apache.ode.test.scheduler.TestScheduler;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

public class BPELTest extends TestCase {
	
	private BpelServer server;

	@Override
	protected void setUp() throws Exception {
		server = new BpelServerImpl();
		server.setDaoConnectionFactory(new BpelDAOConnectionFactoryImpl());
		server.setScheduler(new TestScheduler());
		server.setBindingContext(new BindingContextImpl());
		server.init();
		server.start();
	}

	@Override
	protected void tearDown() throws Exception {
		server.stop();
	}
	
	private void go(String deployDir) throws Exception {
		
		File testPropsFile = new File(deployDir+"/test.properties");
		if ( !testPropsFile.exists()) {
			System.err.println("can't find "+ testPropsFile.toString());
		}
		Properties testProps = new Properties();
		testProps.load(testPropsFile.toURL().openStream());
		
		QName serviceId = new QName(testProps.getProperty("namespace"),
				testProps.getProperty("service"));
		String operation = testProps.getProperty("operation");
		String in = testProps.getProperty("request");
		String responsePattern = testProps.getProperty("response");
		
		server.getDeploymentService().deploy(new File(deployDir));
		
		MyRoleMessageExchange mex = server.getEngine().createMessageExchange("",serviceId,operation);
		
		Message request = mex.createMessage(null);
		
		Element elem = DOMUtils.stringToDOM(in);			
		request.setMessage(elem);
		
		mex.invoke(request);
		
		Message response = mex.getResponse();
		
		String resp = DOMUtils.domToString(response.getMessage());
		assertTrue(Pattern.compile(responsePattern,Pattern.DOTALL).matcher(resp).matches());
		
	}
	
	public void testHelloWorld2() throws Exception {
		go("target/test-classes/bpel/2.0/HelloWorld2");
	}

}
