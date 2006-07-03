/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modsfwk;

import com.fs.pxe.kernel.PxeKernelModException;
import com.fs.utils.jmx.SimpleMBean;
import com.fs.utils.msg.MessageBundle;

import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

abstract class ModAbstractSfwkMBean extends SimpleMBean {

	protected static final Messages __msgs = MessageBundle.getMessages(Messages.class);

  /** Local logger. */
	protected final Log _log;

	/**
	 * @param intfClass
	 * @throws NotCompliantMBeanException
	 */
	public ModAbstractSfwkMBean(Class intfClass) throws NotCompliantMBeanException {
		super(intfClass);
		_log = LogFactory.getLog(intfClass);
	}

  @SuppressWarnings("unchecked")
	<T extends Object> T resolveInJNDI(String name, Class<T> type) throws PxeKernelModException {
    InitialContext ctx;
    try {
      ctx = new InitialContext();
    } catch (NamingException ex) {
      final String errmsg = __msgs.msgInitialContextError();
      _log.error(errmsg,ex);
      throw new PxeKernelModException(errmsg,ex);
    }

    Object obj;
    try {
      obj = ctx.lookup(name);
    } catch (NamingException e) {
      final String s = __msgs.msgJndiLookupError(name);
      _log.error(s, e);
      throw new PxeKernelModException(s, e);
    } finally {
      try {
        ctx.close();
      } catch (Exception ex) {
        // ignore
      }
    }

    if (!type.isAssignableFrom(obj.getClass())) {
      final String errmsg = __msgs.msgWrongObjectInJndi(name, type.getName());
      _log.error(errmsg);
      throw new PxeKernelModException(errmsg);
    }

    return (T)obj;
  }
	
	protected ObjectName createObjectName() {
    return null;
  }
	
}
