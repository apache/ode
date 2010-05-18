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

import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.iapi.BindingContext;
import org.apache.ode.bpel.iapi.Endpoint;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BindingContextImpl implements BindingContext {
	

	public EndpointReference activateMyRoleEndpoint(QName processId, Endpoint myRoleEndpoint) {
		final Document doc = DOMUtils.newDocument();
		Element serviceref = doc.createElementNS(EndpointReference.SERVICE_REF_QNAME.getNamespaceURI(),
                EndpointReference.SERVICE_REF_QNAME.getLocalPart());
        serviceref.setNodeValue(myRoleEndpoint.serviceName +":" +myRoleEndpoint.portName);
        doc.appendChild(serviceref);
        return new EndpointReference() {
            public Document toXML() {
              return doc;
            }
        };
	}

	public void deactivateMyRoleEndpoint(Endpoint myRoleEndpoint) {

	}

	public PartnerRoleChannel createPartnerRoleChannel(QName processId, PortType portType,
			Endpoint initialPartnerEndpoint) {
		return new PartnerRoleChannelImpl();
	}

	public long calculateSizeofService(EndpointReference epr) {
		return 0;
	}

}
