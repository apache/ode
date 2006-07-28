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

import org.apache.ode.bom.wsdl.Definition4BPEL;
import org.apache.ode.bpel.compiler.WsdlFinder;
import org.apache.ode.utils.rr.RepositoryWsdlLocator;
import org.apache.ode.utils.rr.ResourceRepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;

import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Implementation of the {@link WsdlFinder} interface that obtains WSDLs from
 * inside a {@link ResourceRepository} object.
 */
public class RrWsdlFinder implements WsdlFinder {

  private static final Log __log = LogFactory.getLog(RrWsdlFinder.class);

  private ResourceRepository _rr;
  private URI _base;
  
  public RrWsdlFinder(ResourceRepository rr, URI base) {
    _rr = rr;
    _base = base;
  }

  public void setBaseURI(URI base) {
    _base = base;
  }

  public Definition4BPEL loadDefinition(WSDLReader r, URI uri) throws WSDLException {
    RepositoryWsdlLocator loc = new RepositoryWsdlLocator(_rr,resolve(uri));
    try {
      return (Definition4BPEL) r.readWSDL(loc);
    } catch (NullPointerException npe) {
      // Fix for WSDL4J 1.5.1. WSDL4J does not do a proper null check, so
      // it tends to throw NPEs if the resource is not found... We'll convert
      // this to a more friendly exception here.
      __log.debug("BUG:WSDL4J threw a NPE while reading WSDL URI " + uri +
              "; converting to WSDLException");
      WSDLException wsdlex = new WSDLException(WSDLException.OTHER_ERROR,
              "Unable to resolve document at '" +
              uri + "' in resource repository " + _rr);
      wsdlex.setLocation("/");
      throw wsdlex;
    }
  }


  public InputStream openResource(URI uri) throws MalformedURLException, IOException {
		return _rr.resourceAsStream(resolve(uri));
	}

  public String toString() {
    return "{RrWsdlFinder base=" + _base + " rr=" + _rr + "}";
  }
  
  /**
   * Resolve the URI relative to the base.
   * @param uri
   * @return
   */
  private URI resolve(URI uri) {
    if (_base == null)
      return uri;
    return _base.resolve(uri);
  }

}
