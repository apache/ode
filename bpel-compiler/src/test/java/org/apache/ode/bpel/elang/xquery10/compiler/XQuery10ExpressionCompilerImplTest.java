package org.apache.ode.bpel.elang.xquery10.compiler;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import junit.framework.TestCase;

public class XQuery10ExpressionCompilerImplTest extends TestCase {
    
    public void testGetVariableNames() throws Exception {
        String xq = "let $status := string($SomeVariable/ns:somePath)\n" + 
        		"                     return\n" + 
        		"                     if ($status = 'ABC' ) then\n" + 
        		"                        '123'\n" + 
        		"                     else $status (: workaround :)";

        assertMatches(xq);
    }

    public void testGetVariableNames_endsWithVariableName() throws Exception {
        String xq = "let $status := string($SomeVariable/ns:somePath)\n" + 
                "                     return\n" + 
                "                     if ($status = 'ABC' ) then\n" + 
                "                        '123'\n" + 
                "                     else $status";

        assertMatches(xq);
    }

    private void assertMatches(String xq) {
        Collection<String> varNames = XQuery10ExpressionCompilerImpl.getVariableNames(xq);
        Collection<String> expected = new LinkedHashSet(Arrays.asList("status", "SomeVariable"));
        
        assertEquals(2, varNames.size());
        assertEquals(expected, varNames);
    }

}
