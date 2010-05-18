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

package org.apache.ode.jbi;

import java.util.HashMap;
import java.util.Map;

import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.XMLParserUtils;

public class OdeSUManager implements ServiceUnitManager {
  private static final Log __log = LogFactory.getLog(OdeSUManager.class);

  private static final Messages __msgs = Messages.getMessages(Messages.class);

  private static final String XMLNS_JBI_MGMT = "http://java.sun.com/xml/ns/jbi/management-message";
  
  private OdeContext _ode;

  /** All the service units that have been "init"ed. */
  private Map<String, OdeServiceUnit> _serviceUnits = new HashMap<String, OdeServiceUnit>();

  public OdeSUManager(OdeContext odeContext) {
    _ode = odeContext;
  }

    public synchronized String deploy(String serviceUnitID, String serviceUnitRootPath) throws DeploymentException {
        __log.trace("deploy: id=" + serviceUnitID + ", path=" + serviceUnitRootPath);

        OdeServiceUnit su = new OdeServiceUnit(_ode, serviceUnitID, serviceUnitRootPath);
    try {
      su.deploy();
    } catch (Exception ex) {
      __log.error(__msgs.msgServiceUnitDeployFailed(serviceUnitID));
      return makeStatusMessage("deploy", "FAILED");
    }

    return makeStatusMessage("deploy", "SUCCESS");

  }

    public synchronized void init(String serviceUnitID, String serviceUnitRootPath) throws DeploymentException {
    __log.trace("init called for " + serviceUnitID);

    if (_serviceUnits.containsKey(serviceUnitID)) {
            __log.debug("odd, init() called for su " + serviceUnitID + ", but it is already init()ed");
      return;
    }

    try {
            OdeServiceUnit su = new OdeServiceUnit(_ode, serviceUnitID, serviceUnitRootPath);
      su.init();
      _serviceUnits.put(serviceUnitID, su);
    } catch (Exception ex) {
      String errmsg = __msgs.msgServiceUnitInitFailed(serviceUnitID);
      __log.error(errmsg, ex);
      throw new DeploymentException(errmsg, ex);
    }
  }

    public synchronized void shutDown(String serviceUnitID) throws DeploymentException {
    __log.trace("shutDown called for " + serviceUnitID);

    OdeServiceUnit su = _serviceUnits.remove(serviceUnitID);
    if (su == null)
      return;

    try {
      su.shutdown();
    } catch (Exception ex) {
      String errmsg = __msgs.msgServiceUnitShutdownFailed(serviceUnitID);
      __log.error(errmsg, ex);
      throw new DeploymentException(errmsg, ex);
    }
  }

    public synchronized void start(String serviceUnitID) throws DeploymentException {
    __log.trace("start called for " + serviceUnitID);

    OdeServiceUnit su = _serviceUnits.get(serviceUnitID);
    if (su == null) {
      // Should not really happen if JBI is working.
      String errmsg = "Unexpected state; start() called before init()";
      IllegalStateException ex = new IllegalStateException(errmsg);
      __log.error(errmsg, ex);
      throw new DeploymentException(errmsg, ex);
    }
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    try {
     	Thread.currentThread().setContextClassLoader(su.getConfigurationClassLoader());
    	su.start();
    } catch (Exception ex) {
      String errmsg = __msgs.msgServiceUnitStartFailed(serviceUnitID);
      __log.error(errmsg, ex);
      throw new DeploymentException(errmsg, ex);
    } finally {
    	Thread.currentThread().setContextClassLoader(cl);
    }

  }

  /**
   * Stop the deployment. This causes the component to cease generating service
   * requests related to the deployment. This returns the deployment to a state
   * equivalent to after init() was called
   *
   * @param serviceUnitID
   *          service unit ID
   *
   * @throws DeploymentException
   *           deployment exception
   */
    public synchronized void stop(String serviceUnitID) throws DeploymentException {
    __log.trace("stop called for " + serviceUnitID);

    OdeServiceUnit su = _serviceUnits.get(serviceUnitID);
    if (su == null) {
      // Should not really happen if JBI is working.
      String errmsg = "Unexpected state; stop() called before init()";
      IllegalStateException ex = new IllegalStateException(errmsg);
      __log.error(errmsg, ex);
      throw new DeploymentException(errmsg, ex);
    }

    try {
      su.stop();
    } catch (Exception ex) {
      String errmsg = __msgs.msgServiceUnitStopFailed(serviceUnitID);
      __log.error(errmsg, ex);
      throw new DeploymentException(errmsg, ex);
    }

  }

