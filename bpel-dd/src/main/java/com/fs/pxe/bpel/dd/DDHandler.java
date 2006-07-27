package com.fs.pxe.bpel.dd;

import com.fs.pxe.bpel.o.OProcess;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlOptions;

import javax.wsdl.Definition;
import java.io.File;
import java.io.IOException;

/**
 *
 */
public class DDHandler {

  private static final Log __log = LogFactory.getLog(DDHandler.class);

  private static final DDValidator[] VALIDATORS =
          new DDValidator[] { new EndpointValidator(), new EventFilterValidator() };
  private static final DDEnhancer[] ENHANCERS = new DDEnhancer[] { new EndpointEnhancer() };

  private File _ddFile;
  private DeploymentDescriptorDocument _dd;
  private boolean _modified;
  private boolean _exists = false;

  public DDHandler(File ddFile) throws DDException {
    _ddFile = ddFile;
    if (_ddFile.isFile()) {
      try {
        _dd = DeploymentDescriptorDocument.Factory.parse(_ddFile);
        _exists = true;
      } catch (Exception e) {
        throw new DDException("Deployment descriptor " + _ddFile.toString() +
                " could not be read.", e);
      }
    }
    _modified = false;
  }

  /**
   * Validates the deployment descriptor content by confronting it to the process
   * and generates missing endpoints if possible.
   * @param oprocess
   * @param wsdlDefs
   * @return
   * @throws DDException
   */
  public boolean validateAndEnhance(OProcess oprocess, Definition[] wsdlDefs) throws DDException {
    __log.debug("Validating deployment descriptor.");
    if (exists())
      for (DDValidator ddValidator : VALIDATORS)
        ddValidator.validate(_dd.getDeploymentDescriptor(), oprocess);
    else {
      _dd = DeploymentDescriptorDocument.Factory.newInstance();
      _dd.addNewDeploymentDescriptor();
    }

    __log.debug("Updating deployment descriptor.");
    for (DDEnhancer ddEnhancer : ENHANCERS) {
      _modified = ddEnhancer.enhance(_dd.getDeploymentDescriptor(), oprocess, wsdlDefs) || _modified;
    }
    return _modified;
  }

  public boolean exists() {
    return _exists;
  }

  public boolean isModified() {
    return _modified;
  }

  public void write(File output) throws DDException {
    try {
      XmlOptions opts = new XmlOptions();
      opts.setSavePrettyPrint();
      opts.setSavePrettyPrintIndent(4);
      _dd.save(output, opts);
    } catch (IOException e) {
      throw new DDException("Deployment descriptor " + _ddFile.toString() +
              " could not be saved!", e);
    }
  }

}
