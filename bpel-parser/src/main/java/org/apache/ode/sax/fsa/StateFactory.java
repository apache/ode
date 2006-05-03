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
package org.apache.ode.sax.fsa;

import org.apache.ode.sax.evt.StartElement;


/**
 * Responsible for creating instances of a <code>State</code> with specific context.
 */
public interface StateFactory {
  
  /**
   * Create an instance of a {@link State} with the specified context.
   * @param se the <code>startElement</code> SAX Event that caused the creation.
   * @param pc the {@link ParseContext} for the current parse.
   * @return the configured instance.
   * @throws ParseException if the configuration is invalid.
   */
  public State newInstance(StartElement se, ParseContext pc) throws ParseException;
}
