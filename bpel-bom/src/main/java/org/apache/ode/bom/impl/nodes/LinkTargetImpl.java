/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.LinkTarget;

/**
 * Implementation of the {@link LinkTarget} interface.
 */
public class LinkTargetImpl extends BpelObjectImpl implements LinkTarget {
  private static final long serialVersionUID = 1L;

  private ActivityImpl _activity;
  private String _linkName;

  public Activity getActivity() {
    return _activity;
  }

  public String getLinkName() {
    return _linkName;
  }

  public void setLinkName(String linkName) {
    _linkName = linkName;
  }

  void setActivity(ActivityImpl activityImpl) {
    _activity = activityImpl;
  }

}
