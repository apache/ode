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
package org.apache.ode.extension.jmseventlistener;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Basic Consumer that dumps Ode's BPEL events to the console.
 *
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class TestConsumer implements MessageListener {
	private Connection connection;
	private Session session;	
	private Topic topic;

	public static void main(String[] argv) throws Exception {
		TestConsumer c = new TestConsumer();
    	c.run();
	}

	public void run() throws JMSException {
		ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		connection = factory.createConnection();    	
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		topic = session.createTopic("org.apache.ode.events");

		MessageConsumer consumer = session.createConsumer(topic);
		consumer.setMessageListener(this);

		connection.start();

		System.out.println("Waiting for messages...");		
	}

	public void onMessage(Message msg) {
		TextMessage t = (TextMessage)msg;
		try {
			System.err.println("--");
			System.out.println(t.getText());
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
