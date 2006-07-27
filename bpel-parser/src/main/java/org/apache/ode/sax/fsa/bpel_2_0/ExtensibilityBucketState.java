/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.sax.fsa.bpel_2_0;

import org.apache.ode.sax.fsa.AbstractState;
import org.apache.ode.sax.fsa.ParseContext;
import org.apache.ode.sax.fsa.ParseException;
import org.apache.ode.sax.fsa.State;
import org.apache.ode.sax.fsa.StateFactory;
import org.apache.ode.sax.evt.SaxEvent;
import org.apache.ode.sax.evt.StartElement;

/**
 * Bucket state to capture schema-level extensibility elements. Extensibility
 * attributes must be handled elsewhere.
 */
class ExtensibilityBucketState
    extends AbstractState
{

    private static final StateFactory _factory = new Factory();

    ExtensibilityBucketState(StartElement se, ParseContext pc) {
        super(pc);
    }

    static class Factory implements StateFactory
    {
        public State newInstance(StartElement se, ParseContext pc)
                throws ParseException {
            return new ExtensibilityBucketState(se, pc);
        }
    }

    public void handleSaxEvent(SaxEvent se) throws ParseException {
        /*
         * For the moment, this is a no-op implementation, but if supporting
         * extensions is desired, those extensions can be hooked from here.
         * Ideally, we'd have some kind of registry implementation that routes
         * SaxEvent streams based on URI or some other scheme. However, for the
         * moment, we don't have any use cases. WS-BPEL 2.0 extensibility can be
         * implemented according to the spec, once that's settled.
         */
    }

    public StateFactory getFactory() {
        return _factory;
    }

    public int getType() {
        return EXTENSIBILITY_ELEMENT;
    }
}
