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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.bom.Activity;
import org.apache.ode.bpel.compiler.bom.ForEachActivity;
import org.apache.ode.bpel.compiler.bom.Scope;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OForEach;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OXsdTypeVarType;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.Namespaces;

/**
 * Generates code for <code>&lt;forEach&gt;</code> activities.
 */
public class ForEachGenerator extends DefaultActivityGenerator {

    private static final Log __log = LogFactory.getLog(AssignGenerator.class);
    private static final ForEachGeneratorMessages __cmsgs = MessageBundle.getMessages(ForEachGeneratorMessages.class);

    public OActivity newInstance(Activity src) {
        return new OForEach(_context.getOProcess(), _context.getCurrent());
    }

    public void compile(OActivity output, Activity src) {
        if (__log.isDebugEnabled()) __log.debug("Compiling ForEach activity.");
        OForEach oforEach = (OForEach) output;
        ForEachActivity forEach = (ForEachActivity) src;
        oforEach.parallel = forEach.isParallel();
        oforEach.startCounterValue = _context.compileExpr(forEach.getStartCounter());
        oforEach.finalCounterValue = _context.compileExpr(forEach.getFinalCounter());
        if (forEach.getCompletionCondition() != null) {
            oforEach.completionCondition =
                    new OForEach.CompletionCondition(_context.getOProcess());
            oforEach.completionCondition.successfulBranchesOnly
                    = forEach.getCompletionCondition().getBranch().isSuccessfulBranchesOnly();
            oforEach.completionCondition.branchCount = _context.compileExpr(forEach.getCompletionCondition().getBranch());
        }

        // ForEach 'adds' a counter variable in inner scope
        if (__log.isDebugEnabled()) __log.debug("Adding the forEach counter variable to inner scope.");
        if (forEach.getChild() == null) {
            throw new CompilationException(__cmsgs.errMissingScopeinForeach().setSource(forEach));
        }

        Scope s = forEach.getChild().getScope();
        // Checking if a variable using the same name as our counter is already defined.
        // The spec requires a static analysis error to be thrown in that case.
        if (s.getVariableDecl(forEach.getCounterName()) != null)
            throw new CompilationException(__cmsgs.errForEachAndScopeVariableRedundant(forEach.getCounterName()).setSource(src));

        OXsdTypeVarType counterType = new OXsdTypeVarType(oforEach.getOwner());
        counterType.simple = true;
        counterType.xsdType = new QName(Namespaces.XML_SCHEMA, "int");
        OScope.Variable counterVar = new OScope.Variable(oforEach.getOwner(), counterType);
        counterVar.name = forEach.getCounterName();

        if (__log.isDebugEnabled()) __log.debug("Compiling forEach inner scope.");
        oforEach.innerScope = _context.compileSLC(forEach.getChild(), new OScope.Variable[]{counterVar});

        // oforEach.innerScope.addLocalVariable(counterVar);
        oforEach.counterVariable = counterVar;
    }

}
