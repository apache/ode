package com.fs.pxe.axis;

import com.fs.pxe.axis.dd.DeployDocument;
import com.fs.pxe.axis.dd.TDeployment;
import com.fs.pxe.axis.dd.TProvide;
import com.fs.pxe.bom.wsdl.Definition4BPEL;
import com.fs.pxe.bom.wsdl.WSDLFactory4BPEL;
import com.fs.pxe.bom.wsdl.WSDLFactoryBPEL20;
import com.fs.pxe.bpel.o.OProcess;
import com.fs.pxe.bpel.o.Serializer;
import org.apache.axis2.AxisFault;

import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Representation of a process deployment unit. A deployment unit may actually
 * contain more than one process.
 */
public class DeploymentUnit {

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
    File[] cbps = _duDirectory.listFiles(_cbpFilter);
    _processes = new HashMap<QName, OProcess>();
    for (File file : cbps) {
      OProcess oprocess = loadProcess(file);
      _processes.put(new QName(oprocess.targetNamespace, oprocess.getName()), oprocess);
    }
    try {
      _dd = DeployDocument.Factory.parse(ddLocation);
    } catch (Exception e) {
      throw new DeploymentException("Couldnt read deployment descriptor at location " + ddLocation.getAbsolutePath(), e);
    }
  }

  public void deploy(boolean activateOnly) {
    for (TDeployment.Process processDD : _dd.getDeploy().getProcessList()) {
      OProcess oprocess = _processes.get(processDD.getName());
      if (oprocess == null) throw new DeploymentException("Could not find the compiled process definition for a " +
              "process referenced in the deployment descriptor: " + processDD.getName());
      try {
        if (activateOnly) {
          _pxeServer.getBpelServer().activate(processDD.getName(), false);
        } else {
          _pxeServer.getBpelServer().deploy(processDD.getName(),
                  _duDirectory.toURI(), oprocess, _docRegistry.getDefinitions(), processDD);
        }

        for (TProvide provide : processDD.getProvideList()) {
          Definition4BPEL def = _docRegistry.getDefinition(
                  provide.getService().getName().getNamespaceURI());
          _pxeServer.createService(def, provide.getService().getName(), provide.getService().getPort());
        }
      } catch (AxisFault axisFault) {
        throw new DeploymentException("Service deployment in Axis2 failed!", axisFault);
      } catch (IOException e) {
        // Highly unexpected
        e.printStackTrace();
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

  public boolean exists() {
    return _duDirectory.exists();
  }

  public boolean matches(File f){
    return f.lastModified() == _lastModified;
  }

  public int hashCode(){
    return (int) (_name.hashCode() + _lastModified);
  }

  public String toString() {
    return "{DeploymentUnit " + _name + "}";
  }

}
