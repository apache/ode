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
import org.apache.ode.bpel.compiler.bom.CompositeActivity;
import org.apache.ode.bpel.compiler.bom.SequenceActivity;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OSequence;
import org.apache.ode.utils.msg.MessageBundle;


/**
 * Generates code for <code>&lt;sequence&gt;</code> activities.
 */

class SequenceGenerator extends DefaultActivityGenerator {

    private static final CommonCompilationMessages __cmsgs =
        MessageBundle.getMessages(CommonCompilationMessages.class);

    public OActivity newInstance(Activity src) {
        return new OSequence(_context.getOProcess(), _context.getCurrent());
    }

    public void compile(OActivity output, Activity src)  {
        OSequence oseq = (OSequence) output;
        compileChildren(oseq, (SequenceActivity) src);
    }

    protected void compileChildren(OSequence dest, CompositeActivity src) {
        if (src.getActivities().size() == 0)
            throw new CompilationException(__cmsgs.errEmptySequence().setSource(src));

        for (Activity child : src.getActivities()) {
            try {
                OActivity compiledChild = _context.compile(child);
                dest.sequence.add(compiledChild);
            }
            catch (CompilationException ce) {
                _context.recoveredFromError(child, ce);
            }
        }
    }

}
