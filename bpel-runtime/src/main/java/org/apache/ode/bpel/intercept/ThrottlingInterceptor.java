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
package org.apache.ode.bpel.intercept;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.dao.ProcessPropertyDAO;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;

/**
 * An example of a  simple interceptor providing a "throttling"  capability - that is an 
 * ability to limit the number of instances created for a given process.
 * 
 * @author Maciej Szefler
 */
public class ThrottlingInterceptor extends NoOpInterceptor {
	/** Name of process property that control the maximum number of instances. */
	private static final QName PROP_MAX_INSTANCES = new QName("urn:org.apache.ode.bpel.intercept", "maxInstances");

	@Override
	public void onProcessInvoked(MyRoleMessageExchange mex,
			InterceptorContext ic) throws FailMessageExchangeException {
		int maxInstances;
		try {
			maxInstances = Integer.valueOf(getSimpleProperty(PROP_MAX_INSTANCES, ic));
		} catch (Exception ex) {
			return;
		}
		
		if (ic.getProcessDAO().getNumInstances() >= maxInstances)
			throw new FailMessageExchangeException("Too many instances.");
	}

	
	/**
	 * Get the value of a simple process property
	 * @param propertyName name of the property
	 * @param ic interceptor context
	 * @return value of the property, or <code>null</code> if not set
	 */
	private String getSimpleProperty(QName propertyName, InterceptorContext ic) {
		Collection<ProcessPropertyDAO> props = ic.getProcessDAO().getProperties();
		for (ProcessPropertyDAO prop : props) {
			QName pQname = new QName(prop.getNamespace(), prop.getName());
			if (pQname.equals(propertyName))
				return prop.getSimpleContent();
		}
		
		return null;
	}
}
