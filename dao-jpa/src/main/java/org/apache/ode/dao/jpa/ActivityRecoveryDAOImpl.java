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

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.Date;


@Entity
@Table(name="ODE_ACTIVITY_RECOVERY")
@NamedQueries({
 	@NamedQuery(name=ActivityRecoveryDAOImpl.DELETE_ACTIVITY_RECOVERIES_BY_IDS, query="delete from ActivityRecoveryDAOImpl as a where a._instanceId in(:ids)"),
	@NamedQuery(name=ActivityRecoveryDAOImpl.COUNT_ACTIVITY_RECOVERIES_BY_INSTANCES,
			query="select r._instanceId, count(r._id) from ActivityRecoveryDAOImpl r where r._instance in(:instances) group by r._instanceId")
})
public class ActivityRecoveryDAOImpl implements ActivityRecoveryDAO {
 	public final static String DELETE_ACTIVITY_RECOVERIES_BY_IDS = "DELETE_ACTIVITY_RECOVERIES_BY_IDS";
	public final static String COUNT_ACTIVITY_RECOVERIES_BY_INSTANCES = "COUNT_ACTIVITY_RECOVERIES_BY_INSTANCES";
	
    @Id @Column(name="ID")
    @GeneratedValue(strategy= GenerationType.AUTO)
    @SuppressWarnings("unused")
    private Long _id;

	@Basic @Column(name="ACTIVITY_ID")
    private long _activityId;
	@Basic @Column(name="CHANNEL")
    private String _channel;
	@Basic @Column(name="REASON")
    private String _reason;
	@Basic @Column(name="DATE_TIME")
    private Date _dateTime;
	@Lob @Column(name="DETAILS")
    private String _details;
	@Basic @Column(name="ACTIONS")
    private String _actions;
	@Basic @Column(name="RETRIES")
    private int _retries;

 	@SuppressWarnings("unused")
 	@Basic @Column(name="INSTANCE_ID", insertable=false, updatable=false, nullable=true)
     private Long _instanceId;

	// _instances is unused because this is a one-way relationship at the database level
    @SuppressWarnings("unused")
    @ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="INSTANCE_ID")
    private ProcessInstanceDAOImpl _instance;

	
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

    public ProcessInstanceDAOImpl getInstance() {
        return _instance;
    }

    public void setInstance(ProcessInstanceDAOImpl instance) {
        _instance = instance;
    }
}
