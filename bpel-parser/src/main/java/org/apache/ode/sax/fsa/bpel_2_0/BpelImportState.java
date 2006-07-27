/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
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
