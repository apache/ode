/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.wsdl;

import javax.wsdl.Definition;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;

/**
 * Marker interface for WSDL factories that understand BPEL WSDL extensions.
 */
public interface WSDLFactory4BPEL {

  Definition newDefinition();

  WSDLReader newWSDLReader();

  WSDLWriter newWSDLWriter();

}
