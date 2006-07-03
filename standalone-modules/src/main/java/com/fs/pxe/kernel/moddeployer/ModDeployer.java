/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.moddeployer;

import com.fs.pxe.sfwk.mngmt.DomainAdminMBean;
import com.fs.pxe.sfwk.mngmt.ManagementException;
import com.fs.pxe.sfwk.mngmt.SystemAdminMBean;
import com.fs.utils.jmx.SimpleMBean;

import java.io.*;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.management.MBeanServerInvocationHandler;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class implementing a live deployment directory. SAR files placed
 * within the same directory are deployed automatically.
 */
public class ModDeployer  extends SimpleMBean implements ModDeployerMBean {
	/** Class-level logger. */
  private static Log __log = LogFactory.getLog(ModDeployer.class);

  /**
   * Name of file within the deployment directory that holds information
   * about previously deployed objects.
   */
  private static final String DEPLOY_STORE_NAME = ".deployed.props";

  /** The polling interval. */
  private static final long POLL_TIME = 5000;

  /** The deployment directory (the one we are managing). */
  private File _deployDir;

  /** Thread to do the timing. */
  private Poller _poller;

  /** Filter accepting <code>.sar</code> and <code>.jar</code> files. */
  private static final FileFilter _fileFilter = new FileFilter(){
		public boolean accept(File pathname) {
      String name = pathname.getName().toLowerCase(); 
			return name.toLowerCase().endsWith(".sar");
		}
  };

  /**
   * Set of {@link FileInfo} objects regarding all files that have
   * been inspected.
   */
  private final HashSet<FileInfo> _inspectedFiles = new HashSet<FileInfo>();

  private String _deploydir = "etc/deploy";
  private ObjectName _domainAdminMBeanName;

  private DomainAdminMBean _domainAdminMBean;

  /**
   * Constructor.
   */
  public ModDeployer() throws NotCompliantMBeanException {
      super(ModDeployerMBean.class);
  }


  public String getDeployDir() {
    return _deploydir;
  }

  public void setDomainAdminMBean(ObjectName oname) {
    _domainAdminMBeanName = oname;
  }

  public ObjectName getDomainAdminMBean() {
    return _domainAdminMBeanName;
  }


  public void setDeployDir(String deployDir) {
    _deploydir = deployDir;
  }


  public void start() {
    _domainAdminMBean = resolveJmx(_domainAdminMBeanName, DomainAdminMBean.class);

    _deployDir = new File(_deploydir);
    if (!_deployDir.exists())
      _deployDir.mkdir();
    
    loadInfoStore();

    _poller = new Poller();
    _poller.start();
    __log.info("Poller started.");
  }

  public void stop() {
    _poller.kill();
    _poller = null;
  }


  /** Scan the directory for new files (called mainly from {@link Poller}). */
  void check(){
  	File[] files = _deployDir.listFiles(_fileFilter);

    if (files != null)
      for(int i = 0 ; i < files.length; ++i){
        if(checkIsNew(files[i])){
          _inspectedFiles.add(new FileInfo(files[i]));
          writeInfoStore();

          if(!files[i].getName().toLowerCase().endsWith(".sar"))
            continue;

          if (!handleDeploymentBundle(files[i])){
            __log.error("Error deploying file " + files[i].getName() + ".  Ignoring...");
          }
        }
      }
  }

