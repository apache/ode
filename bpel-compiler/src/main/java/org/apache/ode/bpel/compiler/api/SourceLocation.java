package org.apache.ode.bpel.compiler.api;

import java.net.URI;

/**
 * Interface for objects that have some relation to a particular
 * location in a source file.
 * 
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public interface SourceLocation {
    /**
     * URI of the source file.
     * @return
     */
    URI getURI();
    
    /**
     * Line number.
     * @return
     */
    int getLineNo();
    
    /**
     * Column number.
     * @return
     */
    int getColumnNo();
    
    /**
     * Location path (not a file path, but a path in an expression).
     * @return
     */
    String getPath();
}
