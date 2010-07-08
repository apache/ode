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
package org.apache.ode.bpel.compiler;

import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.bom.Activity;
import org.apache.ode.bpel.compiler.bom.ThrowActivity;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OThrow;
import org.apache.ode.utils.msg.MessageBundle;


/**
 * Generates code for <code>&lt;throw&gt;</code> activities.
 */
class ThrowGenerator extends DefaultActivityGenerator {
    private static final ThrowGeneratorMessages __cmsgs = MessageBundle.getMessages(ThrowGeneratorMessages.class);
    
    public OActivity newInstance(Activity src) {
        return new OThrow(_context.getOProcess(), _context.getCurrent());
    }

    public void compile(OActivity output, Activity src) {
        ThrowActivity throwDef = (ThrowActivity)src;
        OThrow othrow = (OThrow) output;

        if (throwDef.getFaultName() == null)
            throw new CompilationException(__cmsgs.errThrowMustDefineFaultName()); 

        othrow.faultName = throwDef.getFaultName();
        if(throwDef.getFaultVariable() != null)
            othrow.faultVariable = _context.resolveVariable(throwDef.getFaultVariable());
    }
}
