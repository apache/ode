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
package org.apache.ode.bpel.explang;

import java.net.URI;
import java.util.Date;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.bpel.o.OLink;
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OScope;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Context for evaluating expressions. Implementations of the
 * {@link ExpressionLanguageRuntime} interface use this interface to access BPEL
 * variables, property sets and link statuses.
 */
public interface EvaluationContext {

    /**
     * Read the value of a BPEL variable.
     *
     * @param variable
     *          variable to read
     * @param part
     *          the part (or <code>null</code>)
     * @return the value of the variable, wrapped in a <code>Node</code>
     */
    Node readVariable(OScope.Variable variable, OMessageVarType.Part part) throws FaultException;

    Node getPartData(Element message, OMessageVarType.Part part) throws FaultException;
    /**
     * Read the value of a BPEL property.
     *
     * @param variable
     *          variable containing property
     * @param property
     *          property to read
     * @return value of the property
     */
    String readMessageProperty(OScope.Variable variable, OProcess.OProperty property)
            throws FaultException;

    /**
     * Obtain the status of a control link.
     *
     * @param olink
     *          link to check
     * @return <code>true</code> if the link is active, <code>false</code>
     *         otherwise.
     */
    boolean isLinkActive(OLink olink) throws FaultException;

    /**
     * Obtain the root node.
     *
     * @return root node
     */
    Node getRootNode();

    /**
     * Evaluate a query expression.
     *
     * @param root
     *          the root context
     * @param expr
     *          the query expression
     * @return node returned by query
     */
    Node evaluateQuery(Node root, OExpression expr) throws FaultException, EvaluationException;

    /**
     * Reads the current process instance id.
     * @return instance id
     */
    Long getProcessId();

    /**
     * Reads the current process's name.
     * @return process name
     */
    QName getProcessQName();
    
    /**
     * Indicates whether simple types should be narrowed to a Java type when
     * using this evaluation context.
     */
    boolean narrowTypes();
    
    /**
     * Retrieves the base URI that the BPEL Process execution contextis running relative to.
     * 
     * @return URI - the URI representing the absolute physical file path location that this process is defined within.
     */
    URI getBaseResourceURI();
    
    /**
     * Retrieves the property value that has been defined for this BPEL Process type.
     * 
     * @return propertyValue - the value corresponding to the process property name.
     */
    Node getPropertyValue(QName propertyName);

    Date getCurrentEventDateTime();
}
