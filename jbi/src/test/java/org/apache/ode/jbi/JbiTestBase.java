/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ode.jbi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.framework.ComponentContextImpl;
import org.apache.servicemix.jbi.framework.ComponentNameSpace;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.util.FileUtil;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.junit.Ignore;
import org.springframework.context.support.AbstractXmlApplicationContext;

@Ignore
public class JbiTestBase extends SpringTestSupport {
    private static Log log = LogFactory.getLog(JbiTestBase.class);

    protected OdeComponent odeComponent;
    protected JBIContainer jbiContainer;
    
    protected Properties testProperties;
    
    @Override
    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("/" + getTestName() + "/smx.xml");
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        jbiContainer = ((JBIContainer) getBean("jbi"));
        odeComponent = new OdeComponent();

        ComponentContextImpl cc = new ComponentContextImpl(jbiContainer, new ComponentNameSpace(jbiContainer.getName(), "ODE"));
        ActivationSpec activationSpec = new ActivationSpec();
        activationSpec.setComponent(odeComponent);
        activationSpec.setComponentName("ODE");
        jbiContainer.activateComponent(new File("target/test/smx/ode").getAbsoluteFile(), odeComponent, "", cc, activationSpec,  true, false, false, null);
        
        testProperties = new Properties();
        testProperties.load(getClass().getResourceAsStream("/" + getTestName() + "/test.properties"));
    }
    
    protected String getTestName() {
        return getClass().getSimpleName();
    }
    
    protected void enableProcess(String resource, boolean enable) throws Exception {
        resource = "target/test/resources/" + resource;
        String process = resource.substring(resource.lastIndexOf('/') + 1);
        if (enable) {
            odeComponent.getServiceUnitManager().deploy(process, resource);
            odeComponent.getServiceUnitManager().init(process, resource);
            odeComponent.getServiceUnitManager().start(process);
        } else {
            odeComponent.getServiceUnitManager().stop(process);
            odeComponent.getServiceUnitManager().undeploy(process, resource);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


    protected void go() throws Exception {
        enableProcess(getTestName(), true);

        int i = 0;
        while (true) {
            String prefix = i == 0 ? "" : "" + i;
            
            String request = testProperties.getProperty(prefix + "request");
            if (request == null) {
                if (i == 0) {
                    i++;
                    continue;
                } else break;
            }
            if (request.startsWith("@")) {
                request = inputStreamToString(getClass().getResourceAsStream("/" + getTestName() + "/" + request.substring(1)));
            }
            String expectedResponse = testProperties.getProperty(prefix + "response");
            {
    	        String httpUrl = testProperties.getProperty(prefix + "http.url");
    	        if (httpUrl != null) {
    	            log.debug(getTestName() + " sending http request to " + httpUrl + " request: " + request);
    	            URLConnection connection = new URL(httpUrl).openConnection();
    	            connection.setDoOutput(true);
    	            connection.setDoInput(true);
    	            //Send request
    	            OutputStream os = connection.getOutputStream();
    	            PrintWriter wt = new PrintWriter(os);
    	            wt.print(request);
    	            wt.flush();
    	            wt.close();
    	            // Read the response.
    	            String result = inputStreamToString(connection.getInputStream());
    	            
    	            log.debug(getTestName() + " have result: " + result);
    	            matchResponse(expectedResponse, result);
    	        }
            }
            {
    	        if (testProperties.getProperty(prefix + "nmr.service") != null) {
    	            DefaultServiceMixClient client = new DefaultServiceMixClient(jbiContainer);
    	            InOut io = client.createInOutExchange();
    	            io.setService(QName.valueOf(testProperties.getProperty(prefix + "nmr.service")));
    	            io.setOperation(QName.valueOf(testProperties.getProperty(prefix + "nmr.operation")));
    	            io.getInMessage().setContent(new StreamSource(new ByteArrayInputStream(request.getBytes())));
    	            client.sendSync(io,20000);
    	            assertEquals(ExchangeStatus.ACTIVE,io.getStatus());
    	            assertNotNull(io.getOutMessage());
    	            String result = new SourceTransformer().contentToString(io.getOutMessage());
    	            matchResponse(expectedResponse, result);
    	            client.done(io);
    	        }
            }
            
            i++;
        }
        
        enableProcess(getTestName(), false);
    }
    
    protected void matchResponse(String expectedResponse, String result) {
        assertTrue("Response doesn't match expected regex.\nExpected: " + expectedResponse + "\nReceived: " + result, Pattern.compile(expectedResponse, Pattern.DOTALL).matcher(result).matches());
    }
    
    private String inputStreamToString(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtil.copyInputStream(is, baos);
        return baos.toString();
    }
}
