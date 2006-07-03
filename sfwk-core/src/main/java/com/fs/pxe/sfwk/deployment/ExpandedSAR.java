/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment;

import com.fs.pxe.sfwk.deployment.som.SystemDescriptor;
import com.fs.pxe.sfwk.rr.RepositoryWsdlLocator;
import com.fs.pxe.sfwk.rr.ResourceRepository;
import com.fs.pxe.sfwk.rr.ResourceRepositoryException;
import com.fs.pxe.sfwk.rr.URLResourceRepository;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Expanded system archive.
 */  
public class ExpandedSAR implements Closeable {
  /** Our logger. */
  private static final Log __log = LogFactory.getLog(ExpandedSAR.class);

  /** The path (within the SAR file) to the system descriptor.  */
  public static final String DESCRIPTOR = "PXE-INF/pxe-system.xml";

  /** The WSDL resource repository directory. */
  public static final String WSDL_RR = "PXE-INF/system.rr";

  /** Base directory for the SAR contents. */
  private File _baseDir;

  private SystemDescriptor _sd;
  private URLResourceRepository _rr;
  private Definition _definition;

  /**
   * <p>
   * Construct a new instance by exploding the contents of an existing
   * <code>SarFile</code>.
   * </p>
   * 
   * @param sf the <code>SarFile</code> that will provide the initial structure
   * @throws IOException if one occurs while unpacking the <code>SarFile</code>
   */
  public ExpandedSAR(File sf) throws IOException, SarFormatException {
    if (!sf.exists()) {
      throw new FileNotFoundException("File does not exist: " + sf);
    }
    if (!sf.isDirectory()) {
      throw new FileNotFoundException("Not a directory: " + sf);
    }

    _baseDir  = sf;
    init();
  }

  protected void finalize() throws Throwable {
    this.close();
    super.finalize();
  }

  /**
   * Initialize the object.
   * @throws SarFormatException
   * @throws IOException
   */
  private void init() throws SarFormatException, IOException {
    File desc = new File(_baseDir, DESCRIPTOR);
    if (!desc.exists()) {
      String msg = "SAR missing system deployment descriptor.";
      FileNotFoundException fnf = new FileNotFoundException(desc.toString());
      __log.error(msg,fnf);
      throw new SarFormatException(msg, fnf);
    }

    FileInputStream fis = new FileInputStream(desc);
    _sd = SystemDescriptorSerUtility.toSystemDescriptor(fis);
    fis.close();

    File rrfile = new File(_baseDir, WSDL_RR);
    if (!rrfile.exists()) {
      String msg = "SAR missing system resource repository catalog.";
      FileNotFoundException fnf = new FileNotFoundException(rrfile.toString());
      __log.error(msg,fnf);
      throw new SarFormatException(msg, fnf);
    }

    try {
      _rr = new URLResourceRepository(rrfile.toURI());
    } catch (ResourceRepositoryException e) {
      String msg = "Error loading WSDL resource repository at " + rrfile.toURI();
      __log.error(msg, e);
      throw new SarFormatException(msg, e);
    }

    WSDLLocator wsdlLocator = new RepositoryWsdlLocator(_rr, _sd.getWsdlUri());
    try {
      WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
      wsdlReader.setFeature("javax.wsdl.verbose", false);
      _definition = wsdlReader.readWSDL(wsdlLocator);
    } catch (Exception ex) {
      String msg = "Error parsing SAR's WSDL.";
      __log.error(msg, ex);
      throw new SarFormatException(msg, ex);
    }
  }

  /**
   * Closing this SAR will release all underlying resources & invalidate it.
   */
  public void close() throws IOException {
    _rr.close();
    _definition = null;
    _rr = null;
    _sd = null;
  }

  /**
   * <p>
   * Get the parsed system descriptor (<code>pxe-system.xml</code> file) for this
   * system deployment bundle. This is the most obvious resource required for
   * deployment. Note that this is the <em>original</em> deployment
   * descriptor as defined by the user (i.e. it has not been modified in the
   * deployment process).
   * </p>
   * @return parsed system descriptor
   */
  public SystemDescriptor getDescriptor() {
    return _sd;
  }

  /**
   * Get the URL to the system descriptor (<code>pxe-system.xml</code> file).
   * @see #getDescriptor()
   * @return system descriptor URL.
   * @throws MalformedURLException
   */
  public URL getDescriptorURL() throws MalformedURLException  {
    return new File(_baseDir, DESCRIPTOR).toURL();
  }

  public URL getResource(String name) throws IOException {
    return new File(_baseDir, name).toURL();
  }

  /**
   * Get the WSDL definition for this system.
   * @return a {@link Definition} representing the system WSDL.
   */
  public Definition getDefinition() {
    return _definition;
  }

  /**
   * Get the base directory for the system archive.
   * @return directory where files are located
   */
  public File getBaseDir() {
    return _baseDir;
  }

  /**
   * Get the XML resource repository against which the system
   * WSDL is resolved.
   * @return system {@link ResourceRepository}
   */
  public URLResourceRepository getSystemResourceRepository() {
    return _rr;
  }
}