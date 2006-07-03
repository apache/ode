/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.mngmt;

import com.fs.pxe.sfwk.mngmt.SystemAdminMBean;
import com.fs.pxe.tools.CommandContext;
import com.fs.pxe.tools.ExecutionException;

import java.lang.reflect.UndeclaredThrowableException;

import javax.management.RuntimeOperationsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class AllSystemsCommand extends JmxCommand {

  public static final short UNDEPLOY = 0;
  public static final short ACTIVATE = 1;
  public static final short DEACTIVATE = 2;
  
  private short _operation = -1;
  
  public void setOperation(short op) {
    _operation = op;
  }
  
  public String getOperationName() {
    switch (_operation) {
    case UNDEPLOY: return "undeploy";
    case ACTIVATE: return "activate";
    case DEACTIVATE: return "deactivate";
    default: throw new IllegalStateException("Illegal value " + _operation +
        " for operation.");
    }
  }
  
  private void validate() throws ExecutionException {
    if (getDomain() == null) {
      if (getDomainUuid() == null) {
        throw new ExecutionException(__msgs.msgNoDomainFound());
      }
      throw new ExecutionException(__msgs.msgNoDomainFound(getDomainUuid()));
    }    
  }
  
  public void execute(CommandContext c) throws ExecutionException {
    validate();
    doCommand(getSystems());
  }
  
  private static final Log __log = LogFactory.getLog(AllSystemsCommand.class);
  
  private void doCommand(SystemAdminMBean[] systems) throws ExecutionException {
    String op = getOperationName();
    for (int i=0; i < systems.length; ++i) {
      String name = systems[i].getName();
      try {
        switch (_operation) {
        case UNDEPLOY:
          systems[i].undeploy();
          break;
        case ACTIVATE:
          systems[i].enable();
          break;
        case DEACTIVATE:
          systems[i].disable();
          break;
        default: throw new IllegalStateException("Illegal value " + _operation +
            " for operation.");
        }
      } catch (RuntimeOperationsException roe) {
        ExecutionException ee;
        RuntimeException re = roe.getTargetException();
        if (re != null) {
          if (re instanceof UndeclaredThrowableException) {
            Throwable cause = ((UndeclaredThrowableException)re).getCause();
            if (cause != null) {
              ee = new ExecutionException(cause.getMessage(),cause);
              continue;
            }
          }
          ee = new ExecutionException(re.getMessage(),re);
        } else {
          ee = new ExecutionException(roe.getMessage(),roe);
        }
        __log.error("Unable to " + op + " system \"" + name + "\": " + 
            ee.getMessage(),ee);
      } catch (Exception e) {
        ExecutionException ee;
        ee = new ExecutionException (e);
        __log.error("Unable to " + op + " system \"" + name + "\": " + 
            ee.getMessage(),ee);        
      }
    }
    
  }
}
