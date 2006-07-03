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

public class Deactivate extends SystemCommand {

  public void execute(CommandContext c) throws ExecutionException {
    validate();
    SystemAdminMBean sys = getSystem();
    try {
      sys.disable();
    } catch (RuntimeOperationsException roe) {
      RuntimeException re = roe.getTargetException();
      if (re != null) {
        if (re instanceof UndeclaredThrowableException) {
          Throwable cause = ((UndeclaredThrowableException)re).getCause();
          if (cause != null) {
            throw new ExecutionException(cause.getMessage(),cause);
          }
        }
        throw new ExecutionException(re.getMessage(),re);
      }
      throw new ExecutionException(roe.getMessage(),roe);
    } catch (Exception e) {
      throw new ExecutionException(e);
    }
  }
}
