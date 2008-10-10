/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ode.axis2;

import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;

/**
 *
 *
 */
public class EndpointConfigurationTest extends Axis2TestBase {


  @Test
    public void testEndpointProperties() throws Exception {
        executeProcess();
    }

    protected void executeProcess() throws Exception {
        executeProcess(null);
    }

    protected void executeProcess(ContextHandler handler) throws Exception {
        JettyWrapper jettyWrapper = new JettyWrapper(handler);
        jettyWrapper.start();

        try {
            String bundleName = "TestEndpointProperties";
            if (server.isDeployed(bundleName)) server.undeployProcess(bundleName);
            server.deployProcess(bundleName);
            try {
                String response = server.sendRequestFile("http://localhost:8888/processes/helloWorld",
                        bundleName, "testRequest.soap");
                assertTrue(response.contains("helloResponse") && response.contains("OK!!!"));
            } finally {
                server.undeployProcess(bundleName);
            }
        } finally {
            jettyWrapper.stop();
        }
    }

    /**
     * Redo the exact same test but with Basic Authentication activated on the external service
     * @throws Exception
     */
  @Test
    public void testHttpAuthentication() throws Exception {
        ContextHandler securedEchoContext;
        {
            Constraint constraint = new Constraint();
            constraint.setName(Constraint.__BASIC_AUTH);
            constraint.setRoles(new String[]{"user"});
            constraint.setAuthenticate(true);

            ConstraintMapping cm = new ConstraintMapping();
            cm.setConstraint(constraint);
            cm.setPathSpec("/*");

            SecurityHandler sh = new SecurityHandler();
            sh.setUserRealm(new HashUserRealm("MyRealm", getClass().getResource("/TestEndpointProperties/jetty-realm.properties").toURI().toString()));
            sh.setConstraintMappings(new ConstraintMapping[]{cm});

            securedEchoContext = new ContextHandler();
            securedEchoContext.setContextPath("/EchoService");

            HandlerList hc = new HandlerList();
            hc.addHandler(sh);
            hc.addHandler(new JettyWrapper.EchoServiceHandler());
            securedEchoContext.addHandler(hc);
        }
        executeProcess(securedEchoContext);
    }


}
