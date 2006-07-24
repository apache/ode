package com.fs.pxe.axis2;

import com.fs.pxe.axis2.dd.DeployDocument;
import com.fs.pxe.axis2.dd.TDeployment;
import com.fs.pxe.axis2.dd.TProvide;
import com.fs.pxe.axis2.dd.TInvoke;
import com.fs.pxe.bom.wsdl.Definition4BPEL;
import com.fs.pxe.bom.wsdl.WSDLFactory4BPEL;
import com.fs.pxe.bom.wsdl.WSDLFactoryBPEL20;
import com.fs.pxe.bpel.o.OProcess;
import com.fs.pxe.bpel.o.Serializer;
import com.fs.pxe.bpel.compiler.BpelC;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Representation of a process deployment unit. A deployment unit may actually
 * contain more than one process.
 */
public class DeploymentUnit {

  private static Log __log = LogFactory.getLog(DeploymentUnit.class);

  private long _lastModified;
  private String _name;
  private File _duDirectory;
  private DocumentRegistry _docRegistry;
  private HashMap<QName,OProcess> _processes;
  private DeployDocument _dd;

  private PXEServer _pxeServer;

  private static final FileFilter _wsdlFilter = new FileFilter(){
    public boolean accept(File path) {
      return path.getName().endsWith(".wsdl");
    }
  };
  private static final FileFilter _cbpFilter = new FileFilter(){
    public boolean accept(File path) {
      return path.getName().endsWith(".cbp");
    }
  };
  private static final FileFilter _bpelFilter = new FileFilter(){
    public boolean accept(File path) {
      return path.getName().endsWith(".bpel");
    }
  };

  public DeploymentUnit(File dir, PXEServer pxeServer) {
    File ddLocation = new File(dir, "deploy.xml");
    _duDirectory = dir;
    _pxeServer = pxeServer;
    _lastModified = ddLocation.lastModified();
    _name = dir.getName();
    _docRegistry = new DocumentRegistry(new DocumentEntityResolver(_duDirectory));

    WSDLFactory4BPEL wsdlFactory = (WSDLFactory4BPEL) WSDLFactoryBPEL20.newInstance();
    WSDLReader r = wsdlFactory.newWSDLReader();

    File[] wsdls = _duDirectory.listFiles(_wsdlFilter);
    for (File file : wsdls) {
      try {
        _docRegistry.addDefinition((Definition4BPEL) r.readWSDL(file.toURI().toString()));
      } catch (WSDLException e) {
        throw new DeploymentException("Couldn't read WSDL document " + file.getAbsolutePath(), e);
      }
    }
    _processes = new HashMap<QName, OProcess>();

    try {
      _dd = DeployDocument.Factory.parse(ddLocation);
    } catch (Exception e) {
      throw new DeploymentException("Couldnt read deployment descriptor at location " + ddLocation.getAbsolutePath(), e);
    }

  }

  public void deploy(boolean activateOnly) {
    // (Re)compile all bpel files if it's a "real" re-deployment, a simple
    // activation doesn't need recompile.
    if (!activateOnly) compileProcesses();
    else loadProcessDefinitions();

    // Going trough each process declared in the dd
    for (TDeployment.Process processDD : _dd.getDeploy().getProcessList()) {
      OProcess oprocess = _processes.get(processDD.getName());
      if (oprocess == null) throw new DeploymentException("Could not find the compiled process definition for a " +
              "process referenced in the deployment descriptor: " + processDD.getName());
      try {
        // We only need to activate, it's not a full re-deployment
        if (activateOnly) {
          _pxeServer.getBpelServer().activate(processDD.getName(), false);
        } else {
          _pxeServer.getBpelServer().deploy(processDD.getName(),
                  _duDirectory.toURI(), oprocess, _docRegistry.getDefinitions(), processDD);
        }

        // But we still need to declare our services internally
        for (TProvide provide : processDD.getProvideList()) {
          Definition4BPEL def = _docRegistry.getDefinition(
                  provide.getService().getName().getNamespaceURI());
          _pxeServer.createService(def, provide.getService().getName(),
                  provide.getService().getPort());
        }
        for (TInvoke invoke : processDD.getInvokeList()) {
          Definition4BPEL def = _docRegistry.getDefinition(
                  invoke.getService().getName().getNamespaceURI());
          _pxeServer.createExternalService(def, invoke.getService().getName(),
                  invoke.getService().getPort());
        }
      } catch (AxisFault axisFault) {
        __log.error("Service deployment in Axis2 failed!", axisFault);
      } catch (Throwable e) {
        __log.error("Service deployment failed!", e);
      }
    }
    _lastModified = new File(_duDirectory, "deploy.xml").lastModified();
  }

  public void undeploy() {
    for (TDeployment.Process processDD : _dd.getDeploy().getProcessList()) {
      OProcess oprocess = _processes.get(processDD.getName());
      if (oprocess == null) throw new DeploymentException("Could not find the compiled process definition for a " +
              "process referenced in the deployment descriptor: " + processDD.getName());
      for (TProvide provide : processDD.getProvideList()) {
        _pxeServer.destroyService(provide.getService().getName());
      }
      _pxeServer.getBpelServer().undeploy(processDD.getName());
    }
  }

  /**
   * Load the parsed and compiled BPEL process definition.
   */
  private OProcess loadProcess(File f) {
    InputStream is = null;
    try {
      is = new FileInputStream(f);
      Serializer ofh = new Serializer(is);
      return ofh.readOProcess();
    } catch (Exception e) {
      throw new DeploymentException("Couldn't read compiled BPEL process " + f.getAbsolutePath(), e);
    } finally {
      try {
        if (is !=null) is.close();
      } catch (Exception e) { }
    }
  }

  /**
   * Checking for each BPEL file if we have a corresponding compiled process. If we
   * don't, starts compilation. The force parameter just forces compilation, whether
   * a cbp file exists or not.
   */
  private void compileProcesses() {
    File[] bpels = _duDirectory.listFiles(_bpelFilter);
    for (File bpel : bpels) {
      compile(bpel);
    }
    loadProcessDefinitions();
  }

  private void compile(File bpelFile) {
    BpelC bpelc = BpelC.newBpelCompiler();
    bpelc.setOutputDirectory(_duDirectory);
    bpelc.setWsdlFinder(new DUWsdlFinder(_duDirectory));
    bpelc.setXsltFinder(new DUXsltFinder(_duDirectory));
    try {
      bpelc.compile(bpelFile.toURL());
    } catch (IOException e) {
      __log.error("Couldn't compile process file!", e);
    }
  }

  private void loadProcessDefinitions() {
    File[] cbps = _duDirectory.listFiles(_cbpFilter);
    for (File file : cbps) {
      OProcess oprocess = loadProcess(file);
      _processes.put(new QName(oprocess.targetNamespace, oprocess.getName()), oprocess);
    }
  }

  public boolean exists() {
    return _duDirectory.exists();
  }

  public boolean matches(File f){
    return f.lastModified() == _lastModified;
  }

  public int hashCode(){
    return (int) (_name.hashCode() + _lastModified);
  }

  public File getDuDirectory() {
    return _duDirectory;
  }

  public void setLastModified(long lastModified) {
    _lastModified = lastModified;
  }

  public long getLastModified() {
    return _lastModified;
  }

  public String toString() {
    return "{DeploymentUnit " + _name + "}";
  }

}
