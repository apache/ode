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

import org.apache.ode.bom.api.BpelObject;
import org.apache.ode.sax.evt.SaxEvent;

public interface ParseContext {
  
  public void setBaseUri(String uri);
  public String getBaseUri();
  public void parseError(ParseError pe) throws ParseException;
  public void parseError(short severity, SaxEvent se, String key, String msg)
    throws ParseException;
  public void parseError(short severity, BpelObject bo, String key, String msg)
    throws ParseException;
  public void parseError(short severity, String key, String msg)
    throws ParseException;
  
}
