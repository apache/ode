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

import java.io.Serializable;
import java.util.List;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;
import org.apache.ode.utils.stl.UnaryFunction;
import org.w3c.dom.Element;

/**
 * WSDL4J representation of a BPEL <code>&lt;partnerLink&gt;</code>
 * declaration.
 * 
 * @see org.apache.ode.bom.wsdl.PartnerLinkTypeSerializer
 */
public class PartnerLinkType extends BpelObject4WSDL implements ExtensibilityElement, Serializable {

    private static final long serialVersionUID = -1L;
    private List<Role> _roles;

    public PartnerLinkType(Element el) {
        super(el);
    }


    public QName getName() {
        return new QName(getTargetNamespace(),getAttribute("name"));
    }


    public Role getRole(final String roleName) {
        if (roleName == null)
            throw new IllegalArgumentException("Null name not permitted.");
        return CollectionsX.find_if(getRoles(), new MemberOfFunction<Role>() {
            public boolean isMember(Role o) {
                return o.getName() != null && o.getName().equals(roleName);
            }
        });
    }

    public List<Role> getRoles() {
        if (_roles == null) {
            _roles = getChildren(Role.class);
            CollectionsX.apply(_roles,new UnaryFunction<Role,Void>() {

                public Void apply(Role x) {
                    x.setTargetNamespace(getTargetNamespace());
                    return null;
                }
                
            });
        }
        
        return _roles;
    }

    /**
     * Representation of the WSDL partnerLink link type role elements.
     */
    public static class Role extends BpelObject4WSDL  {
        private static final long serialVersionUID = -1L;

        public Role(Element el) {
            super(el);
        }

        /**
         * Get the portName of the role (e.g. "Buyer", "Seller").
         * 
         * @return role portName
         */
        public String getName() {
            return getAttribute("name");
        }

        /**
         * Get the WSDL portType of the role (i.e. the interface implemented by
         * the object acting in the role).
         * 
         * @return role portType
         */
        public QName getPortType() {
            return getNamespaceContext().derefQName(getAttribute("portType"));
        }

    }
    
    /**
     * BPEL 1.1 nonsense.
     * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
     *
     */
    public static class Role11 extends Role {
        private static final long serialVersionUID = -1L;

        public Role11(Element el) {
            super(el);
        }

        /**
         * Get the WSDL portType of the role (i.e. the interface implemented by
         * the object acting in the role).
         * 
         * @return role portType
         */
        public QName getPortType() {
            PortType11 pt11 = getFirstChild(PortType11.class);
            return pt11 == null ? null : pt11.getName();
        }
        
        
        public static class PortType11 extends BpelObject4WSDL {
            private static final long serialVersionUID = 8174002706633806360L;

            public PortType11(Element el) {
                super(el);
            }

            public QName getName() {
                return getNamespaceContext().derefQName(getAttribute("name"));
            }
        }
    }

}
