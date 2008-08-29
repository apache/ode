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
package org.apache.ode.bpel.compiler.bom;

import org.apache.ode.bpel.compiler.bom.AssignActivity.AssignOperation;
import org.apache.ode.bpel.extension.ExtensibleElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * BOM representation of the BPEL <code>&lt;extensionAssignOperation&gt;</code>.
 *  
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class ExtensionAssignOperation extends BpelObject implements AssignOperation,
        ExtensibleElement {
	private Element _childElement;
	
    public ExtensionAssignOperation(Element el) {
        super(el);
    }

    public Element getNestedElement() {
        //XXX
    	//return getFirstExtensibilityElement(); 
    	if (_childElement == null) {
	    	NodeList nl = getElement().getChildNodes();
	        for (int i = 0; i < nl.getLength(); ++i) {
	            Node node = nl.item(i);
	            if (node.getNodeType() == Node.ELEMENT_NODE && 
	            		!Bpel20QNames.NS_WSBPEL2_0.equals(node.getNamespaceURI())) {
	                _childElement = (Element)node;
	                break;
	            }
	        }
        }
        return _childElement;
    }

}
