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

/**
 * Factory for creating {@link BpelProcessBuilder} objects.
 */
public abstract class BpelProcessBuilderFactory {
  
  /**
   * Get a {@link BpelProcessBuilderFactory} object.
   * @return a usable {@link BpelProcessBuilderFactory} object
   * @throws BpelProcessBuilderFactoryException
   */
  public static BpelProcessBuilderFactory newProcessBuilderFactory() throws BpelProcessBuilderFactoryException {
    return new org.apache.ode.bpel.parser.BpelProcessBuilderFactoryImpl();
  }

  /**
   * Enable or disable strict parsing mode. In strict parsing mode the BPEL schema is
   * enforced, any parse problems will resultin a {@link BpelParseException}. In
   * non-strict mode, schema conformance is relaxed, and a best-effort will be made
   * to load the BPEL process description.
   *
   * @param strict if <code>true</code> strict parsing will be used
   */
  abstract public void setStrict(boolean strict);

  /**
   * Get the strict parsing flag.
   *
   * @return value of strict parse flag
   * @see #setStrict(boolean)
   */
  abstract public boolean getStrict();

  /**
   * Create a new {@link BpelProcessBuilder}.
   *
   * @return
   */
  abstract public BpelProcessBuilder newBpelProcessBuilder();
}
