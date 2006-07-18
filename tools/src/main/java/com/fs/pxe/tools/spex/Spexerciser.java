/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.spex;

import com.fs.pxe.bpel.test.BpelTestDef;
import com.fs.pxe.bpel.test.BpelTestParser;
import com.fs.pxe.ra.PxeConnection;
import com.fs.pxe.ra.PxeConnectionFactory;
import com.fs.pxe.ra.PxeManagedConnectionFactory;
import com.fs.pxe.tools.CommandContext;
import com.fs.pxe.tools.ExecutionException;
import com.fs.pxe.tools.mngmt.JmxCommand;
import com.fs.utils.rmi.RMIConstants;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

import javax.resource.ResourceException;

import junit.framework.*;
import junit.textui.ResultPrinter;
import junit.textui.TestRunner;

public class Spexerciser extends JmxCommand {
 
  private boolean _verbose;
  private boolean _quiet;
  private String _pxeConnectionUrl = RMIConstants.getConnectionURL();
  private ArrayList<File> _tests = new ArrayList<File>();
  private ArrayList<String> _systems;

	public Spexerciser() {
		super();
	}

	/**
	 * @see com.fs.pxe.tools.Command#execute(com.fs.pxe.tools.CommandContext)
	 */
	public void execute(CommandContext cc) throws ExecutionException {
	  _systems = new ArrayList<String>();
    
    PxeConnection pxeConn;
    try{
      PxeManagedConnectionFactory pmcf = new PxeManagedConnectionFactory();
      pmcf.setURL(_pxeConnectionUrl);
  	  PxeConnectionFactory cf = (PxeConnectionFactory)pmcf.createConnectionFactory();
  	  pxeConn = (PxeConnection)cf.getConnection();
    }catch(ResourceException e){
      cc.error("Failure obtaining a PxeConnection", e);
      throw new ExecutionException(
          "Unable to create PxeConnection to domain at " + _pxeConnectionUrl +
          "; reason: " + e.getMessage(),e);
    }
    
    
    BpelTestParser tparser = new BpelTestParser();
    List<BpelTestDef> tests  = new ArrayList<BpelTestDef>();
    for (File test : _tests)
		try {
			tests.addAll(tparser.scanForTests(test));
		} catch (Exception e2) {
			cc.error("Error parsing BPEL unit tests at " + test,e2);
      throw new ExecutionException(e2);
		}

    TestSuite suite = new TestSuite();
    
    for(BpelTestDef def : tests){
      try {
        cc.debug("Found test " + def.bpelFile.getName() + ".  Queueing up.");
        String name = "BpelUnitTest-" + Long.toHexString(System.currentTimeMillis()) + "-" +
          def.bpelFile.getName();
        suite.addTest(new BpelUnitTest(name,def.bpelFile.getName(), def,
            pxeConn, getDomain(),getConnection()));
        _systems.add(name);
			} catch (Exception e3) {
				cc.error("Unable to deploy unit test '" + def.bpelFile.getName() + "'", e3);
			}
    }

    TestRunner tr;
    if (getQuiet()) {
      tr = new TestRunner(new PrintStream(new DevNullOutputStream()));      
    } else {
      tr = new TestRunner(new PrettyTestPrinter(cc));
      cc.out("Running " + suite.countTestCases() + " test" + 
          (suite.countTestCases() > 1?"s:":":") + (getVerbose()?"\n":""));
    }
    TestResult tres = tr.doRun(suite);
    if (tres.wasSuccessful()) {      
      if (!getQuiet()) {
        cc.outln("");
        cc.outln("OK (" + tres.runCount() + " tests passed.)");
      }
    } else {
      if (!getQuiet()) {
        cc.outln("");
      }
      cc.outln(tres.runCount() + " tests run; " + tres.errorCount() + " errors, " + 
          tres.failureCount() + " failures");
      if (getVerbose()) {
        if (tres.errorCount() > 0) {
          cc.outln(compose("Errors:",tres.errors()));
        }
        if (tres.failureCount() > 0) {
          cc.outln(compose("Failures:",tres.failures()));
        }
      }
    }
    try {
      Thread.sleep(2000);
    } catch (InterruptedException ie) {
      // ignore.
    }
    if (getVerbose()) {
      cc.out("Cleaning up: ");
    }
    
	}
  
  private String compose(String s, Enumeration en) {
    StringBuffer buf = new StringBuffer(s);
    for (;en.hasMoreElements();) {
      buf.append(' ');
      buf.append(((TestCase)((TestFailure)en.nextElement()).failedTest()).getName());
    }
    return buf.toString();
  }
  
