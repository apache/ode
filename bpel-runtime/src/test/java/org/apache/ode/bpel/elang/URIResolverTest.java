package org.apache.ode.bpel.elang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.net.URI;

import javax.xml.transform.Source;

import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import org.apache.ode.utils.DOMUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

public class URIResolverTest {

    @Test
    public void testResolveExistingFile() throws Exception {
        OXPath10Expression expr = new OXPath10Expression(null, null, null, null);
        URI baseResourceURI = getClass().getResource("/xpath20/").toURI();
        XslRuntimeUriResolver resolver = new XslRuntimeUriResolver(expr, baseResourceURI);
        Source source = resolver.resolve("variables.xml", null);
        Document doc = DOMUtils.sourceToDOM(source);
        
        assertThat(DOMUtils.domToString(doc), containsString("<variables>"));
    }

    @Test
    public void testResolveNonExistingFile() throws Exception {
        OXPath10Expression expr = new OXPath10Expression(null, null, null, null);
        URI baseResourceURI = getClass().getResource("/xpath20/").toURI();
        XslRuntimeUriResolver resolver = new XslRuntimeUriResolver(expr, baseResourceURI);

        assertNull(resolver.resolve("variablesa.xml", null));
    }

    @Test
    public void testEncoding() throws Exception {
        OXPath10Expression expr = new OXPath10Expression(null, null, null, null);
        URI baseResourceURI = getClass().getResource("/xslt/").toURI();
        XslRuntimeUriResolver resolver = new XslRuntimeUriResolver(expr, baseResourceURI);

        Document doc = DOMUtils.sourceToDOM(resolver.resolve("test.xml", null));
        assertEquals("Prova lettere accentate: à è ì ò ù", doc.getDocumentElement().getTextContent().trim());
    }

    @Test
    @Ignore("automated tests should not rely on remote connections.")
    public void testResolveURL() throws Exception {
        OXPath10Expression expr = new OXPath10Expression(null, null, null, null);
        URI baseResourceURI = getClass().getResource("/xpath20/").toURI();
        XslRuntimeUriResolver resolver = new XslRuntimeUriResolver(expr, baseResourceURI);
        Source source = resolver.resolve("https://svn.apache.org/repos/asf/ode/trunk/bpel-schemas/src/main/xsd/pmapi.xsd", null);
        Document doc = DOMUtils.sourceToDOM(source);
        
        assertThat(DOMUtils.domToString(doc), containsString("activity-info"));
    }

}
