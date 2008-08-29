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
package org.apache.ode.bpel.compiler.bom;

import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;
import org.apache.ode.bpel.extension.ExtensibleElement;

/**
 * BOM representation of the BPEL <code>&lt;extensionActivity&gt;</code> activity. The
 * <code>&lt;extensionActivity&gt;</code> activity contains a nested DOM element that 
 * represents the actual extension element. According to the BPEL 2.0 PR1 specification, 
 * the standards elements and standards attributes are not located in the extensionActivity
 * element but in the nested element. Therefore the convenient access methods for standards
 * attributes/elements are overridden to refer to the nested elements.
 *
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class ExtensionActivity extends CompositeActivity implements ExtensibleElement {
	private Activity _childActivity;

	public ExtensionActivity(Element el) {
        super(el);
        _childActivity = null;
        Element child = getFirstExtensibilityElement();
        if (child != null) {
        	_childActivity = new Activity(getFirstExtensibilityElement());
        }
    }

    @Override
	public Expression getJoinCondition() {
		if (_childActivity == null) {
			return null;
		}
    	return _childActivity.getJoinCondition();
	}

	@Override
	public List<LinkSource> getLinkSources() {
		if (_childActivity == null) {
			return Collections.emptyList();
		}
		return _childActivity.getLinkSources();
	}

	@Override
	public List<LinkTarget> getLinkTargets() {
		if (_childActivity == null) {
			return Collections.emptyList();
		}
		return _childActivity.getLinkTargets();
	}

	@Override
	public String getName() {
		if (_childActivity == null) {
			return null;
		}
		return _childActivity.getName();
	}

	@Override
	public SuppressJoinFailure getSuppressJoinFailure() {
		if (_childActivity == null) {
			return SuppressJoinFailure.NOTSET;
		}
		return _childActivity.getSuppressJoinFailure();
	}
	
	@Override
	public List<Activity> getActivities() {
		if (_childActivity == null) {
			return Collections.emptyList();
		}

		return _childActivity.getChildren(Activity.class);
	}

	public Element getNestedElement() {
		return getFirstExtensibilityElement();
	}

}
