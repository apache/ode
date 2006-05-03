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

import org.apache.ode.bom.api.Copy;
import org.apache.ode.bom.api.From;
import org.apache.ode.bom.api.To;
import org.apache.ode.utils.NSContext;

/**
 * Assignmenet copy entry, i.e. what the assignment consits of.
 */
public class CopyImpl extends BpelObjectImpl implements Copy {
  private static final long serialVersionUID = -1L;
  private To _to;
  private From _from;
  private boolean keepSrcElement = false;

  public CopyImpl(NSContext ns) {
    super(ns);
  }

  public To getTo() {
    return _to;
  }

  public void setTo(To to) {
    _to = to;
  }

  public From getFrom() {
    return _from;
  }

  public void setFrom(From from) {
    _from = from;
  }

  public boolean isKeepSrcElement() {
    return keepSrcElement;
  }

  public void setKeepSrcElement(boolean keepSrcElement) {
    this.keepSrcElement = keepSrcElement;
  }
}
