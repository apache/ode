package org.apache.ode.bpel.elang.xpath10.runtime;

import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import org.apache.ode.bpel.o.OXslSheet;

import javax.xml.transform.URIResolver;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.net.URI;
import java.io.StringReader;

/**
 * Used to give the Xsl processor a way to access included XSL sheets
 * by using the maps of sheets pre-processed at compilation time and
 * stored in the OXPath10Expression.
 */
public class XslRuntimeUriResolver implements URIResolver {

  private OXPath10Expression _expr;

  public XslRuntimeUriResolver(OXPath10Expression expr) {
    _expr = expr;
  }

  public Source resolve(String href, String base) throws TransformerException {
    URI uri = URI.create(href);
    OXslSheet sheet = _expr.xslSheets.get(uri);
    return new StreamSource(new StringReader(sheet.sheetBody));
  }
}
