/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modsfwk;

import com.fs.pxe.kernel.PxeKernelModMBean;

public interface ModSvcProviderMBean extends PxeKernelModMBean{
	
	public void setProviderURI(String uri);
	
	public String getProviderURI();
	
	public void setProviderClass(String cls);
	
	public String getProviderClass();
	
	public void setProviderProperties(String properties);
	
	public String getProviderProperties();
	
}
