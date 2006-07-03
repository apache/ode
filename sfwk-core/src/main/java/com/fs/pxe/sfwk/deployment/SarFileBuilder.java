/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment;

import com.fs.pxe.sfwk.deployment.som.SystemDescriptor;
import com.fs.pxe.sfwk.deployment.som.sax.SystemDescriptorFactory;
import com.fs.pxe.sfwk.rr.ResourceRepository;
import com.fs.pxe.sfwk.rr.ResourceRepositoryException;
import com.fs.pxe.sfwk.rr.URLResourceRepository;
import com.fs.utils.StreamUtils;
import com.fs.utils.fs.FileUtils;
import com.fs.utils.fs.TempFileManager;
import com.fs.utils.sax.FailOnErrorErrorHandler;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Class used to build SAR files.
 */
public class SarFileBuilder {
  private static Log __log = LogFactory.getLog(SarFileBuilder.class);

  private File _baseDir;
  private boolean _temporary = false;
  private File _rr;

  /**
   * Create a builder using a temporary directory.
   */
  public SarFileBuilder() {
    _baseDir = TempFileManager.getTemporaryDirectory(getClass().getName());
    _temporary = true;
  }

  // just in case someone forgets to cleanup() manually..
  protected void finalize() throws Throwable {
    this.cleanup();
    super.finalize();
  }

  /**
   * Create a builder using the given (non-temporary) directory.
   * @param baseDir
   */
  public SarFileBuilder(File baseDir) {
    _baseDir = baseDir;
  }

  /**
   * Set the <code>SystemDescriptor</code> instance for the bundle.
   * @param sd the <code>SystemDescriptor</code> to use.
   * @throws java.io.IOException if one occurs while writing out the descriptor into the
   * exploded store.
   */
  public void setSystemDescriptor(SystemDescriptor sd) throws IOException {
    File f = new File(_baseDir, ExpandedSAR.DESCRIPTOR);
    f.getParentFile().mkdirs();
    OutputStream fos = new BufferedOutputStream(new FileOutputStream(f));
    try {
      SystemDescriptorSerUtility.toOutputStream(sd,fos);
    } finally {
      fos.close();
    }
  }

  /**
   * Set the <code>SystemDescriptor</code> instance for the bundle from the contents
   * of a <code>File</code>.
   *
   * @param f the <code>File</code> to read the descriptor from.
   * @throws IOException if one occurs while reading the <code>File</code> or writing
   * the descriptor into the exploded store.
   * @see #setSystemDescriptor(URL)
   */
  public void setSystemDescriptor(File f) throws IOException {
    setSystemDescriptor(f.toURI().toURL());
  }

  /**
   * Set the <code>SystemDescriptor</code> instance for the bundle from the contents
   * of a <code>URL</code>.
   *
   * @param u the <code>URL</code> to read the descriptor from.
   * @throws IOException if one occurs while reading the <code>URL</code> or writing
   * the descriptor into the exploded store.
   * @see #setSystemDescriptor(File)
   */
  public void setSystemDescriptor(URL u) throws IOException {
    removeSystemDescriptor();
    FileOutputStream fos = new FileOutputStream(new File(_baseDir, ExpandedSAR.DESCRIPTOR)); 
    StreamUtils.copy(fos, u);
    fos.close();
  }

  /**
   * Remove the system descriptor.
   */
  public void removeSystemDescriptor() {
    File file = new File(_baseDir, ExpandedSAR.DESCRIPTOR);
    if (!FileUtils.deepDelete(file)) {
      __log.warn("removeSystemDescriptor: failed to delete " + file.getAbsolutePath());
    }
  }

  /**
   * Remove a resource.
   * @param name resource name
   */
  public void removeResource(String name) {
    File file = new File(_baseDir, name);
    if (!FileUtils.deepDelete(file)) {
      __log.warn("removeResource: failed to delete " + file.getAbsolutePath());
    }
  }

  /**
   * Cleanup any temporary files; object should not be used after this call.
   */
  public  void cleanup() {
    if (_temporary && _baseDir != null) {
      if (!FileUtils.deepDelete(_baseDir)) {
        __log.warn("cleanup: failed to delete " + _baseDir.getAbsolutePath());
      }
    }
    _baseDir = null;
    _temporary = false;
  }

