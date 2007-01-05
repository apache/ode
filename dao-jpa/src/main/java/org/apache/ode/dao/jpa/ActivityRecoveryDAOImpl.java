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

package org.apache.ode.dao.jpa;


import org.apache.ode.bpel.dao.ActivityRecoveryDAO;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name="ODE_ACTIVITY_RECOVERY")
public class ActivityRecoveryDAOImpl implements ActivityRecoveryDAO {

	@Id @Column(name="ACTIVITY_ID") private long	_activityId;
	@Basic @Column(name="CHANNEL") private String   _channel;
	@Basic @Column(name="REASON") private String    _reason;
	@Basic @Column(name="DATE_TIME") private Date   _dateTime;
	@Lob @Column(name="DETAILS") private String  	_details;
	@Basic @Column(name="ACTIONS") private String   _actions;
	@Basic @Column(name="RETRIES") private int      _retries;
	@Version @Column(name="VERSION") private long   _version;
	
	public ActivityRecoveryDAOImpl() {}
	public ActivityRecoveryDAOImpl(String channel, long activityId,
			String reason, Date dateTime, Element data, String[] actions,
			int retries) {
		_channel = channel;
		_activityId = activityId;
		_reason = reason;
		_dateTime = dateTime;

        if (data != null) _details = DOMUtils.domToString(data);
		
        String alist = actions[0];
        for (int i = 1; i < actions.length; ++i)
            alist += " " + actions[i];
		_actions = alist;
		
		_retries = retries;
		
	}
	
	public String getActions() {
		return _actions;
	}

	public String[] getActionsList() {
		return getActions().split(" ");
	}

	public long getActivityId() {
		return _activityId;
	}

	public String getChannel() {
		return _channel;
	}

	public Date getDateTime() {
		return _dateTime;
	}

	public Element getDetails() {
		Element ret = null;
		if ( _details != null ) {
			try {
				ret = DOMUtils.stringToDOM(_details);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return ret;
	}

	public String getReason() {
		return _reason;
	}

	public int getRetries() {
		return _retries;
	}

}
