/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.daohib.bpel;

import java.io.Serializable;

/**
 * Serializable class for obtain a reference to a hibernate POJO
 */
class HibernateHandle implements Serializable{

  private static final long serialVersionUID = 1L;
  private Class _daoCls;
	private Class _hibCls;
  private Serializable _id;
  /**
	 * 
	 */
	public HibernateHandle(Class daoCls, Class hibCls, Serializable id) {
		_daoCls = daoCls;
    _hibCls = hibCls;
    _id = id;
	}
  
  public Class getHibernateClass(){
  	return _hibCls;
  }
  
  public Class getDAOClass(){
  	return _daoCls;
  }
  
  public Serializable getId(){
  	return _id;
  }
}
