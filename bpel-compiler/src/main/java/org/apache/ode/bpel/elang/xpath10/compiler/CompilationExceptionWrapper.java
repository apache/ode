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
package org.apache.ode.bpel.elang.xpath10.compiler;

import org.apache.ode.bpel.compiler.api.CompilationException;

import org.jaxen.JaxenException;

/**
 * Jaxen-compliant wrapper for
 * {@link org.apache.ode.bpel.compiler.api.CompilationException}.
 */
class CompilationExceptionWrapper extends JaxenException {

	private static final long serialVersionUID = -6918197147269316065L;

	public CompilationExceptionWrapper(CompilationException cause) {
		super(cause);
		assert getCompilationException() != null;
	}

	public CompilationException getCompilationException() {
		return (CompilationException) getCause();
	}
}