  /**
   * Check if a file has not been seen before.
   * @param f file to check
   * @return <code>true</code> if new
   */
  private boolean checkIsNew(File f){
    for(Iterator<FileInfo> iter = _inspectedFiles.iterator(); iter.hasNext(); ){
  		FileInfo deployed = iter.next();
  		if(deployed.matches(f))
        return false;
    }
    return true;
  }

  
  private boolean handleDeploymentBundle(File sar){
      
    try {
      ObjectName systemName = _domainAdminMBean.deploySystem(sar.toURL().toExternalForm(), true);
      SystemAdminMBean system =  resolveJmx(systemName, SystemAdminMBean.class);
     	system.enable();
    } catch (MalformedURLException mue) {
      return false;
    } catch (ManagementException mex) {
      return false;
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  private <T extends Object> T resolveJmx(ObjectName oname, Class<T> mbeanClass) {
    return (T)MBeanServerInvocationHandler.newProxyInstance(_mbeanServer,oname, mbeanClass, true);
  }

  private void writeInfoStore(){
    FileWriter fw = null;
    try{
      fw = new FileWriter(new File(_deployDir, DEPLOY_STORE_NAME));

      for(Iterator<FileInfo> iter = _inspectedFiles.iterator(); iter.hasNext(); ){
        FileInfo fi = iter.next();
        fw.write(fi.toInfoString());
        fw.write("\n");
      }
      fw.flush();
    }catch(IOException e){
      __log.error("Error writing deployed store file '" + DEPLOY_STORE_NAME + "'", e);
    }finally{
      if(fw != null){
        try {
          fw.close();
        } catch (IOException e1) {
          // nothing to do
          e1.printStackTrace();
        }
      }
    }
  }

  private void loadInfoStore(){
    File f = new File(_deployDir, DEPLOY_STORE_NAME);
    if(!f.exists())
      return;
    LineNumberReader lnr;
    try {
      lnr =  new LineNumberReader(new FileReader(f));
    } catch (FileNotFoundException fnfe) {
      // Nothing to do if the file is not found...
      return;
    }

    String line = null;
    try{
      while((line = lnr.readLine()) != null && !line.trim().equals("")) {
        try {
          _inspectedFiles.add(FileInfo.fromInfoString(line));
        } catch (RuntimeException re) {
          __log.fatal("Invalid entry (ignoring): " + line, re);
        }
      }
    }catch(IOException e){
      __log.error("Error reading deployed store file '" + DEPLOY_STORE_NAME + "'", e);
    }finally{
      try {
        lnr.close();
      } catch (IOException e1) {
        // ignore
      }
    }
  }

  protected ObjectName createObjectName() {
    return null;
  }

  /**
   * Thread that does the actual polling for new files.
   */
  private class Poller extends Thread {
    private boolean _active = true;

    /** Stop this poller, and block until it terminates. */
    void kill() {
      synchronized(this) {
        _active = false;
        this.notifyAll();
      }
      try {
        join();
      } catch (InterruptedException ie) {
        __log.fatal("Thread unexpectedly interrupted.", ie);
      }
    }

  	public void run(){
  		try{
    		while(_active){
          check();
    			synchronized(this){
            try{
              this.wait(POLL_TIME);
            }catch(InterruptedException e){}
          }
        }
      }catch(Throwable t){
      	__log.fatal("Encountered an unexpected error.  Exiting poller...", t);
      }
    }

  }

  /**
   * Information about scanned file.
   */
  private static class FileInfo {
    /** Last modified date. */
  	private final long lastModified;

    /** File name. */
    private final String name;

    /** File size. */
    private final long size;

    private FileInfo(File f){
    	lastModified = f.lastModified();
      name = f.getName();
      size = f.length();
    }
    
    private FileInfo(String name, long size, long lastModified){
    	this.name = name;
      this.size = size;
      this.lastModified = lastModified;
    }
    
    public boolean matches(File f){
    	return f.lastModified() == lastModified
        && f.length() == size
        && f.getName().equals(name);
    }
    
    public int hashCode(){
    	return name.hashCode() + Long.valueOf(lastModified).hashCode() + Long.valueOf(size).hashCode();
    }

    public String toString() {
      return "{FileInfo " + toInfoString() + "}";
    }

    /** Convert to string representation suitable for writing to file. */
    public String toInfoString() {
      StringBuffer sb = new StringBuffer();
      sb.append(Long.toString(lastModified));
      sb.append(';');
      sb.append(Long.toString(size));
      sb.append(';');
      sb.append(name);
      return sb.toString();
    }

    /** Convert from string representation. */
    static FileInfo fromInfoString(String infoString) {
      StringTokenizer st = new StringTokenizer(infoString, ";\n", false);
      if(!st.hasMoreTokens())
        throw new IllegalArgumentException("Invalid argument: " +  infoString);
      long modified = Long.parseLong(st.nextToken());
      if(!st.hasMoreTokens())
        throw new IllegalArgumentException("Invalid argument: " +  infoString);
      long size = Long.parseLong(st.nextToken());
      if(!st.hasMoreTokens())
        throw new IllegalArgumentException("Invalid argument: " +  infoString);
      String name = st.nextToken();
      return new FileInfo(name,size,modified);
    }
  }
  
}
