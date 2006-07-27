package org.apache.ode.bpel.iapi;

/**
 * Opaque representation of content. Implementation provided by integration
 * layer. In the engine, we don't manipulate content directly, so we 
 * don't care what this is. Excpetions are BPEL data manipulation activities 
 * (like ASSIGN) and expressions: implementations of these will have to be
 * specific to content (via a ContentHandler mechanism).  
 * @todo define content handler mechanism
 */
public interface Content {

}
