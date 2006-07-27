/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.api;


/**
 * BOM representation of a BPEL <code>&lt;invoke&gt;</code> activity.
 */
public interface InvokeActivity extends Activity, Communication, ScopeLikeConstruct {
  /**
   * Set the input variable.
   *
   * @param variable name of input variable
   */
  void setInputVar(String variable);

  /**
   * Get the input variable.
   *
   * @return name of input variable
   */
  String getInputVar();

  /**
   * Set the output variable.
   *
   * @param variable output variable name
   */
  void setOutputVar(String variable);

  /**
   * The output variable.
   *
   * @return output variable name
   */
  String getOutputVar();


}
