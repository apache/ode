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

package org.apache.ode.daohib.bpel.hobj;

import org.apache.ode.daohib.hobj.HObject;

/**
 * The property content is stored into a classic varchar if it's a simple type
 * and into a CLOB if it's mixed (so one of the two will always be null).
 * @hibernate.class table="BPEL_PROCESS_PROPERTY"
 */
public class HProcessProperty extends HObject {

  private String _name;
  private String _namespace;
  private String _simpleContent;
  private String _mixedContent;
  private HProcess _process;

  public HProcessProperty() {
    super();
  }

  /**
  * @hibernate.property
  *  column="PROPNAME"
  */
  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  /**
  * @hibernate.property
  *  column="PROPNS"
  */
  public String getNamespace() {
    return _namespace;
  }

  public void setNamespace(String namespace) {
    _namespace = namespace;
  }

  /**
  * @hibernate.property
  *  column="SIMPLE_CNT"
  */
  public String getSimpleContent() {
    return _simpleContent;
  }

  public void setSimpleContent(String simpleContent) {
    _simpleContent = simpleContent;
  }

  /**
  * @hibernate.property
  *  column="MIXED_CNT"
  *  type="text"
  *  length="1000000000"
  */
  public String getMixedContent() {
    return _mixedContent;
  }

  public void setMixedContent(String mixedContent) {
    _mixedContent = mixedContent;
  }

  /**
   * @hibernate.many-to-one
   *  column="PROCESS_ID"
   */
	public HProcess getProcess() {
		return _process;
	}

	public void setProcess(HProcess process) {
		_process = process;
	}

}
