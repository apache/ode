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

package org.apache.ode.utils.jmx;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;


/**
 * Constant related to naming of ODE objects in JMX.
 */
public final class JMXConstants {

  /** JMX domain key */
  public static final String JMX_DOMAIN = "com.fivesight.ode";

  // type properties

  /** Value of <code>type</code> name attribute for {@link org.apache.ode.sfwk.mngmt.DomainNodeMBean}s. */
  public static final String TYPE_DOMAINMBEAN = "DomainAdmin";

  /** Value of <code>type</code> name attribute for {@link org.apache.ode.sfwk.mngmt.SystemRuntimeMBean}s. */
  public static final String TYPE_SYSTEMMBEAN = "SystemAdmin";

  /** Value of <code>type</code> name attribute for {@link org.apache.ode.sfwk.mngmt.ServiceRuntimeMBean}s. */
  public static final String TYPE_SERVICEMBEAN = "service";

  /**
   * Create a JMX name for a ODE domain.
   * @param domainId domain identifier
   * @return JMX {@link ObjectName}
   */
  public static ObjectName createDomainObjectName(String domainId) throws MalformedObjectNameException {
  	Hashtable<String, String> tbl = new Hashtable<String, String>();
    tbl.put("domain", domainId);
    tbl.put("node", "node0");
    tbl.put("type", TYPE_DOMAINMBEAN);

    return new ObjectName(JMX_DOMAIN, tbl);
  }

  /**
   * Create a JMX name query to find ODE domain MBeans.
   * @return {@link ObjectName} pattern
   * @throws MalformedObjectNameException
   */ 
  public static ObjectName createDomainObjectQuery()  throws MalformedObjectNameException {
    return new ObjectName(JMX_DOMAIN + ":type=" + TYPE_DOMAINMBEAN + ",*");
  }
}
