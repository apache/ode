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
package org.apache.ode.bpel.evt;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.CorrelationKeys;

/**
 * Message arrived and matched neither (a) createInstance or (b) correlation
 * match
 */
public class CorrelationNoMatchEvent extends CorrelationEvent {
	private static final long serialVersionUID = 1L;
	
	// left out for backward-compatibility
	private final Set<CorrelationKey> _keys = new HashSet<CorrelationKey>();
	private CorrelationKeys _keySet = null;

	public CorrelationNoMatchEvent(QName qName, String opName, String mexId,
			CorrelationKeys keySet) {
		super(qName, opName, mexId);

		_keySet = keySet;
	}

	public CorrelationKeys getKeySet() {
		// backward-compatibility; add up keys
		if( _keys.size() > 0 && _keySet == null ) {
			_keySet = new CorrelationKeys();
		}
		for (CorrelationKey aKey : _keys) {
			if (aKey != null && !_keySet.contains(aKey)) {
				_keySet.add(aKey);
			}
		}
		
		return _keySet;
	}

	public void setKeys(CorrelationKeys keySet) {
		_keySet = keySet;
	}

}