  /**
   * <p>
   * Validate the system descriptor in the context of the common resource
   * repository, throwing an exception if validation fails.
   * </p>
   *
   * @throws SAXException
   *           if a validation error occurs
   * @throws IOException
   *           if an I/O error occurs while reading a resource.
   * @see FailOnErrorErrorHandler
   * @see SystemDescriptorFactory#parseDescriptor(InputSource, ErrorHandler,
   *      ResourceRepository, boolean)
   * @see SystemDescriptorFactory#parseDescriptor(URL, ErrorHandler,
   *      ResourceRepository, boolean)
   */
  public void validate() throws SAXException, IOException, SarFormatException, ResourceRepositoryException {
    validate(new FailOnErrorErrorHandler());
  }

  /**
   * <p>
   * Validate the system descriptor in the context of the common resource
   * repository, using the supplied <code>ErrorHandler</code> implementation
   * to handle any validation-related errors that occur.
   * </p>
   *
   * @param eh
   *          the <code>ErrorHandler</code> to use for validation or parsing
   *          errors.
   * @throws SAXException
   *           if a validation error occurs (that the <code>ErrorHandler</code>
   *           does not ignore)
   * @throws IOException
   *           if an I/O error occurs while reading a resource.
   * @see SystemDescriptorFactory#parseDescriptor(InputSource, ErrorHandler,
   *      ResourceRepository, boolean)
   * @see SystemDescriptorFactory#parseDescriptor(URL, ErrorHandler,
   *      ResourceRepository, boolean)
   */
  public void validate(ErrorHandler eh) throws SAXException,
          IOException, ResourceRepositoryException {
    URL descURL = new File(_baseDir, ExpandedSAR.DESCRIPTOR).toURL();
    if (_rr == null)
      throw new IllegalStateException("System resource repository not set. ");
    URI rrURI = _rr.toURI();
    URLResourceRepository rr = new URLResourceRepository(rrURI);
    try {
      SystemDescriptorFactory.parseDescriptor(descURL, eh, rr, true);
    } finally {
      rr.close();
    }
  }

  /**
   * Add an entry to the SAR.
   *
   * @param name
   *          the name at which the resource should be accessible.
   * @param f
   *          a <code>File</code> containing the resource.
   * @throws IOException
   *           if one occurs while reading or writing.
   */
  public void addEntry(String name, File f) throws IOException {
    addEntry(name, f.toURL());
  }

  /**
   * Add an entry to the SAR.
   *
   * @param name
   *          the name at which the resource should be accessible.
   * @param u
   *          a <code>URL</code> containing the resource.
   * @throws IOException
   *           if one occurs while reading or writing.
   */
  public synchronized void addEntry(String name, URL u) throws IOException {
    OutputStream fos = new BufferedOutputStream(new FileOutputStream(new File(_baseDir, name)));
    StreamUtils.copy(fos, u);
    fos.close();
  }

  /**
   * Pack the SAR into JAR archive format.
   * @param jarFile output file
   * @throws IOException
   */
  public void pack(File jarFile) throws IOException {
    OutputStream fos = new BufferedOutputStream(new FileOutputStream(jarFile));
    try {
      pack(fos);
    } finally {
      fos.close();
    }
  }

  /**
   * Pack the SAR into JAR archive format.
   * @param jarOs destination output stream
   * @throws IOException
   */
  public void pack(OutputStream jarOs) throws IOException {
    JarOutputStream zos = new JarOutputStream(jarOs);
    if (_rr != null) {
      ZipEntry ze = new ZipEntry(ExpandedSAR.WSDL_RR + "/");
      zos.putNextEntry(ze);
      zos.closeEntry();
      packFiles(ExpandedSAR.WSDL_RR + "/", zos, _rr);
    }
    packFiles("", zos, _baseDir);
    zos.finish();
    zos.flush();
  }

  private void packFiles(String stem, JarOutputStream zos, File dir)
      throws IOException {
    File[] ff = dir.listFiles();
    assert ff != null;
    for (int i = 0; i < ff.length; ++i) {
      if (ff[i].isDirectory()) {
        ZipEntry ze = new ZipEntry(stem + ff[i].getName() + "/");
        zos.putNextEntry(ze);
        zos.closeEntry();
        packFiles(stem + ff[i].getName() + "/", zos, ff[i]);
        continue;
      }
      ZipEntry ze = new ZipEntry(stem + ff[i].getName());
      zos.putNextEntry(ze);
      FileInputStream fis = new FileInputStream(ff[i]);
      StreamUtils.copy(zos, fis);
      zos.closeEntry();
      fis.close();
    }
  }

  /**
   * Set the location of the resource repository containing the
   * system WSDL.
   * @param rr resource repository directory
   */
  public void setWSDLResourceRepository(File rr) {
    _rr = rr;
  }
}
