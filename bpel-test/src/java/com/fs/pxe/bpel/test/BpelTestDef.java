/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * BPEL test-case definition.
 */
public class BpelTestDef {
  /** .bpel file. */
  public File bpelFile;

  /** .wsdl file. */
  public File wsdlFile;

  /** The BPEL invocations. */
  public List<MessageDef> receives = new ArrayList<MessageDef>();

  /** The partner responses. */
  public List<MessageDef> invokes = new ArrayList<MessageDef>();

  /** Namespace declarations (for the postChecks). */
  public Map<String, String> namespaces = new HashMap<String, String>();

  /** Post-checks (assertions). */
  public List<String> postChecks = new ArrayList<String>();

  public static class MessageDef {
    public String partnerLink;
    public String operation;
    public File inFile;
    public String fault;
    public Integer delay;
  }
}
