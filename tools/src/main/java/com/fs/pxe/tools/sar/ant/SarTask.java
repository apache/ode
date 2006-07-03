/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.sar.ant;

import com.fs.pxe.sfwk.deployment.ExpandedSAR;
import com.fs.pxe.sfwk.deployment.som.SystemDescriptor;
import com.fs.pxe.sfwk.deployment.som.sax.SystemDescriptorFactory;
import com.fs.pxe.sfwk.rr.ResourceRepositoryException;
import com.fs.pxe.sfwk.rr.URLResourceRepository;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.ZipFileSet;
import org.xml.sax.SAXException;

/**
 * An extension of the ANT {@link Jar} task for dealing with 
 * some SAR-specific items (like the system deployment descriptor
 * and system resource repository location. 
 */
public class SarTask extends Jar {
  
	private File _pxeSystemXml;
	private File _systemRR;
	
  public void setDescriptor(File f) {
    _pxeSystemXml = f; 
  }
  
  public void setSystemrr(File f) {
    _systemRR = f;
  }
      
  public void execute() throws BuildException {
  	processSystemRR();
  	processDescriptor();
  	super.execute();
  }

	private void processDescriptor() {
		if (_pxeSystemXml == null)
			throw new BuildException("A PXE system descriptor must be specified!");
		if (!_pxeSystemXml.exists() || !_pxeSystemXml.isFile())
			throw new BuildException("System descriptor " + _pxeSystemXml + " not found!");
		
    SystemDescriptor sd;
    AntErrorHandler handler = new AntErrorHandler(this,_pxeSystemXml.getName());
    try {
      sd = SystemDescriptorFactory.parseDescriptor(_pxeSystemXml.toURI().toURL(),
          handler, null, true);
    } catch (SAXException se) {
      throw new BuildException("Error parsing PXE system descriptor " + _pxeSystemXml + " !",se);
    } catch (IOException ioe) {
      throw new BuildException("Error reading PXE system descriptor " + _pxeSystemXml + " !",ioe);
    }

    if (handler.getErrors() != 0)
    	throw new BuildException("PXE system descriptor "  + _pxeSystemXml + " contained errors!");

		URLResourceRepository rr;
		try {
			rr = new URLResourceRepository(_systemRR.toURI());
		} catch (ResourceRepositoryException ex) {
			throw new BuildException("Error opening system resource repository " + _systemRR);
		}

    URL wsdl = rr.resolveURI(sd.getWsdlUri());
		if (wsdl == null) {
			throw new BuildException("Unable to resolve system WSDL URI " + sd.getWsdlUri() + " in repository " + _systemRR + " !");
    }
    try {
      rr.close();
    }
    catch (IOException ioex) {
      // ignore
    }

    ZipFileSet fs = new ZipFileSet();
		fs.setIncludes(_pxeSystemXml.getName());
		fs.setDir(_pxeSystemXml.getParentFile());
		fs.setFullpath(ExpandedSAR.DESCRIPTOR);
    this.addZipfileset(fs);
	}

	private void processSystemRR() throws BuildException {
		if (_systemRR == null)
			throw new BuildException("System resource repository was not specified!");
		if (!_systemRR.exists())
			throw new BuildException("System resource repository " + _systemRR + " does not exist!");
		if (!_systemRR.isDirectory())
			throw new BuildException("System resource repository " + _systemRR + " is not a directory!");
	
  	ZipFileSet systemrr = new ZipFileSet();
  	systemrr.setDir(_systemRR);
  	systemrr.setPrefix(ExpandedSAR.WSDL_RR);
  	addZipfileset(systemrr);
	}
}
