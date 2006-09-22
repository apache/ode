package org.apache.ode.jacob;

/**
 * Interface implemented by JACOB objects that are to be indexed. 
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public interface IndexedObject {

    /**
     * Get the value of the object's index.
     * @return
     */
    public Object getKey();
    
}
