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

package org.apache.ode.bpel.elang.xpath10.compiler;

import org.apache.ode.bpel.compiler.api.CompilerContext;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import org.apache.ode.bpel.o.OXslSheet;

import javax.xml.transform.URIResolver;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

/**
 * Used to give the Xsl processor a way to access included XSL sheets
 * by using the CompilerContext (which uses RR behind the scene).
 */
public class XslCompileUriResolver implements URIResolver {

  private CompilerContext _cc;
  private OXPath10Expression _expr;

  public XslCompileUriResolver(CompilerContext cc, OXPath10Expression expr) {
    _cc = cc;
    _expr = expr;
  }

  public Source resolve(String href, String base) throws TransformerException {
    OXslSheet xslSheet = _cc.compileXslt(href);
    _expr.setXslSheet(xslSheet.uri, xslSheet);
    return new StreamSource(new StringReader(xslSheet.sheetBody));
  }
}
