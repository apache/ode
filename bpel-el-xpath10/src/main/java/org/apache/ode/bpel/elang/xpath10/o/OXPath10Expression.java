/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.elang.xpath10.o;

import org.apache.ode.bpel.o.*;
import org.apache.ode.bpel.o.OScope.Variable;
import org.apache.ode.utils.NSContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.net.URI;

import javax.xml.namespace.QName;


/**
 * Jaxen-based compiled-xpath representation for XPATH 1.0 expression language. 
 */
public class OXPath10Expression extends OLValueExpression implements Serializable {
	private static final long serialVersionUID = -1L;

  /** Map from query name to variable declaration. */
  public final Map<String, OScope.Variable> vars =
    new HashMap<String, OScope.Variable>();

  public final Map<String, OProcess.OProperty> properties =
    new HashMap<String, OProcess.OProperty>();

  public final Map<String, OLink> links = new HashMap<String,OLink>();

  public final Map<URI,OXslSheet> xslSheets = new HashMap<URI, OXslSheet>();

  /** Map getVariableData invocation signature to compiled objects. */
  private final Map<SigGetVariableData,OSigGetVariableData> _getVariableDataSigs =
    new HashMap<SigGetVariableData,OSigGetVariableData>();

  public String xpath;
  public NSContext namespaceCtx;

  /** QName of the <code>bpws:getVariableData</code> function. */
  public final QName qname_getVariableData;

  /** QName of the <code>bpws:getVariableData</code> function. */
  public final QName qname_getVariableProperty;

  /** QName of the <code>bpws:getVariableData</code> function. */
  public final QName qname_getLinkStatus;


  public OXPath10Expression(OProcess owner,
                            QName qname_getVariableData,
                            QName qname_getVariableProperty,
                            QName qname_getLinkStatus) {
    super(owner);
    this.qname_getLinkStatus = qname_getLinkStatus;
    this.qname_getVariableData = qname_getVariableData;
    this.qname_getVariableProperty = qname_getVariableProperty;
  }

  /**
   * Get the compiled representation of a certain <code>bpws:getVariableData(...)</code> call.
   * @param varname variable name
   * @param partname part name
   * @param location location query
   * @return compiled representation, or <code>null</code> if none found
   */
  public OSigGetVariableData resolveGetVariableDataSig(String varname, String partname, String location) {
    SigGetVariableData key = new SigGetVariableData(varname,  partname, location);
    return _getVariableDataSigs.get(key);
  }

  /**
   * Add a compiled representation for a certain <code>bpws:getVariableData(...)</code> call.
   * @param varname variable name
   * @param partname part name
   * @param location location query
   * @param compiled compiled representation
   */
  public void addGetVariableDataSig(String varname, String partname, String location, OSigGetVariableData compiled) {
    _getVariableDataSigs.put(new SigGetVariableData(varname,  partname, location), compiled);
  }

  public String toString() {
    return "{OXPath10Expression " + xpath + "}";
  }

  /**
   * Data structure representing the signature of a <code>bpws:getVariableData</code>
   * invocation.
   */
  private static final class SigGetVariableData implements Serializable {
    private static final long serialVersionUID = -1L;

    private final String varname;
    private final String partname;
    private final String location;

    private SigGetVariableData(String varname, String partname, String location) {
      this.varname = varname;
      this.partname = partname;
      this.location = location;
    }

    public boolean equals(Object obj) {
      SigGetVariableData other = (SigGetVariableData) obj;
      if (varname != null && (other.varname == null || !varname.equals(other.varname)))
        return false;
      if (partname != null && (other.partname == null || !partname.equals(other.partname)))
        return false;
      if (location != null && (other.location == null || !location.equals(other.location)))
        return false;

      return true;
    }

    public int hashCode() {
      int hashCode = 0;
      if (varname != null) hashCode ^= varname.hashCode();
      if (partname != null) hashCode ^= partname.hashCode();
      if (location != null) hashCode ^= location.hashCode();
      return hashCode;
    }
  }

  /**
   * Data structure representing the compiled signature of a <code>bpws:getVariableData</code>
   * invocation.
   */
  public static final class OSigGetVariableData extends OBase {
    private static final long serialVersionUID = -1L;
    public final OScope.Variable variable;
    public final OMessageVarType.Part part;
    public final OExpression location;

    public OSigGetVariableData(OProcess owner, OScope.Variable variable, OMessageVarType.Part part, OExpression location) {
      super(owner);
      this.variable = variable;
      this.part = part;
      this.location = location;
    }
  }

	/**
	 * @see org.apache.ode.bpel.o.OLValueExpression#getVariable()
	 */
	public Variable getVariable() {
		if(vars.size() > 1)
			throw new IllegalStateException("LValue must not have more than one variable reference");
		if(vars.size() == 0)
			throw new IllegalStateException("LValue must have one variable reference.");
		return vars.values().iterator().next();
	}

}

