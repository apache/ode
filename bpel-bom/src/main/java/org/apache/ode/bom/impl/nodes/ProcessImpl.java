/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.Import;
import org.apache.ode.bom.api.Process;
import org.apache.ode.utils.NSContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * BPEL object model representation of a BPEL process.
 * Objects of this class serve as the "root" objects in the BPEL object model.
 */
public class ProcessImpl extends ScopeImpl implements Process {

  private static final long serialVersionUID = -1L;

  private static final Log __log = LogFactory.getLog(ProcessImpl.class);

  private Activity _rootActivity;
  private String _name;
  private String _targetNamespace;
  private String _source;
  private short _suppressJoinFailure;
  private short _bpelVersion = Process.BPEL_V200;

  /**
   * Name to {@link Activity} mapping for all activities in the process.
   */
  private HashMap<String,Activity> _activities = new HashMap<String,Activity>();
  private String _exprLang = null;
  private String _queryLang = null;
  private final Set<Import> _imports = new HashSet<Import>();

  /**
   * Constructor.
   *
   * @param name                process name
   * @param targetNamespace     target namespace
   * @param source              URL of the BPEL source document
   * @param suppressJoinFailure process-level suppress join failure flag
   */
  public ProcessImpl(NSContext nsContext, String name, String targetNamespace, String source,
                     short suppressJoinFailure) {
    super(nsContext);
    _name = name;
    _targetNamespace = targetNamespace;
    _source = source;
    _suppressJoinFailure = suppressJoinFailure;
  }

  public ProcessImpl() {
    super();
  }

  public String getType() {
    return "process";
  }

  public void setName(String name) {
    if (__log.isDebugEnabled()) {
      __log.debug(getName() + ": name(" + name + ")");
    }

    _name = name;
  }

  public String getName() {
    return _name;
  }

  public short getBpelVersion() {
    return _bpelVersion;
  }

  public void setBpelVersion(short bpelVersion) {
    _bpelVersion = bpelVersion;
  }

  public void setRootActivity(Activity root) {
    if (__log.isDebugEnabled()) {
      __log.debug(getName() + ": setRootActivity(" + root + ")");
    }

    _rootActivity = root;
  }

  public Activity getRootActivity() {
    return _rootActivity;
  }

  public void setSource(String source) {
    if (__log.isDebugEnabled()) {
      __log.debug(getName() + ": setSource(" + source + ")");
    }

    _source = source;
  }

  public String getSource() {
    return _source;
  }

  public short getSuppressJoinFailure() {
    return _suppressJoinFailure;
  }

  public void setSuppressJoinFailure(short suppressJoinFailure) {
    _suppressJoinFailure = suppressJoinFailure;
  }
  
  public String getTargetNamespace() {
    return _targetNamespace;
  }

  public org.apache.ode.bom.api.Activity findActivity(String name) {
    Activity retVal = _activities.get(name);

    if (__log.isTraceEnabled()) {
      __log.trace("findActivity(" + name + ")=" + retVal);
    }

    return retVal;
  }

  public String getQueryLanguage() {
    return _queryLang;
  }

  public void setQueryLanguage(String queryLang) {
    _queryLang = queryLang;
  }

  public String getExpressionLanguage() {
    return _exprLang;
  }

  public void setExpressionLanguage(String expLanguage) {
    _exprLang = expLanguage;
  }


  public Set<Import> getImports() {
    return Collections.unmodifiableSet(_imports);
  }

  public void addImport(Import imprt) {
    _imports.add(imprt);
  }

  public void removeImport(Import imprt) {
    _imports.remove(imprt);
  }

	/**
	 * @see org.apache.ode.bom.api.Process#setTargetNamespace(java.lang.String)
	 */
	public void setTargetNamespace(String tns) {
		_targetNamespace = tns;
	}

}
