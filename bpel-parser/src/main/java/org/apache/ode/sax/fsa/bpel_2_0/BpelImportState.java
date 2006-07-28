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
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.bom.api.Import;
import org.apache.ode.bom.impl.nodes.ImportImpl;
import org.apache.ode.sax.fsa.*;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;

import java.net.URI;
import java.net.URISyntaxException;

class BpelImportState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private Import _import;
  
  private BpelImportState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    _import = new ImportImpl();
    XmlAttributes attr = se.getAttributes();
    _import.setImportType(attr.getValue("importType"));
    try {
			_import.setLocation(new URI(attr.getValue("location")));
		} catch (URISyntaxException e) {
      // TODO: Error or warning?
      // TODO: Add meaningful key.
      getParseContext().parseError(ParseError.ERROR,se,"","Unable to parse URI '" + attr.getValue("location") + "'");
		}
    _import.setNamespace(attr.getValue("namespace"));
    _import.setLineNo(se.getLocation().getLineNumber());
    _import.setNamespaceContext(se.getNamespaceContext());
  }
  
  Import getImport(){
    return _import;
  }
  
  /**
   * @see org.apache.ode.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return _factory;
  }

  /**
   * @see org.apache.ode.sax.fsa.State#getType()
   */
  public int getType() {
    return BPEL_IMPORT;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new BpelImportState(se,pc);
    }
  }
}
