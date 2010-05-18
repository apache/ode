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

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.api.CompilerContext;
import org.apache.ode.bpel.compiler.api.ExpressionValidator;
import org.apache.ode.bpel.compiler.bom.Process;
import org.apache.ode.bpel.compiler.bom.Expression;
import org.apache.ode.bpel.o.OVarType;

public class ExpressionValidatorFactory {
    private static final Log __log = LogFactory.getLog(ExpressionValidatorFactory.class);
	private ExpressionValidator _validator = new EmptyValidator();

	public ExpressionValidatorFactory(Properties config) {
		String propertyName = "org.apache.ode.validator";
		String validatorClass = (String) config.getProperty(propertyName, "");
		__log.debug("Trying property " + propertyName + " got value: " + validatorClass);
		if (!validatorClass.equals("")) {
			try {
				_validator = (ExpressionValidator) getClass().forName(validatorClass).newInstance();
			} catch (Exception e) {
				__log.warn("Cannot instantiate expression validator of class " + validatorClass);
			}
		}
	}
	
	public ExpressionValidator getValidator() {
		return _validator;
	}
	
	private static class EmptyValidator implements ExpressionValidator {
        public void bpelImportsLoaded(Process source, CompilerContext compilerContext) throws CompilationException {}
        public void bpelCompilationCompleted(Process source) throws CompilationException {}
        public Object validate(Expression source, OVarType rootNodeType, Object requestedResultType) throws CompilationException { return null; }
	}
}
