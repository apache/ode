package org.apache.ode.bpel.compiler.bom;

import org.w3c.dom.Element;

public class ScopeLikeActivity extends Activity {

    private Scope _scope;

    public ScopeLikeActivity(Element el) {
        super(el);
        _scope = new Scope(el);
    }

    
    public Scope getScope() {
        return _scope;
    }
    
}
