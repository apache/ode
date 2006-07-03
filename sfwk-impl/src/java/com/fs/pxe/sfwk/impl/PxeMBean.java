/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.utils.jmx.SimpleMBean;

import javax.management.NotCompliantMBeanException;

abstract class PxeMBean extends SimpleMBean {

  protected PxeMBean(Class intfClass) throws NotCompliantMBeanException {
    super(intfClass);
  }

}