  public boolean getVerbose() {
    return _verbose;
  }
  
  public void setVerbose(boolean flag) {
    _verbose = flag;
    if (_verbose) {
      setQuiet(false);
    }
  }
  
  public void setQuiet(boolean flag) {
    _quiet = flag;
  }
  
  public boolean getQuiet() {
    return _quiet;
  }
  
  public void addTest(File location){
    _tests.add(location);
  }
  
	public String getPxeConnectionUrl() {
		return _pxeConnectionUrl;
	}
	public void setPxeConnectionUrl(String pxeConnectionUrl) {
		_pxeConnectionUrl = pxeConnectionUrl;
	}
  
  /*
   * This is a hack to avoid JUnit's output and keep control over the verbosity.
   */
  private class PrettyTestPrinter extends ResultPrinter {
    
    CommandContext _cc;
    boolean _skipout;
    
    PrettyTestPrinter(CommandContext cc) {
      super(new PrintStream(new DevNullOutputStream()));
      _cc = cc;
    }
    
    /**
     * @see junit.framework.TestListener#addError(junit.framework.Test, java.lang.Throwable)
     */
    public void addError(Test arg0, Throwable arg1) {
      if (arg0 instanceof TestCase) {
        TestCase tc = (TestCase) arg0;
        if (arg1 instanceof BpelUnitTest.RuntimeBpelUnitTestException) {
          BpelUnitTest.RuntimeBpelUnitTestException butrbrute =
            (BpelUnitTest.RuntimeBpelUnitTestException) arg1;
          if (getVerbose()) {
            _cc.outln("[ " + tc.getName() + " BPEL ERROR -- " + butrbrute.getMessage() + " ]");
          } else if (!getQuiet()) {
            _cc.out("[BPEL ERROR in " + tc.getName() + ":: " + butrbrute.getMessage() + "]");
          }
        } else if (arg1.getCause() == null) {
          StackTraceElement ste = arg1.getStackTrace()[0];
          if (getVerbose()) {
            _cc.outln("[ ERROR in " + ((TestCase)arg0).getName() + "  -- " + 
                arg1.getClass().getName() + "@" +  ste.getClassName() + "#"+ ste.getMethodName() + ", " + ste.getFileName() +
                ":" + ste.getLineNumber() + "-- " + arg1.getMessage() + " ]");
          } else if (!getQuiet()) {
            _cc.out("[ERROR in " + ((TestCase)arg0).getName() + ":: " + arg1.getClass().getName() + "@" +
                ste.getClassName() + "#"+ ste.getMethodName() + ", " + ste.getFileName() +
                ":" + ste.getLineNumber() + " " + arg1.getMessage()+ "]");
          }
        } else if (arg1.getCause() != null) {
          addError(arg0,arg1.getCause());
        }
      }
      _skipout = true;
    }
    
    /**
     * @see junit.framework.TestListener#addFailure(junit.framework.Test, junit.framework.AssertionFailedError)
     */
    public void addFailure(Test arg0, AssertionFailedError arg1) {
      if (arg0 instanceof TestCase) {
        if (getVerbose()) {
          _cc.outln("[ FAILURE in " + ((TestCase)arg0).getName() + "  -- " + arg1.getMessage() + " ]");
        } else if (!getQuiet()) {
          _cc.out("[FAILURE in " + ((TestCase)arg0).getName() + ":: " + arg1.getMessage() + "]");
        }
      }
      _skipout = true;
    }
    
    /**
     * @see junit.framework.TestListener#endTest(junit.framework.Test)
     */
    public void endTest(Test arg0) {
      if (arg0 instanceof TestCase) {
        if (getVerbose()) {
          _cc.outln("[ end " + ((TestCase)arg0).getName() + " ]");
        } else if (!getQuiet() && !_skipout) {
          _cc.out(".");
        }
      }
    }
    /**
     * @see junit.framework.TestListener#startTest(junit.framework.Test)
     */
    public void startTest(Test arg0) {
      if (arg0 instanceof TestCase) {
        _skipout = false;
        if (getVerbose()) {
          _cc.outln("[ start " + ((TestCase)arg0).getName() + " ]");
        }
      }
    }
  }

  
  private class DevNullOutputStream extends OutputStream {
    /**
     * @see java.io.OutputStream#write(int)
     */
    public void write(int b) throws IOException {
      // This space intentionally left blank.
    }
}
  
}
