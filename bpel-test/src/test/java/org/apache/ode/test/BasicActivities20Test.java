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
package org.apache.ode.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.junit.Test;

public class BasicActivities20Test extends BPELTestAbstract {

    @Test public void testHelloWorld2() throws Throwable {
        go("/bpel/2.0/HelloWorld2");
    }

    @Test public void testNegativeTargetNS1() throws Throwable {
        /**
         * Test for an invalid targetNamespace has been entered into the WSDL. See JIRA ODE-67 Test for a specific exception
         * message.
         */
        Deployment deployment = addDeployment("/bpel/2.0/NegativeTargetNSTest1");
        deployment.expectedException = ContextException.class;

        go();
    }

    @Test public void testTimer() throws Throwable {
        go("/bpel/2.0/TestTimer");
    }

    @Test public void testIf() throws Throwable {
        go("/bpel/2.0/TestIf");
    }

    @Test public void testIfBoolean() throws Throwable {
        go("/bpel/2.0/TestIfBoolean");
    }

    /**
     * Tests the wait "for" syntax.
     * @throws Throwable
     */
    @Test public void testWaitFor() throws Throwable {
        deploy("/bpel/2.0/TestWait1");
        Invocation inv = addInvoke("Wait1#1", new QName("http://ode/bpel/unit-test.wsdl", "testService"), "testOperation",
            "<message><TestPart/><Time/></message>",
            null);
        inv.minimumWaitMs=5*1000L;
        inv.maximumWaitMs=7*1000L;
        inv.expectedStatus = MessageExchange.Status.ASYNC;
        inv.expectedFinalStatus = MessageExchange.Status.RESPONSE;

        go();
    }

    /**
     * Test the wait "until" syntax.
     */
    @Test public void testWaitUntil() throws Throwable {
        deploy("/bpel/2.0/TestWaitUntil");
        DateFormat idf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        String isountil = idf.format(new Date(System.currentTimeMillis()+5000));
        Invocation inv = addInvoke("WaitUntil", new QName("http://ode/bpel/unit-test.wsdl", "testService"), "testOperation",
            "<message><TestPart/><Time>"+isountil+"</Time></message>",
            null);
        inv.minimumWaitMs=4*1000L;
        inv.maximumWaitMs=7*1000L;
        inv.expectedStatus = MessageExchange.Status.ASYNC;
        inv.expectedFinalStatus = MessageExchange.Status.RESPONSE;

        go();
    }

    /**
     * Test the wait "until" syntax.
     */
    @Test public void testWaitUntilPast() throws Throwable {
        deploy("/bpel/2.0/TestWaitUntil");
        DateFormat idf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        String isountil = idf.format(new Date(System.currentTimeMillis()-5000));
        Invocation inv = addInvoke("WaitUntil", new QName("http://ode/bpel/unit-test.wsdl", "testService"), "testOperation",
            "<message><TestPart/><Time>"+isountil+"</Time></message>",
            null);
        inv.maximumWaitMs=2*1000L;
        inv.expectedStatus = MessageExchange.Status.ASYNC;
        inv.expectedFinalStatus = MessageExchange.Status.RESPONSE;

        go();
    }

    /**
     * Tests the wait "for" syntax.
     * @throws Throwable
     */
    @Test public void testOnAlarm() throws Throwable {
        deploy("/bpel/2.0/TestAlarm");
        Invocation inv = addInvoke("Wait1#1", new QName("http://ode.apache.org/example", "CanonicServiceForClient"), "receive",
            "<message><body><start xmlns=\"http://ode.apache.org/example\">start</start></body></message>",
            null);
        inv.maximumWaitMs=20*1000L;
        inv.expectedStatus = MessageExchange.Status.ASYNC;
        inv.expectedFinalStatus = MessageExchange.Status.RESPONSE;

        go();
    }

}
