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

package org.apache.ode.jbi.msgmap;

import java.util.Set;

import javax.activation.DataHandler;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessagingException;
import javax.security.auth.Subject;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.DOMUtils;

public class JbiWsdl11WrapperMapperTest extends junit.framework.TestCase {
    private static Log __log = LogFactory.getLog(JbiWsdl11WrapperMapperTest.class);

    public static class MockJbiFault implements javax.jbi.messaging.Fault {
        private String resourceName;

        public MockJbiFault(String resourceName) {
            this.resourceName = resourceName;
        }

        public void addAttachment(String id, DataHandler content) throws MessagingException {
            // TODO Auto-generated method stub
            __log.debug("addAttachment");

        }

        public DataHandler getAttachment(String id) {
            // TODO Auto-generated method stub
            __log.debug("getAttachment");
            return null;
        }

        public Set getAttachmentNames() {
            // TODO Auto-generated method stub
            __log.debug("getAttachmentNames");
            return null;
        }

        public Source getContent() {
            try {
                return new DOMSource(DOMUtils.stringToDOM(IOUtils.toString(getClass().getResourceAsStream(resourceName))));
            } catch (Exception e) {
                throw new RuntimeException("", e);
            }
        }

        public Object getProperty(String name) {
            // TODO Auto-generated method stub
            __log.debug("getProperty");
            return null;
        }

        public Set getPropertyNames() {
            // TODO Auto-generated method stub
            __log.debug("getPropertyNames");
            return null;
        }

        public Subject getSecuritySubject() {
            // TODO Auto-generated method stub
            __log.debug("getSecuritySubject");
            return null;
        }

        public void removeAttachment(String id) throws MessagingException {
            // TODO Auto-generated method stub
            __log.debug("removeAttachment");

        }

        public void setContent(Source content) throws MessagingException {
            // TODO Auto-generated method stub
            __log.debug("setContent");

        }

        public void setProperty(String name, Object value) {
            // TODO Auto-generated method stub
            __log.debug("setProperty");

        }

        public void setSecuritySubject(Subject subject) {
            // TODO Auto-generated method stub
            __log.debug("setSecuritySubject");

        }

    };

    public void testFault() throws Exception {
        JbiWsdl11WrapperMapper m = new JbiWsdl11WrapperMapper();
        Definition w = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLReader().readWSDL(getClass().getResource("/test.wsdl").getFile());
        {
            Fault f = new MockJbiFault("/fault.xml");
            javax.wsdl.Fault k = m.toFaultType(f, ((Operation) w.getPortType(QName.valueOf("{http://www.example.org/test/}test")).getOperations().get(0)).getFaults().values());
            assertEquals("fault", k.getName());
        }
        {
            Fault f = new MockJbiFault("/fault1.xml");
            javax.wsdl.Fault k = m.toFaultType(f, ((Operation) w.getPortType(QName.valueOf("{http://www.example.org/test/}test")).getOperations().get(0)).getFaults().values());
            assertEquals("fault1", k.getName());
        }
    }
}
