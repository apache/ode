/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sax.fsa.bpel_2_0;

import com.fs.pxe.bom.api.Import;
import com.fs.pxe.bom.impl.nodes.ImportImpl;
import com.fs.pxe.sax.fsa.*;
import com.fs.sax.evt.StartElement;
import com.fs.sax.evt.XmlAttributes;

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
   * @see com.fs.pxe.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return _factory;
  }

  /**
   * @see com.fs.pxe.sax.fsa.State#getType()
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
