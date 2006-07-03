/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils.xsd;

import common.TestResources;

import java.net.URI;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Test schema capture functionality.
 */
public class SchemaCaptureTest extends TestCase {

  public void testSchemaCapture() throws Exception {
    String initialURI = TestResources.getRetailerSchema().toExternalForm();
    Map<URI, byte[]> s = XSUtils.captureSchema(initialURI, new DefaultXMLEntityResolver());
    // we expect the root schema and three includes
    assertEquals(4, s.size());
  }

}
