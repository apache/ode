/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.elang.xpath10.compiler;

/**
 * XPath-4-BPEL related constants.
 */
class Constants {
  /**
   * Extension function bpws:getVariableData('variableName', 'partName'?,
   * 'locationPath'?)
   */
  public static final String EXT_FUNCTION_GETVARIABLEDATA = "getVariableData";

  /**
   * Extension function
   * bpws:getVariableProperty('variableName','propertyName')
   */
  public static final String EXT_FUNCTION_GETVARIABLEPROPRTY = "getVariableProperty";

  /**
   * Extension function bpws:getLinkStatus('getLinkName')
   */
  public static final String EXT_FUNCTION_GETLINKSTATUS = "getLinkStatus";

  /**
   * Extension function bpws:doXslTransform('xslSheetUri', node-set, (xslParamKey, xslParamValue)*)
   */
  public static final String EXT_FUNCTION_DOXSLTRANSFORM = "doXslTransform";

  public static final String BPEL20_NS = "http://schemas.xmlsoap.org/ws/2004/03/business-process/";
  public static final String BPEL11_NS = "http://schemas.xmlsoap.org/ws/2003/03/business-process/";

  public static final String NS_ODE_MESSAGE = "http://www.fivesight.com/ode/message";

}
