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

import org.apache.ode.bom.api.Correlation;

/**
 * Model of a BPEL correlation (on an invoke/receive/reply).
 */
public class CorrelationImpl extends BpelObjectImpl implements Correlation {

  private static final long serialVersionUID = -1L;

  private String _correlationSetName;
  private short _initiate;
  private short _pattern;
  
  public CorrelationImpl() {
  }

  public short getInitiate() {
    return _initiate;
  }

  public void setInitiate(short initiate) {
    _initiate = initiate;
  }

  public short getPattern() {
    return _pattern;
  }

  public void setPattern(short pattern) {
    _pattern = pattern;
  }

  public String getCorrelationSet() {
    return _correlationSetName;
  }

  public void setCorrelationSet(String correlationSetName) {
    _correlationSetName = correlationSetName;
  }

}