  /**
   * Cancel a Service Deployment. If the deployment is in use (has
   * dependencies), then will operation may fail.
   *
   * @param serviceUnitID -
   *          ID of the Service Unit being undeployed
   * @param serviceUnitRootPath -
   *          Full path to the Service Unit root.
   *
   * @return NOT YET DOCUMENTED
   *
   * @throws DeploymentException
   *           deployment exception
   */
    public synchronized String undeploy(String serviceUnitID, String serviceUnitRootPath) throws DeploymentException {
        __log.trace("undeploy: id=" + serviceUnitID + ", path=" + serviceUnitRootPath);

        OdeServiceUnit su = new OdeServiceUnit(_ode, serviceUnitID, serviceUnitRootPath);

    try {
      su.undeploy();
    } catch (Exception ex) {
      __log.error(__msgs.msgServiceUnitDeployFailed(serviceUnitID));
      return makeStatusMessage("undeploy", "FAILED");
    }

    return makeStatusMessage("undeploy", "SUCCESS");

  }

  /**
   * Generate those lame XML result strings that JBI requires. Oh did I mention
   * how lame this is? If not, let me remind the reader: this is just about the
   * lamest "clever idea" I have ever seen.
   *
   * @param task
   *          the task that failed and must now generate a lame result string
   * @param status
   *          the status code that will go into the lame result string.
   * @return a lame JBI result string
   */
  private String makeStatusMessage(String task, String status) {

    /*
     * Cheat sheet: <component-task-result> <component-name>BC1</component-name>
     * <component-task-result-details
     * xmlns="http://java.sun.com/xml/ns/jbi/management- <task-result-details>
     * <task-id>deploy</task-id> <task-result>SUCCESS</task-result>
     * </task-result-details> </component-task-result-details>
     * </component-task-result>
     *
     */

    // First of all, what is the logic why XML ? and if XML, why a String
    // and not a DOM ? But the 64k question is what is wrong with Exceptions?
    Document doc;
    try {
        // Note that we are using our own choice of factory (xerces), not the
        // one that is provided by the system. This is important, otherwise the
        // serialization routine won't work.
      DocumentBuilderFactory dbf = XMLParserUtils.getDocumentBuilderFactory();
      DocumentBuilder db = dbf.newDocumentBuilder();
      doc = db.newDocument();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

    Element elem = doc.createElementNS(XMLNS_JBI_MGMT, "component-task-result");
    doc.appendChild(elem);
    Element compNameElem = doc.createElementNS(XMLNS_JBI_MGMT, "component-name");
    elem.appendChild(compNameElem);
    Element compTaskRsltDtlsElem = doc.createElementNS(XMLNS_JBI_MGMT, "component-task-result-details");
    elem.appendChild(compTaskRsltDtlsElem);
    Element taskRsltDtlsElem = doc.createElementNS(XMLNS_JBI_MGMT, "task-result-details");
    compTaskRsltDtlsElem.appendChild(taskRsltDtlsElem);

    Element taskId = doc.createElementNS(XMLNS_JBI_MGMT, "task-id");
    taskRsltDtlsElem.appendChild(taskId);

    Element taskResult = doc.createElementNS(XMLNS_JBI_MGMT, "task-result");
    taskRsltDtlsElem.appendChild(taskResult);

    // Why do I have to tell this thing the component name? It /knows/ the
    // component name....
    compNameElem.appendChild(doc.createTextNode(_ode.getContext().getComponentName()));

    // And why on earth do I have to tell my caller the method he just
    // called?
    taskId.appendChild(doc.createTextNode(task));

    taskResult.appendChild(doc.createTextNode(status));
    return DOMUtils.domToString(elem);
  }
}
