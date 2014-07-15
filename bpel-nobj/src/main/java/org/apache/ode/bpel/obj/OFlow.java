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
package org.apache.ode.bpel.obj;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;

import java.util.HashSet;
import java.util.Set;

/**
 */
public class OFlow extends OActivity {

	/** Links delcared within this activity. */
	private static final String LOCALLINKS = "localLinks";
	private static final String PARALLELACTIVITIES = "parallelActivities";
	
	@JsonCreator
	public OFlow(){}
	
	public OFlow(OProcess owner, OActivity parent) {
		super(owner, parent);
		setLocalLinks(new HashSet<OLink>());
		setParallelActivities(new HashSet<OActivity>());
	}

	@JsonIgnore
	public OLink getLocalLink(final String linkName) {
		return CollectionsX.find_if(getLocalLinks(),
				new MemberOfFunction<OLink>() {
					public boolean isMember(OLink o) {
						return o.getName().equals(linkName);
					}
				});
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public Set<OLink> getLocalLinks() {
		Object o = fieldContainer.get(LOCALLINKS);
		return o == null ? null : (Set<OLink>)o;
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public Set<OActivity> getParallelActivities() {
		Object o = fieldContainer.get(PARALLELACTIVITIES);
		return o == null ? null : (Set<OActivity>)o;
	}

	public void setLocalLinks(Set<OLink> localLinks) {
		if (getLocalLinks() == null){
			fieldContainer.put(LOCALLINKS, localLinks);
		}
	}

	public void setParallelActivities(Set<OActivity> parallelActivities) {
		if (getParallelActivities() == null){
			fieldContainer.put(PARALLELACTIVITIES, parallelActivities);
		}
	}

}
