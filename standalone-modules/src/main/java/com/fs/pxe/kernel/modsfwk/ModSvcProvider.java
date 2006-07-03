/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.kernel.modsfwk;

import com.fs.pxe.kernel.PxeKernelModException;
import com.fs.pxe.sfwk.spi.ServiceProvider;


import java.util.*;
import javax.management.NotCompliantMBeanException;


import org.apache.commons.collections.BeanMap;

public class ModSvcProvider extends ModAbstractSfwkMBean implements ModSvcProviderMBean {

  static Map<String, ServiceProvider> __svcProviders = Collections.synchronizedMap(new HashMap<String,ServiceProvider>());

	private ClassLoader _svcProviderCL = getClass().getClassLoader();
		
	private String _providerUri;
	private String _providerClass;
	private Properties _properties = new Properties();

  /**
	 * @throws NotCompliantMBeanException 
	 */
	public ModSvcProvider() throws NotCompliantMBeanException {
		super(ModSvcProviderMBean.class);
	}

	/**
	 * @see com.fs.pxe.kernel.modsfwk.ModSvcProviderMBean#setProviderURI(java.lang.String)
	 */
	public void setProviderURI(String uri) {
		_providerUri = uri;
	}

	/**
	 * @see com.fs.pxe.kernel.modsfwk.ModSvcProviderMBean#getProviderURI()
	 */
	public String getProviderURI() {
		return _providerUri;
	}

	/**
	 * @see com.fs.pxe.kernel.modsfwk.ModSvcProviderMBean#setProviderClass(java.lang.String)
	 */
	public void setProviderClass(String cls) {
		_providerClass = cls;
	}

	/**
	 * @see com.fs.pxe.kernel.modsfwk.ModSvcProviderMBean#getProviderClass()
	 */
	public String getProviderClass() {
		return _providerClass;
	}

	/**
	 * @see com.fs.pxe.kernel.modsfwk.ModSvcProviderMBean#setProviderProperties(java.lang.String)
	 */
	public void setProviderProperties(String properties) {
		StringTokenizer st = new StringTokenizer(properties, ",;");
		while(st.hasMoreTokens()){
			String tok = st.nextToken();
			int idx = tok.indexOf('=');
			if(idx == -1)
				throw new IllegalArgumentException("Bad provider property: expected '='");
			_properties.put(tok.substring(0, idx), tok.substring(idx+1, tok.length()));
		}
	}

	/**
	 * @see com.fs.pxe.kernel.modsfwk.ModSvcProviderMBean#getProviderProperties()
	 */
	public String getProviderProperties() {
		StringBuffer sb = new StringBuffer();
		for(Iterator i = _properties.entrySet().iterator(); i.hasNext(); ){
			Map.Entry e = (Map.Entry)i.next();
			if(sb.length() != 0) {
				sb.append(",");
      }
			sb.append(e.getKey());
			sb.append("=");
			sb.append(e.getValue());
		}
		return sb.toString();
	}

	/**
	 * @see com.fs.pxe.kernel.PxeKernelModMBean#start()
	 */
	public void start() throws PxeKernelModException {
		if(_providerUri == null)
			throw new PxeKernelModException("Missing required attribute 'ProviderUri'");
		if(_providerClass == null)
			throw new PxeKernelModException("Missing required attribute 'ProviderClass'");
    
    ServiceProvider svcProvider;
    Class svcProviderClass;
    try{
    	svcProviderClass = _svcProviderCL.loadClass(_providerClass);
    	svcProvider = (ServiceProvider)svcProviderClass.newInstance();
    }catch (ClassNotFoundException e) {
    	String msg = __msgs.msgPxeServiceProviderClassNotFound(_providerClass);
      _log.error(msg, e);
    	throw new PxeKernelModException(msg);
    }catch(InstantiationException e){
    	String msg = __msgs.msgServiceProviderInstantiationException(_providerClass);
    	_log.error(msg, e);
    	throw new PxeKernelModException(msg);
    } catch (IllegalAccessException e) {
    	String msg = __msgs.msgServiceProviderInstantiationException(_providerClass);
    	_log.error(msg, e);
    	throw new PxeKernelModException(msg);
		}

    try {
      BeanMap providerBeanMap = new BeanMap(svcProvider);
      providerBeanMap.putAll(_properties);
    } catch (Exception ex) {
      String msg = __msgs.msgServiceProviderConfigError(_providerClass,ex.getMessage());
      _log.error(msg,ex);
      throw new PxeKernelModException(msg,ex);
    }
    
	   __svcProviders.put(_providerUri, svcProvider);    
	}

	/**
	 * @see com.fs.pxe.kernel.PxeKernelModMBean#stop()
	 */
	public void stop() throws PxeKernelModException {
		
	}
 }
