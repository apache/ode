/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.o;

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
   * Add a {@link com.fs.pxe.bpel.o.OScope.CorrelationSet} to an {@link Operation}'s list
   * of "non-initiating" correlation sets. The non-initiating correlation sets are those
   * sets that are used (along with the operation) to "match" incoming messages.
   * We need to know which correlation sets are used with which operation in order to
   * pre-compute correlation keys at the time of message receipt.
   * @param operation WSDL {@link Operation}
   * @param cset non-initiating correlation used in this operation
   */
  public void addCorrelationSetForOperation(Operation operation, OScope.CorrelationSet cset) {
    Set<OScope.CorrelationSet> ret = _nonIntitiatingCorrelationSets.get(operation.getName());
    if (ret == null) {
      ret = new HashSet<OScope.CorrelationSet>();
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
  public Set<OScope.CorrelationSet> getCorrelationSetsForOperation(Operation operation) {
    Set<OScope.CorrelationSet> ret = _nonIntitiatingCorrelationSets.get(operation.getName());
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
