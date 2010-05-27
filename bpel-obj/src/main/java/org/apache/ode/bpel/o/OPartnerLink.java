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
package org.apache.ode.bpel.o;

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
public class OPartnerLink extends OBase {
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
    private final HashMap<String,Set<OScope.CorrelationSet>> _nonIntitiatingCorrelationSets = new HashMap<String,Set<OScope.CorrelationSet>>();

    /** The set of joining CorrelationSets that may be used as a match criteria, organized by {@link Operation} */
    private HashMap<String,Set<OScope.CorrelationSet>> _joiningCorrelationSets = new HashMap<String,Set<OScope.CorrelationSet>>();

    /** The set of {@link Operation}s that can be used to create a process instance. */
    private final HashSet<String> _createInstanceOperations = new HashSet<String>();

    public OPartnerLink(OProcess owner) {
        super(owner);
    }

    public String getName() {
        return name;
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
    public void addCorrelationSetForOperation(Operation operation, OScope.CorrelationSet cset, boolean isJoin) {
        if( !isJoin ) {
            Set<OScope.CorrelationSet> ret = _nonIntitiatingCorrelationSets.get(operation.getName());
            if (ret == null) {
                ret = new HashSet<OScope.CorrelationSet>();
                _nonIntitiatingCorrelationSets.put(operation.getName(), ret);
            }
            ret.add(cset);
        } else {
            // serialization backward compatibility; joiningCorrelationSets could be null if read from old definition
            if( _joiningCorrelationSets == null ) {
                _joiningCorrelationSets = new HashMap<String,Set<OScope.CorrelationSet>>();
            }
            Set<OScope.CorrelationSet> ret = _joiningCorrelationSets.get(operation.getName());
            if (ret == null) {
                ret = new HashSet<OScope.CorrelationSet>();
                _joiningCorrelationSets.put(operation.getName(), ret);
            }
            ret.add(cset);
        }
    }

    /**
     * Get all non-initiating correlation sets that are ever used to qualify a receive for a the given
     * operation.
     * @param operation the operation
     * @return all non-initiating correlation sets used in the given operation
     */
    @SuppressWarnings("unchecked")
    public Set<OScope.CorrelationSet> getNonInitiatingCorrelationSetsForOperation(Operation operation) {
        Set<OScope.CorrelationSet> ret = _nonIntitiatingCorrelationSets.get(operation.getName());
        if (ret == null) {
            return Collections.EMPTY_SET;
        }
        return Collections.unmodifiableSet(ret);
    }

    /**
     * Get all joining correlation sets that are ever used to qualify a receive for a the given
     * operation.
     * @param operation the operation
     * @return all non-initiating correlation sets used in the given operation
     */
    @SuppressWarnings("unchecked")
    public Set<OScope.CorrelationSet> getJoinningCorrelationSetsForOperation(Operation operation) {
        if (_joiningCorrelationSets == null) return new HashSet();

        Set<OScope.CorrelationSet> ret = _joiningCorrelationSets.get(operation.getName());
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

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof OPartnerLink)) return false;
        OPartnerLink other = (OPartnerLink) obj;
        return (name == null && other.name == null && super.equals(obj)) || name.equals(other.name);
    }
}
