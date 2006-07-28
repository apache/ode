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
import java.util.HashSet;
import java.util.Set;

/**
 * @hibernate.class table="BPEL_CORRELATOR_MESSAGE"
 */
public class HCorrelatorMessage extends HObject {
  
	private Set<HCorrelatorMessageKey> _correlationKeys = new HashSet<HCorrelatorMessageKey>();
  private HCorrelator _correlator;
  private HMessageExchange _messageExchange;

	public HCorrelatorMessage() {
		super();
	}
  
	/**
   * @hibernate.set
   *  lazy="true"
   *  inverse="true"
   *  cascade="delete"
   * @hibernate.collection-key
   *  column="CORRELATOR_MESSAGE_ID"
   * @hibernate.collection-one-to-many
   *  class="org.apache.ode.daohib.bpel.hobj.HCorrelatorMessageKey"
   */
  public Set<HCorrelatorMessageKey> getCorrelationHashKeys() {
    return _correlationKeys;
  }

  public void setCorrelationHashKeys(Set<HCorrelatorMessageKey> correlationHashKeys) {
    _correlationKeys = correlationHashKeys;
  }
  
  /**
   * @hibernate.many-to-one
   * @hibernate.column name="CORRELATOR" index="IDX_CORRELATORMESSAGE_CID"
   */
  public HCorrelator getCorrelator() {
    return _correlator;
  }

  public void setCorrelator(HCorrelator correlator) {
    _correlator = correlator;
  }

  /**
   * @hibernate.many-to-one column="MEX" 
   */
  public HMessageExchange getMessageExchange() {
    return _messageExchange;
  }

  public void setMessageExchange(HMessageExchange data) {
    _messageExchange = data;
  }

}
