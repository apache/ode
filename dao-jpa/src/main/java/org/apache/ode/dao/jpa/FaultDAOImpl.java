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

import org.apache.ode.bpel.dao.FaultDAO;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.namespace.QName;


@Entity
@Table(name="ODE_FAULT")
@NamedQueries({
	@NamedQuery(name=FaultDAOImpl.DELETE_FAULTS_BY_IDS, query="delete from FaultDAOImpl as f where f._id in(:ids)")
})
public class FaultDAOImpl implements FaultDAO {
	public final static String DELETE_FAULTS_BY_IDS = "DELETE_FAULTS_BY_IDS";
	
	@Id @Column(name="FAULT_ID") 
	@GeneratedValue(strategy=GenerationType.AUTO)
	@SuppressWarnings("unused")
	private Long _id;
	@Basic @Column(name="NAME")
    private String _name;
	@Basic @Column(name="MESSAGE", length=4000)
    private String _explanation;
	@Lob @Column(name="DATA")
    private String _data;
	@Basic @Column(name="LINE_NUMBER")
    private int _lineNo;
	@Basic @Column(name="ACTIVITY_ID")
    private int _activityId;

	public FaultDAOImpl() {}
	public FaultDAOImpl(QName faultName, String explanation, int faultLineNo,
			int activityId, Element faultMessage) {
		_name = faultName.toString();
		_explanation = explanation;
		_lineNo = faultLineNo;
		_activityId = activityId;
		_data = (faultMessage == null)?null:DOMUtils.domToString(faultMessage);
	}
	
	public int getActivityId() {
		return _activityId;
	}

	public Element getData() {
		Element ret = null;
		
		try {
			ret = (_data == null)?null:DOMUtils.stringToDOM(_data);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return ret;
	}

	public String getExplanation() {
		return _explanation;
	}

	public int getLineNo() {
		return _lineNo;
	}

	public QName getName() {
		return _name == null ? null : QName.valueOf(_name);
	}

}
