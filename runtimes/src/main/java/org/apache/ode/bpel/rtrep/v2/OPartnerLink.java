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
package org.apache.ode.bpel.rtrep.v2;

import org.apache.ode.bpel.rapi.PartnerLinkModel;
import org.apache.ode.bpel.rapi.CorrelationSetModel;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Compiled representation of a BPEL partnerLink.
 */
public class OPartnerLink extends OBase implements PartnerLinkModel {
    static final long serialVersionUID = -1L  ;
    /** partnerLink name. */
    public String name;

    /** Scope in which this partnerLink is declared. */
    public OScope declaringScope;

    /** The type of this partnerLink. */
    public QName partnerLinkType;

    public String partnerRoleName;

    public String myRoleName;

    public PortType myRolePortType;

    public PortType partnerRolePortType;

    public boolean initializePartnerRole;

    /** The set of CorrelationSets that may be used as a match criteria, organized by {@link Operation} */
    private final HashMap<String,Set<CorrelationSetModel>> _nonIntitiatingCorrelationSets = new HashMap<String,Set<CorrelationSetModel>>();

    /** The set of initiating CorrelationSets that may be used as an uniqueness criteria, organized by {@link Operation} */
    private final HashMap<String,Set<CorrelationSetModel>> _uniqueInitiatingCorrelationSets = new HashMap<String,Set<CorrelationSetModel>>();
    
    /** The set of {@link Operation}s that can be used to create a process instance. */
    private final HashSet<String> _createInstanceOperations = new HashSet<String>();

    public OPartnerLink(OProcess owner) {
        super(owner);
    }

    public String getName() {
        return name;
    }

    public String getMyRoleName() {
        return myRoleName;
    }

    public String getPartnerRoleName() {
        return partnerRoleName;
    }

    public boolean isInitializePartnerRoleSet() {
        return initializePartnerRole;
    }

    public PortType getMyRolePortType() {
        return myRolePortType;
    }

    public PortType getPartnerRolePortType() {
        return partnerRolePortType;
    }

    public boolean hasMyRole() {
        return myRolePortType != null;
    }

    public boolean hasPartnerRole() {
        return partnerRolePortType != null;
    }

    public boolean isCreateInstanceOperation(Operation op) {
        return _createInstanceOperations.contains(op.getName());
    }

    public void addCreateInstanceOperation(Operation operation) {
        _createInstanceOperations.add(operation.getName());
    }

    /**
     * Add a {@link org.apache.ode.bpel.o.OScope.CorrelationSet} to an {@link Operation}'s list
     * of "non-initiating" correlation sets. The non-initiating correlation sets are those
     * sets that are used (along with the operation) to "match" incoming messages.
     * We need to know which correlation sets are used with which operation in order to
     * pre-compute correlation keys at the time of message receipt.
     * @param operation WSDL {@link Operation}
     * @param cset non-initiating correlation used in this operation
     */
    public void addCorrelationSetForOperation(Operation operation, CorrelationSetModel cset) {
        Set<CorrelationSetModel> ret = _nonIntitiatingCorrelationSets.get(operation.getName());
        if (ret == null) {
            ret = new HashSet<CorrelationSetModel>();
            _nonIntitiatingCorrelationSets.put(operation.getName(), ret);
        }
        ret.add(cset);

    }

    /**
     * Get all non-initiating correlation sets that are ever used to qualify a receive for a the given
     * operation.
     * @param operation the operation
     * @return all non-initiating correlation sets used in the given operation
     */
    @SuppressWarnings("unchecked")
    public Set<CorrelationSetModel> getCorrelationSetsForOperation(Operation operation) {
        Set<CorrelationSetModel> ret = _nonIntitiatingCorrelationSets.get(operation.getName());
        if (ret == null) {
            return Collections.EMPTY_SET;
        }
        return Collections.unmodifiableSet(ret);
    }

    /**
     * Add a {@link org.apache.ode.bpel.o.OScope.CorrelationSet} to an {@link Operation}'s list
     * of "unique-initiating" correlation sets. The unique-initiating correlation sets are those
     * sets that are used (along with the operation) to "initiate" incoming messages, provided
     * that there doesn't already exist a process instance with that correlation key.
     * We need to know which correlation sets are used with which operation in order to
     * pre-compute correlation keys at the time of message receipt.
     * @param operation WSDL {@link Operation}
     * @param cset unique-initiating correlation used in this operation
     */
    public void addUniqueCorrelationSetForOperation(Operation operation, CorrelationSetModel cset) {    	
    	if (cset.isUnique()) {
	        Set<CorrelationSetModel> ret = _uniqueInitiatingCorrelationSets.get(operation.getName());
	        if (ret == null) {
	            ret = new HashSet<CorrelationSetModel>();
	            _uniqueInitiatingCorrelationSets.put(operation.getName(), ret);
	        }
	        ret.add(cset);
    	}
    }
    
    /**
     * Get all unique initiating correlation sets that are ever used to qualify a receive for a the given
     * operation.
     * @param operation the operation
     * @return all unique-initiating correlation sets used in the given operation
     */
    @SuppressWarnings("unchecked")
    public Set<CorrelationSetModel> getUniqueCorrelationSetsForOperation(Operation operation) {
        Set<CorrelationSetModel> ret = _uniqueInitiatingCorrelationSets.get(operation.getName());
        if (ret == null) {
            return Collections.EMPTY_SET;
        }
        return Collections.unmodifiableSet(ret);
    }

    @SuppressWarnings("unchecked")
    public Operation getMyRoleOperation(String name) {
        for (Operation op : (List<Operation>)myRolePortType.getOperations()) 
            if (op.getName().equals(name))
                return op;
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public Operation getPartnerRoleOperation(String name) {
        for (Operation op : (List<Operation>)partnerRolePortType.getOperations()) 
            if (op.getName().equals(name))
                return op;
        return null;
    }
}
