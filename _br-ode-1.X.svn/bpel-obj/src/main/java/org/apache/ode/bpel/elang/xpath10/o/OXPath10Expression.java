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
package org.apache.ode.bpel.elang.xpath10.o;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.ode.bpel.o.OBase;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.bpel.o.OLValueExpression;
import org.apache.ode.bpel.o.OLink;
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OXslSheet;
import org.apache.ode.bpel.o.OScope.Variable;
import org.apache.ode.utils.NSContext;


/**
 * Jaxen-based compiled-xpath representation for XPATH 1.0 expression language. 
 */
public class OXPath10Expression extends OLValueExpression implements Serializable {
	private static final long serialVersionUID = -1L;

  /** Map from query name to variable declaration. */
  public HashMap<String, OScope.Variable> vars =
    new HashMap<String, OScope.Variable>();

  public final HashMap<String, OProcess.OProperty> properties =
    new HashMap<String, OProcess.OProperty>();

  public final HashMap<String, OLink> links = new HashMap<String,OLink>();

  /** Map getVariableData invocation signature to compiled objects. */
  private final HashMap<SigGetVariableData,OSigGetVariableData> _getVariableDataSigs =
    new HashMap<SigGetVariableData,OSigGetVariableData>();

  public String xpath;
  public NSContext namespaceCtx;

  /** QName of the <code>bpws:getVariableData</code> function. */
  public final QName qname_getVariableData;

  /** QName of the <code>bpws:getVariableData</code> function. */
  public final QName qname_getVariableProperty;

  /** QName of the <code>bpws:getVariableData</code> function. */
  public final QName qname_getLinkStatus;

  protected final HashMap<URI, OXslSheet> xslSheets = new HashMap<URI, OXslSheet>();

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
  
  public void setXslSheet(URI projectRelativeXslUri, OXslSheet xslSheet) {
	  xslSheets.put(projectRelativeXslUri, xslSheet);
  }

  public OXslSheet getXslSheet(URI projectRelativeXslUri) {
	  return xslSheets.get(projectRelativeXslUri);
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
		if(vars.size() == 0)
			throw new IllegalStateException("LValue must have one variable reference.");
        // We're interested in the first variable referenced by the LValue
        for (String varName : vars.keySet()) {
            if (xpath.substring(1, xpath.length()).startsWith(varName))
                return vars.get(varName);
        }
        throw new IllegalStateException("Either the expression doesn't start with a variable reference or " +
                "the reference is unknow.");
	}

}

