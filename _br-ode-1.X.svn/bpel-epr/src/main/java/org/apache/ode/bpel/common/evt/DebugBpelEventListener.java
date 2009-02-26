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
package org.apache.ode.bpel.common.evt;

import java.util.Properties;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.iapi.BpelEventListener;

/**
 * Example implementation of the {@link BpelEventListener} interface.
 * 
 * Dumps navigation events to a logging appender and optionally to stdout.
 * To use the DebugBpelEventListener add the following lines to your 
 * ode-xxx.properties file:
 * <code>
 * ode-jbi.event.listeners=org.apache.ode.bpel.common.evt.DebugBpelEventListener
 * debugeventlistener.dumpToStdOut=on/off
 * </code>
 * 
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class DebugBpelEventListener implements BpelEventListener {
	private static final Log __log = LogFactory.getLog(BpelEventListener.class);

	private static final String SYSOUT_KEY = "debugeventlistener.dumpToStdOut";
	private boolean _dumpToStdOut = false;
	
	public void onEvent(BpelEvent bpelEvent) {
		if (__log.isDebugEnabled()) {
			__log.debug(bpelEvent.toString());
		}
	
		if (_dumpToStdOut) {
			System.out.println(bpelEvent.toString());
		}
	}

	public void startup(Properties configProperties) {
		if (configProperties != null) {
			_dumpToStdOut = BooleanUtils.toBoolean(configProperties.getProperty(SYSOUT_KEY, "false"));
		}
	}

	public void shutdown() {}
}
