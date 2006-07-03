/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package genbuild;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.Ant;

/**
 * Try running an Ant file, but trap BuildExceptions
 */
public class TryAnt extends Ant {
  private String _property;
  private String _failOnError;
  private String _logfile;


  public void init() {
    super.init();
    _property = null;
    _failOnError = null;
    _logfile = null;
  }
  
  /**
   * @see org.apache.tools.ant.Task#execute()
   */
  public void execute() throws BuildException {
    boolean failonerr = false;
    BuildLogger logger = null;
    PrintStream ps = null;
    Project dummy = new Project();
    if (_failOnError != null && (_failOnError.equalsIgnoreCase("yes") || _failOnError.equalsIgnoreCase("true")))
      failonerr = true;
    
    if (_logfile != null) {
      try {
        FileOutputStream fos = new FileOutputStream(_logfile);
        ps = new PrintStream(fos);
      } catch (IOException ioe) {
	throw new BuildException("Error opening XML output file: " + _logfile, ioe);
      }
      
      logger = new XmlLogger();
      logger.setMessageOutputLevel(Project.MSG_INFO);
      logger.setOutputPrintStream(ps);
      logger.buildStarted(new BuildEvent(dummy));
  
      getProject().addBuildListener(logger);
    }
    
    BuildException err = null;
    try {
      super.execute();
    } catch (BuildException be) {
      err = be;
      if (_property != null) 
        getProject().setProperty(_property, be.getMessage());
      if (failonerr)
        throw be;
      
    } finally {
      if (logger != null) {
        BuildEvent be = new BuildEvent( dummy );
        if (err != null) {
          be.setException(err);
          be.setMessage(err.getMessage(), Project.MSG_ERR);
        }
        logger.buildFinished(be);
        getProject().removeBuildListener(logger);
        ps.flush();
        ps.close();
      }
    }
  }
  
  public void setProperty(String property) {
    _property = property;
  }
  
  public void setFailonerror(String failonerror) {
    _failOnError = failonerror;
  }
  
  public void setLogfile(String logfile) {
    _logfile = logfile;
  }

}
