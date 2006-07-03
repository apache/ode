package com.fs.pxe.jbi;

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

import com.fs.utils.DOMUtils;

public class PxeSUManager implements ServiceUnitManager {
  private static final Log __log = LogFactory.getLog(PxeSUManager.class);

  private static final Messages __msgs = Messages.getMessages(Messages.class);

  private PxeContext _pxe;

  /** All the service units that have been "init"ed. */
  private Map<String, PxeServiceUnit> _serviceUnits = new HashMap<String, PxeServiceUnit>();

  public PxeSUManager(PxeContext pxeContext) {
    _pxe = pxeContext;
  }

  public synchronized String deploy(String serviceUnitID,
      String serviceUnitRootPath) throws DeploymentException {
    __log
        .trace("deploy: id=" + serviceUnitID + ", path=" + serviceUnitRootPath);

    PxeServiceUnit su = new PxeServiceUnit(_pxe, serviceUnitID,
        serviceUnitRootPath);
    try {
      su.deploy();
    } catch (Exception ex) {
      __log.error(__msgs.msgServiceUnitDeployFailed(serviceUnitID));
      return makeStatusMessage("deploy", "FAILURE");
    }

    return makeStatusMessage("deploy", "SUCCESS");

  }

  public synchronized void init(String serviceUnitID, String serviceUnitRootPath)
      throws DeploymentException {
    __log.trace("init called for " + serviceUnitID);

    if (_serviceUnits.containsKey(serviceUnitID)) {
      __log.debug("odd, init() called for su " + serviceUnitID
          + ", but it is already init()ed");
      return;
    }

    try {
      PxeServiceUnit su = new PxeServiceUnit(_pxe, serviceUnitID,
          serviceUnitRootPath);
      su.init();
      _serviceUnits.put(serviceUnitID, su);
    } catch (Exception ex) {
      String errmsg = __msgs.msgServiceUnitInitFailed(serviceUnitID);
      __log.error(errmsg, ex);
      throw new DeploymentException(errmsg, ex);
    }
  }

  public synchronized void shutDown(String serviceUnitID)
      throws DeploymentException {
    __log.trace("shutDown called for " + serviceUnitID);

    PxeServiceUnit su = _serviceUnits.remove(serviceUnitID);
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

  public synchronized void start(String serviceUnitID)
      throws DeploymentException {
    __log.trace("start called for " + serviceUnitID);

    PxeServiceUnit su = _serviceUnits.get(serviceUnitID);
    if (su == null) {
      // Should not really happen if JBI is working.
      String errmsg = "Unexpected state; start() called before init()";
      IllegalStateException ex = new IllegalStateException(errmsg);
      __log.error(errmsg, ex);
      throw new DeploymentException(errmsg, ex);
    }

    try {
      su.start();
    } catch (Exception ex) {
      String errmsg = __msgs.msgServiceUnitStartFailed(serviceUnitID);
      __log.error(errmsg, ex);
      throw new DeploymentException(errmsg, ex);
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
  public synchronized void stop(String serviceUnitID)
      throws DeploymentException {
    __log.trace("stop called for " + serviceUnitID);

    PxeServiceUnit su = _serviceUnits.get(serviceUnitID);
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
  public synchronized String undeploy(String serviceUnitID,
      String serviceUnitRootPath) throws DeploymentException {
    __log
        .trace("undeploy: id=" + serviceUnitID + ", path=" + serviceUnitRootPath);

    PxeServiceUnit su = new PxeServiceUnit(_pxe, serviceUnitID,
        serviceUnitRootPath);
    
    try {
      su.undeploy();
    } catch (Exception ex) {
      __log.error(__msgs.msgServiceUnitDeployFailed(serviceUnitID));
      return makeStatusMessage("undeploy", "FAILURE");
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
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      doc = db.newDocument();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

    Element elem = doc.createElement("component-task-result");
    doc.appendChild(elem);
    Element compNameElem = doc.createElement("component-name");
    elem.appendChild(compNameElem);
    Element compTaskRsltDtlsElem = doc
        .createElement("component-task-result-details");
    elem.appendChild(compTaskRsltDtlsElem);
    Element taskRsltDtlsElem = doc.createElement("task-result-details");
    compTaskRsltDtlsElem.appendChild(taskRsltDtlsElem);

    Element taskId = doc.createElement("task-id");
    taskRsltDtlsElem.appendChild(taskId);

    Element taskResult = doc.createElement("task-result");
    taskRsltDtlsElem.appendChild(taskResult);

    // Why do I have to tell this thing the component name? It /knows/ the
    // component name....
    compNameElem.appendChild(doc.createTextNode(_pxe.getContext()
        .getComponentName()));

    // And why on earth do I have to tell my caller the method he just
    // called?
    taskId.appendChild(doc.createTextNode(task));

    taskResult.appendChild(doc.createTextNode(status));
    return DOMUtils.domToString(elem);
  }
}
