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
package org.apache.ode.bpel.parser;

import org.apache.ode.sax.fsa.ParseContext;

import org.xml.sax.ErrorHandler;

/**
 * <p>
 * Simple interface for handling either SAX-related (i.e., validation and
 * well-formedness) errors and BPEL-specific errors at parse time.  The most
 * common use case for the interface will be a collector that holds errors until
 * later.
 * </p>
 */
public interface BpelParseErrorHandler extends ErrorHandler, ParseContext {
    
}
