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
package org.apache.ode.bpel.engine;

import com.fs.jacob.soup.ReplacementMap;
import org.apache.ode.bpel.o.OBase;
import org.apache.ode.bpel.o.OProcess;

/**
 * A JACOB {@link ReplacementMap} implementation that eliminates unnecessary serialization
 * of the (constant) compiled process model.
 */
class ReplacementMapImpl implements ReplacementMap {
  private OProcess _oprocess;

  ReplacementMapImpl(OProcess oprocess) {
    _oprocess = oprocess;
  }

  public boolean isReplacement(Object obj) {
    return obj instanceof BpelProcess.OBaseReplacementImpl;
  }

  public Object getOriginal(Object replacement) throws IllegalArgumentException {
    if (!(replacement instanceof BpelProcess.OBaseReplacementImpl))
      throw new IllegalArgumentException("Not OBaseReplacementObject!");
    return _oprocess.getChild(((BpelProcess.OBaseReplacementImpl)replacement)._id);
  }

  public Object getReplacement(Object original) throws IllegalArgumentException {
    if (!(original instanceof OBase))
      throw new IllegalArgumentException("Not OBase!");
    return new BpelProcess.OBaseReplacementImpl(((OBase)original).getId());
  }

  public boolean isReplaceable(Object obj) {
    return obj instanceof OBase;
  }

}
