package org.apache.ode.bpel.elang;

import java.net.URI;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.internal.matchers.StringContains.containsString;

public class URIResolverTest {

    @Test
    public void testResolveExistingFile() throws Exception {
        OXPath10Expression expr = new OXPath10Expression(null, null, null, null);
        URI baseResourceURI = getClass().getResource("/xpath20/").toURI();
        XslRuntimeUriResolver resolver = new XslRuntimeUriResolver(expr, baseResourceURI);
        StreamSource ss = (StreamSource)resolver.resolve("variables.xml", null);
        String result = IOUtils.toString(ss.getReader());
        
        assertThat(result, containsString("<variables>"));
    }

    @Test
    public void testResolveNonExistingFile() throws Exception {
        OXPath10Expression expr = new OXPath10Expression(null, null, null, null);
        URI baseResourceURI = getClass().getResource("/xpath20/").toURI();
        XslRuntimeUriResolver resolver = new XslRuntimeUriResolver(expr, baseResourceURI);

        assertNull(resolver.resolve("variablesa.xml", null));
    }

    @Test
    @Ignore("automated tests should not rely on remote connections.")
    public void testResolveURL() throws Exception {
        OXPath10Expression expr = new OXPath10Expression(null, null, null, null);
        URI baseResourceURI = getClass().getResource("/xpath20/").toURI();
        XslRuntimeUriResolver resolver = new XslRuntimeUriResolver(expr, baseResourceURI);
        StreamSource ss = (StreamSource)resolver.resolve("http://ode.apache.org/", null);
        String result = IOUtils.toString(ss.getReader());
        
        assertThat(result, containsString("Orchestration Director Engine"));
    }

}
