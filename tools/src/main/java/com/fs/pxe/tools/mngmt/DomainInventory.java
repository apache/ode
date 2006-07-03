/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.mngmt;

import com.fs.pxe.sfwk.mngmt.ServiceAdminMBean;
import com.fs.pxe.sfwk.mngmt.SystemAdminMBean;
import com.fs.pxe.tools.CommandContext;
import com.fs.pxe.tools.ExecutionException;

import java.lang.reflect.UndeclaredThrowableException;

import javax.management.RuntimeOperationsException;

public class DomainInventory extends JmxCommand {

  public static final short TERSE = 0;
  public static final short NORMAL = 1;
  public static final short VERBOSE = 2;
  public static final short VERYVERBOSE = 3;
  
  private static final double ln10 = Math.log(10d);
  private short _verbosity = NORMAL;
    
  private void validate() throws ExecutionException {
    if (getDomain() == null) {
      if (getDomainUuid() == null) {
        throw new ExecutionException(__msgs.msgNoDomainFound());
      }
      throw new ExecutionException(__msgs.msgNoDomainFound(getDomainUuid()));
    }    
  }
  
  public void setVerbosity(short v) {
    _verbosity = v;
  }
  
  public void execute(CommandContext c) throws ExecutionException {
    validate();
    SystemAdminMBean[] systems = getSystems();
    boolean[] actives = new boolean[systems.length];
    String[] names = new String[systems.length];
    int activeCount = 0;
    try {
      for (int i=0; i < systems.length; ++i) {
        actives[i] = systems[i].isEnabled();
        if (actives[i]) {
          ++activeCount;
        }
        names[i] = systems[i].getName();
      }
    } catch (RuntimeOperationsException roe) {
      RuntimeException re = roe.getTargetException();
      if (re != null) {
        if (re instanceof UndeclaredThrowableException) {
          Throwable cause = ((UndeclaredThrowableException)re).getCause();
          if (cause != null) {
            throw new ExecutionException(cause);
          }
        }
        throw new ExecutionException(re);
      }
      throw new ExecutionException(roe);
    } catch (Exception me) {
      throw new ExecutionException(me);
    }

    c.outln("Domain: " + getDomainUuid() + " (" + systems.length + " system" +
            (systems.length==1?"":"s") + (systems.length==0?"":(", " + activeCount +
            " active")) + ")");
    if (_verbosity < NORMAL || systems.length == 0) return;
    
    int w = (int) Math.round(Math.ceil(Math.log(systems.length) / ln10)); 
    
    for (int i=0; i < systems.length; ++i) {
      if (_verbosity > NORMAL && i!=0) {
        c.outln("");
      }
      c.outln(
          lPad(' ',String.valueOf(i),w+1) + ":  " +
          (actives[i]?"+":"-") + "  " + 
          names[i]);
      if (_verbosity > NORMAL) {
        ServiceAdminMBean[] svcs = getServices(systems[i]);
     
        for (int j=0; j<svcs.length; ++j) {
          c.outln(lPad(' ',String.valueOf(j),w+3) + ":: " + svcs[j].getName() + " [" + 
              svcs[j].getServiceProviderURI() + "]");
        }
      }
    }
  }
  
  private String lPad(char c, String s, int w) {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i < w-s.length(); ++i) {
      sb.append(c);
    }
    sb.append(s);
    return sb.toString();
  }
}
