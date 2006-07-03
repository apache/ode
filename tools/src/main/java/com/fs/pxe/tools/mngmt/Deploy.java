/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.mngmt;

import com.fs.pxe.sfwk.mngmt.DomainAdminMBean;
import com.fs.pxe.sfwk.mngmt.SystemAdminMBean;
import com.fs.pxe.tools.CommandContext;
import com.fs.pxe.tools.ExecutionException;

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;

import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;

public class Deploy extends JmxCommand {
  
  private String _sarUrl;
  private boolean _activate = true;
  private File _sar;
  
  public void setSarFile(String s) {
    _sar = new File(s);
  }
  
  public File getSarFile() {
    return _sar;
  }
  
  public String getSarUrl() {
    return _sarUrl;
  }
  
  public void setSarUrl(String sarUrl) {
    _sarUrl = sarUrl;
  }
  
  public void setActivateOnDeploy(boolean activate) {
    _activate = activate;
  }
  
  public boolean getActivateOnDeploy() {
    return _activate;
  }
  
  protected void validate() throws ExecutionException {
    if (_sarUrl == null && _sar == null) {
      throw new ExecutionException(__msgs.msgSarOrUrlIsRequired());
    }
    if (_sar != null) {
      if (!_sar.exists() || !_sar.canRead() || _sar.isDirectory()) {
        throw new ExecutionException(__msgs.msgSarMustBeReadable(_sar.getPath()));
      }
    }
    if (getDomain() == null) {
      if (getDomainUuid() == null) {
        throw new ExecutionException(__msgs.msgNoDomainFound());
      }
      throw new ExecutionException(__msgs.msgNoDomainFound(getDomainUuid()));
    }
  }
  
  public void execute(CommandContext c) throws ExecutionException {
    validate();
    DomainAdminMBean db = getDomain();
    assert db != null;
    
    ObjectName on;
    try {
      if (_sarUrl != null) {
        on = db.deploySystem(_sarUrl, true);
      } else if (_sar != null) {
        // TODO: Fix this so it works across servers.
        on = db.deploySystem(_sar.getCanonicalFile().toURI().toURL().toExternalForm(), true);
      } else
        throw new ExecutionException(__msgs.msgSarOrUrlIsRequired());

    } catch (MalformedURLException e) {
      throw new ExecutionException(__msgs.msgBadSarUrl(getSarUrl(),e.getMessage()), e);
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
      throw new ExecutionException(
          __msgs.msgDeploymentException(getDomainUuid(),
              (getSarUrl()!=null?getSarUrl():getSarFile().getPath()),e.getMessage()),
          e);
    }
    
    String n = on.getKeyProperty("system");
    
    c.info(__msgs.msgSuccessDeployingSystem(getDomainUuid(),n));
    
    if (_activate) {
      setSystemName(n);
      SystemAdminMBean sys = getSystem();
      if (sys == null) {
        throw new ExecutionException("Internal error; supposedly deployed system not found.");
      }
      c.info(__msgs.msgSystemActivated(getDomainUuid(),n));
      try {
        sys.enable();
      } catch (Exception me) {
        throw new ExecutionException(me.getMessage(),me);
      }
    }
  }
}
