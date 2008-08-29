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
package org.apache.ode.bpel.rtrep.v2;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.rtrep.common.ConfigurationException;
import org.apache.ode.utils.xsd.Duration;
import org.w3c.dom.Node;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A registry of {@link ExpressionLanguageRuntime} objects that is able to map a given expression to the appropriate 
 * language runtime. We also do some exception guarding here so that the core of the engine does not have to deal
 * with random exceptions from not-quite perfect expression runtime imlementation.
 */
public class ExpressionLanguageRuntimeRegistry {
    private final Map<OExpressionLanguage, ExpressionLanguageRuntime> _runtimes = new HashMap<OExpressionLanguage, ExpressionLanguageRuntime>();

    public ExpressionLanguageRuntimeRegistry() {
    }

    public void registerRuntime(OExpressionLanguage oelang) throws ConfigurationException {
        String className = oelang.properties.get("runtime-class");
        try {
            Class cls = Class.forName(className);
            ExpressionLanguageRuntime elangRT = (ExpressionLanguageRuntime) cls.newInstance();
            elangRT.initialize(oelang.properties);
            _runtimes.put(oelang, elangRT);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException("Illegal Access Error", e);
        } catch (InstantiationException e) {
            throw new ConfigurationException("Instantiation Error", e);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Class Not Found Error: " + className, e);
        }

    }

    public String evaluateAsString(OExpression cexp, EvaluationContext ctx) throws FaultException {
        try {
            return findRuntime(cexp).evaluateAsString(cexp, ctx);
        } catch (FaultException fe) {
            throw fe;
        } catch (Throwable t) {
            throw new FaultException(cexp.getOwner().constants.qnSubLanguageExecutionFault, t.toString(), t);
        }
    }

    public boolean evaluateAsBoolean(OExpression cexp, EvaluationContext ctx) throws FaultException {
        try {
            return findRuntime(cexp).evaluateAsBoolean(cexp, ctx);
        } catch (FaultException fe) {
            throw fe;
        } catch (Throwable t) {
            throw new FaultException(cexp.getOwner().constants.qnSubLanguageExecutionFault, t.toString(), t);
        }
    }

    public Number evaluateAsNumber(OExpression cexp, EvaluationContext ctx) throws FaultException {
        try {
            return findRuntime(cexp).evaluateAsNumber(cexp, ctx);
        } catch (FaultException fe) {
            throw fe;
        } catch (Throwable t) {
            throw new FaultException(cexp.getOwner().constants.qnSubLanguageExecutionFault, t.toString(), t);
        }
    }

    public List evaluate(OExpression cexp, EvaluationContext ctx) throws FaultException {
        try {
            return findRuntime(cexp).evaluate(cexp, ctx);
        } catch (FaultException fe) {
            throw fe;
        } catch (Throwable t) {
            throw new FaultException(cexp.getOwner().constants.qnSubLanguageExecutionFault, t.toString(), t);
        }
    }

    public Node evaluateNode(OExpression cexp, EvaluationContext ctx) throws FaultException {
        try {
            return findRuntime(cexp).evaluateNode(cexp, ctx);
        } catch (FaultException fe) {
            throw fe;
        } catch (Throwable t) {
            throw new FaultException(cexp.getOwner().constants.qnSubLanguageExecutionFault, t.toString(), t);
        }
    }

    public Calendar evaluateAsDate(OExpression cexp, EvaluationContext ctx) throws FaultException {
        try {
            return findRuntime(cexp).evaluateAsDate(cexp, ctx);
        } catch (FaultException fe) {
            throw fe;
        } catch (Throwable t) {
            throw new FaultException(cexp.getOwner().constants.qnSubLanguageExecutionFault, t.toString(), t);
        }
    }

    public Duration evaluateAsDuration(OExpression cexp, EvaluationContext ctx) throws FaultException {
        try {
            return findRuntime(cexp).evaluateAsDuration(cexp, ctx);
        } catch (FaultException fe) {
            throw fe;
        } catch (Throwable t) {
            throw new FaultException(cexp.getOwner().constants.qnSubLanguageExecutionFault, t.toString(), t);
        }
    }

    private ExpressionLanguageRuntime findRuntime(OExpression cexp) {
        return _runtimes.get(cexp.expressionLanguage);
    }

}
