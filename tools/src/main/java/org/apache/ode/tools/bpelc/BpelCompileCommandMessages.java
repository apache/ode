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

package org.apache.ode.tools.bpelc;

import org.apache.ode.utils.msg.MessageBundle;

import java.io.File;
import java.net.URL;

/**
 * Internationalization message bundle for the BPEL compiler.
 */
public class BpelCompileCommandMessages extends MessageBundle {

  /** Unable to write compiled process to {0}: {1} */
  public String msgIoErrWritingCbp(String file, String reason) {
    return this.format("Unable to write compiled process to {0}: {1}", file, reason);
  }

  /** The repository does not contain a resource at the URI {0}. */
  public String msgNoSuchWsdl(String uri) {
    return this.format("The repository does not contain a resource at the URI {0}.", uri);
  }

  /** Unable to open resource repository: {0}. */
  public String msgIoErrOpeningRr(String err) {
    return this.format("Unable to open resource repository: {0}.", err);
  }

  /** The location {0} is not a resource repository. */
  public String msgInvalidRrDirectory(String name) {
    return this.format("The location {0} is not a resource repository.", name);
  }

  /** At least one BPEL URL must be specified. */
  public String msgAtLeastOneProcessRequired() {
    return this.format("At least one BPEL URL must be specified.");
  }

  /** Error reading stream at "{0}". */
  public String msgIoExReadingStream(URL streamUrl) {
    return this.format("Error reading stream at \"{0}\".", streamUrl);
  }

  /** Error reading stream at "{0}": {1} */
  public String msgIoExReadingStreamWithMsg(File file, String msg) {
    return this.format("Error reading file at \"{0}\": {1}", file, msg);
  }

  /** The BPEL URL "{0}" is malformed. */
  public String msgInvalidBpelUrl(String bpelUrl) {
    return this.format("The BPEL URL \"{0}\" is malformed.", bpelUrl);
  }

  /** The WSDL URL "{0}" is malformed. */
  public String msgInvalidWsdlUrl(String wsdlUrl) {
    return this.format("The WSDL URL \"{0}\" is malformed.", wsdlUrl);
  }

  /** No resource repository specified, using default URL resolution mechanism. */
  public String msgNoResourceRepository() {
    return this.format("No resource repository specified, using default"
        + " URL resolution mechanism.");
  }

  /**
   * [-cp addclasspath] [-o outfile] [-rr rrfile] [-sar [-sdd sdd-url]]
   * '{'wsdl-url '}' '{' bpel-url, ... '}'
   */
  String strBpelcUsage() {
    return this.format("[-cp addclasspath] [-o outfile] [-rr rrfile] [-sar [-sdd sdd-url]]"
        + "'{'wsdl-url '}' '{' bpel-url, ... '}'");
  }

  /** Compile BPEL processes. */
  String strBpelcPrefix() {
    return this.format("Compile BPEL processes.");
  }

  // TODO
  String strBpelcSufix() {
    throw new UnsupportedOperationException();
  }

  // TODO
  public String msgBpelcInvalidOutputFile(String outfile) {
    throw new UnsupportedOperationException();
  }

  // TODO
  public String msgBpelcResourceRepositoryNotFound(String filename) {
    throw new UnsupportedOperationException();
  }

  // TODO
  public String msgBpelcResourceRepositoryIoError(String filename) {
    throw new UnsupportedOperationException();
  }

  /** Unable to create temporary rr file: {0} */
  public String msgUnableToCreateTempRr(String reason) {
    return this.format("Unable to create temporary rr file: {0}", reason);
  }

  // TODO
  public String msgBpelcSarCreateError(String message) {
    throw new UnsupportedOperationException();
  }

}
