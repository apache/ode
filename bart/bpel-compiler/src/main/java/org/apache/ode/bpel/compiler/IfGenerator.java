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
import org.apache.ode.bpel.compiler.bom.IfActivity;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OSwitch;
import org.apache.ode.utils.msg.MessageBundle;


/**
 * Generates code for the <code>&lt;switch&gt;</code> activities.
 */
class IfGenerator extends DefaultActivityGenerator {
    private static final IfGeneratorMessages __cmsgs = MessageBundle.getMessages(IfGeneratorMessages.class);

    public OActivity newInstance(Activity src) {
        return new OSwitch(_context.getOProcess(), _context.getCurrent());
    }

    public void compile(OActivity output, Activity src) {
        OSwitch oswitch = (OSwitch) output;
        IfActivity switchDef = (IfActivity)src;

        if (switchDef.getCondition() == null)
            throw new CompilationException(__cmsgs.errIfWithNoCondition());

        boolean first = true;
        if (switchDef.getActivity() != null) {
            OSwitch.OCase ocase = new OSwitch.OCase(_context.getOProcess());
            ocase.activity = _context.compile(switchDef.getActivity());
            ocase.expression = _context.compileExpr(switchDef.getCondition());
            oswitch.addCase(ocase);
            first = false;
        }

        for (IfActivity.Case ccase : switchDef.getCases()) {
            OSwitch.OCase ocase = new OSwitch.OCase(_context.getOProcess());
            ocase.activity = _context.compile(ccase.getActivity());
            ocase.expression = first ? _context.compileExpr(switchDef.getCondition())
                    : (ccase.getCondition() == null ? _context.constantExpr(true) : _context.compileExpr(ccase.getCondition()));
            oswitch.addCase(ocase);
            first = false;
        }
    }
}
